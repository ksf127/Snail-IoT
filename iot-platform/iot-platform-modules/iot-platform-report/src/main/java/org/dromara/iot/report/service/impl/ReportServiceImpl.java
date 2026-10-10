package org.dromara.iot.report.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.apache.fesod.sheet.write.metadata.WriteSheet;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.excel.utils.ExcelWriterWrapper;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.common.enums.DeviceOnlineStatusEnum;
import org.dromara.iot.device.domain.IotDevice;
import org.dromara.iot.device.domain.IotProduct;
import org.dromara.iot.device.domain.vo.IotDeviceVo;
import org.dromara.iot.device.domain.vo.IotProductVo;
import org.dromara.iot.device.mapper.IotDeviceMapper;
import org.dromara.iot.device.mapper.IotProductMapper;
import org.dromara.iot.device.service.IIotDeviceService;
import org.dromara.iot.report.service.IReportService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 报表 服务层实现
 * <p>
 * - 设备数据统计：设备维度取自 PostgreSQL，上报量/来源/TOP 设备取自 Elasticsearch 聚合（未启用 ES 时自动降级）；
 * - 批量上传：使用 iot-common-excel 的 ExcelBuilder.read 解析 Excel，按唯一键（deviceCode / productCode）新增或更新；
 * - 批量下载：使用 ExcelBuilder.of(...).zip() 打包多页数据后写入 HttpServletResponse。
 *
 * @author iot team
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements IReportService {

    /**
     * 业务类型：设备
     */
    private static final String BIZ_TYPE_DEVICE = "device";

    /**
     * 业务类型：产品
     */
    private static final String BIZ_TYPE_PRODUCT = "product";

    /**
     * ES 聚合桶名称：按设备编码
     */
    private static final String AGG_BY_DEVICE = "byDevice";

    /**
     * ES 聚合桶名称：按数据来源
     */
    private static final String AGG_BY_SOURCE = "bySource";

    /**
     * ES 聚合返回的最大设备数量
     */
    private static final int AGG_DEVICE_SIZE = 20;

    /**
     * ES 聚合返回的最大来源数量
     */
    private static final int AGG_SOURCE_SIZE = 10;

    /**
     * 导出时分页读取的单页数据量
     */
    private static final int EXPORT_PAGE_SIZE = 1000;

    /**
     * 导入进度落缓存的刷新间隔（每处理 N 行刷新一次）
     */
    private static final int IMPORT_PROGRESS_STEP = 100;

    /**
     * 导入任务状态：进行中
     */
    private static final String STATUS_RUNNING = "RUNNING";

    /**
     * 导入任务状态：成功
     */
    private static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * 导入任务状态：失败
     */
    private static final String STATUS_FAIL = "FAIL";

    /**
     * 导入任务缓存有效期
     */
    private static final Duration IMPORT_TASK_TTL = Duration.ofSeconds(IotConstants.REPORT_IMPORT_TASK_TTL_SECONDS);

    /**
     * 分片上传会话缓存有效期
     */
    private static final Duration CHUNK_SESSION_TTL = Duration.ofSeconds(IotConstants.REPORT_CHUNK_SESSION_TTL_SECONDS);

    /**
     * 分片总数上限
     */
    private static final int MAX_CHUNK_COUNT = 10000;

    /**
     * 分片临时文件后缀
     */
    private static final String CHUNK_FILE_SUFFIX = ".part";

    /**
     * 合并后临时文件后缀
     */
    private static final String MERGED_FILE_SUFFIX = ".merged";

    private final IIotDeviceService deviceService;
    private final IotDeviceMapper deviceMapper;
    private final IotProductMapper productMapper;
    private final ObjectProvider<ElasticsearchClient> elasticsearchClientProvider;

    /**
     * 批量导入异步执行线程池（守护线程；队列满时由调用线程执行，形成背压）
     */
    private final ThreadPoolExecutor reportImportExecutor = createImportExecutor();

    /**
     * 设备数据统计报表
     *
     * @param query 统计条件（时间范围、产品、设备等）
     * @return 统计结果
     */
    @Override
    public Map<String, Object> deviceDataReport(Map<String, Object> query) {
        Map<String, Object> params = query == null ? Map.of() : query;
        String beginTime = asText(params.get("beginTime"));
        String endTime = asText(params.get("endTime"));

        long total = deviceService.countByOnlineStatus(null);
        long online = deviceService.countByOnlineStatus(DeviceOnlineStatusEnum.ONLINE.getCode());
        long offline = deviceService.countByOnlineStatus(DeviceOnlineStatusEnum.OFFLINE.getCode());
        long unknown = deviceService.countByOnlineStatus(DeviceOnlineStatusEnum.UNKNOWN.getCode());

        Map<String, Object> result = new LinkedHashMap<>(16);
        result.put("统计时间范围", StrUtil.isBlank(beginTime) && StrUtil.isBlank(endTime)
            ? "全部" : StrUtil.blankToDefault(beginTime, "-") + " ~ " + StrUtil.blankToDefault(endTime, "-"));
        result.put("设备总数", total);
        result.put("在线设备数", online);
        result.put("离线设备数", offline);
        result.put("未知状态设备数", unknown);
        result.put("设备在线率", total == 0 ? "0.00%" : String.format("%.2f%%", online * 100.0D / total));

        EsStatistic statistic = queryElasticsearch(parseMillis(beginTime), parseMillis(endTime));
        if (statistic == null) {
            result.put("上报数据总量", 0L);
            result.put("上报设备数", 0);
            result.put("数据来源分布", "Elasticsearch 未启用或不可用");
            result.put("上报次数TOP设备", "-");
        } else {
            result.put("上报数据总量", statistic.total());
            result.put("上报设备数", statistic.deviceCount());
            result.put("数据来源分布", statistic.sourceSummary());
            result.put("上报次数TOP设备", statistic.deviceSummary());
        }
        return result;
    }

    /**
     * 批量上传（Excel 导入设备数据）
     *
     * @param bizType  业务类型（如 device / product）
     * @param fileName 文件名
     * @param content  文件二进制内容
     * @return 导入结果摘要
     */
    @Override
    public Map<String, Object> batchUpload(String bizType, String fileName, byte[] content) {
        if (content == null || content.length == 0) {
            throw new ServiceException("导入文件内容为空：" + fileName);
        }
        checkBizType(bizType);
        String taskId = IdUtil.fastSimpleUUID();
        ImportTask task = new ImportTask(taskId, bizType, fileName);
        cacheImportTask(task);
        Map<String, Object> result;
        try {
            result = doImport(bizType, new ByteArrayInputStream(content), task);
        } catch (Exception e) {
            task.fail(e.getMessage());
            cacheImportTask(task);
            if (e instanceof ServiceException se) {
                throw se;
            }
            log.error("[IoT] 批量导入失败, bizType={}, fileName={}", bizType, fileName, e);
            throw new ServiceException("Excel 解析失败：" + e.getMessage());
        }
        task.finish(result);
        cacheImportTask(task);
        result.put("taskId", taskId);
        result.put("status", task.toMap().get("status"));
        return result;
    }

    /**
     * 异步批量上传（大文件导入）
     * <p>
     * 立即返回 RUNNING 状态与 taskId，由后台线程池解析入库；
     * 进度与结果写入 Redis，可通过 {@link #getUploadResult} 轮询。
     *
     * @param bizType  业务类型
     * @param fileName 文件名
     * @param content  文件二进制内容
     * @return 任务摘要（taskId + status）
     */
    @Override
    public Map<String, Object> batchUploadAsync(String bizType, String fileName, byte[] content) {
        if (content == null || content.length == 0) {
            throw new ServiceException("导入文件内容为空：" + fileName);
        }
        checkBizType(bizType);
        String taskId = IdUtil.fastSimpleUUID();
        ImportTask task = new ImportTask(taskId, bizType, fileName);
        cacheImportTask(task);
        submitImport(task, () -> new ByteArrayInputStream(content), null);
        return task.toMap();
    }

    /**
     * 初始化分片上传会话
     *
     * @param bizType     业务类型
     * @param fileName    文件名
     * @param totalChunks 分片总数
     * @param fileSize    文件总字节数
     * @return 会话信息（含 uploadId）
     */
    @Override
    public Map<String, Object> initChunkUpload(String bizType, String fileName, int totalChunks, long fileSize) {
        checkBizType(bizType);
        if (StrUtil.isBlank(fileName)) {
            throw new ServiceException("文件名不能为空");
        }
        if (totalChunks <= 0 || totalChunks > MAX_CHUNK_COUNT) {
            throw new ServiceException("分片总数非法：" + totalChunks);
        }
        String uploadId = IdUtil.fastSimpleUUID();
        Map<String, Object> session = new LinkedHashMap<>(8);
        session.put("uploadId", uploadId);
        session.put("bizType", bizType);
        session.put("fileName", fileName);
        session.put("totalChunks", totalChunks);
        session.put("fileSize", fileSize);
        session.put("received", new ArrayList<String>());
        RedisUtils.setCacheObject(chunkSessionKey(uploadId), session, CHUNK_SESSION_TTL);
        return session;
    }

    /**
     * 上传单个分片
     *
     * @param uploadId   上传会话ID
     * @param chunkIndex 分片序号（从 0 开始）
     * @param content    分片内容
     * @return 上传进度
     */
    @Override
    public Map<String, Object> uploadChunk(String uploadId, int chunkIndex, byte[] content) {
        Map<String, Object> session = requireChunkSession(uploadId);
        int totalChunks = toInt(session.get("totalChunks"));
        if (chunkIndex < 0 || chunkIndex >= totalChunks) {
            throw new ServiceException("分片序号越界：" + chunkIndex);
        }
        if (content == null || content.length == 0) {
            throw new ServiceException("分片内容为空：" + chunkIndex);
        }
        try {
            Path dir = chunkDir(uploadId);
            Files.createDirectories(dir);
            Files.write(dir.resolve(chunkIndex + CHUNK_FILE_SUFFIX), content);
        } catch (IOException e) {
            throw new ServiceException("分片写入失败：" + e.getMessage());
        }
        List<String> received = receivedList(session);
        String key = String.valueOf(chunkIndex);
        if (!received.contains(key)) {
            received.add(key);
        }
        RedisUtils.setCacheObject(chunkSessionKey(uploadId), session, CHUNK_SESSION_TTL);
        Map<String, Object> resp = new LinkedHashMap<>(8);
        resp.put("uploadId", uploadId);
        resp.put("chunkIndex", chunkIndex);
        resp.put("receivedCount", received.size());
        resp.put("totalChunks", totalChunks);
        return resp;
    }

    /**
     * 合并分片并异步导入
     *
     * @param uploadId 上传会话ID
     * @return 任务摘要（taskId + status）
     */
    @Override
    public Map<String, Object> mergeChunks(String uploadId) {
        Map<String, Object> session = requireChunkSession(uploadId);
        int totalChunks = toInt(session.get("totalChunks"));
        List<String> received = receivedList(session);
        if (received.size() < totalChunks) {
            throw new ServiceException("分片尚未全部上传，已完成 " + received.size() + "/" + totalChunks);
        }
        String bizType = String.valueOf(session.get("bizType"));
        String fileName = String.valueOf(session.get("fileName"));
        Path merged = mergedFile(uploadId);
        try (OutputStream out = Files.newOutputStream(merged,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < totalChunks; i++) {
                Path part = chunkDir(uploadId).resolve(i + CHUNK_FILE_SUFFIX);
                if (!Files.exists(part)) {
                    throw new ServiceException("分片缺失：" + i);
                }
                Files.copy(part, out);
            }
        } catch (IOException e) {
            throw new ServiceException("分片合并失败：" + e.getMessage());
        }
        RedisUtils.deleteObject(chunkSessionKey(uploadId));
        deleteChunkDir(uploadId);

        String taskId = IdUtil.fastSimpleUUID();
        ImportTask task = new ImportTask(taskId, bizType, fileName);
        cacheImportTask(task);
        submitImport(task, () -> {
            try {
                return Files.newInputStream(merged);
            } catch (IOException e) {
                throw new ServiceException("读取合并文件失败：" + e.getMessage());
            }
        }, () -> deleteQuietly(merged));
        return task.toMap();
    }

    /**
     * 查询批量导入任务进度与结果（缓存）
     *
     * @param taskId 任务ID
     * @return 任务进度与结果
     */
    @Override
    public Map<String, Object> getUploadResult(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        Map<String, Object> task = RedisUtils.getCacheObject(IotConstants.REPORT_IMPORT_TASK_PREFIX + taskId);
        if (task == null) {
            throw new ServiceException("导入任务不存在或已过期：" + taskId);
        }
        return task;
    }

    /**
     * 批量下载（按查询条件打包导出）
     *
     * @param bizType  业务类型
     * @param query    查询条件
     * @param response HTTP 响应
     */
    @Override
    public void batchDownload(String bizType, Map<String, Object> query, HttpServletResponse response) {
        Map<String, Object> params = query == null ? Map.of() : query;
        String beginTime = asText(params.get("beginTime"));
        String endTime = asText(params.get("endTime"));

        if (BIZ_TYPE_DEVICE.equals(bizType)) {
            LambdaQueryWrapper<IotDevice> lqw = Wrappers.lambdaQuery();
            lqw.ge(StrUtil.isNotBlank(beginTime), IotDevice::getCreateTime, beginTime);
            lqw.le(StrUtil.isNotBlank(endTime), IotDevice::getCreateTime, endTime);
            lqw.orderByDesc(IotDevice::getCreateTime);
            WriteSheet sheet = ExcelWriterWrapper.buildSheet("设备数据");
            ExcelBuilder.writer(IotDeviceVo.class).sheetName("设备数据").toResponse(response,
                wrapper -> writePages(wrapper, sheet,
                    (Page<IotDevice> page) -> deviceMapper.selectVoPage(page, lqw).getRecords()));
            return;
        }
        if (BIZ_TYPE_PRODUCT.equals(bizType)) {
            LambdaQueryWrapper<IotProduct> lqw = Wrappers.lambdaQuery();
            lqw.ge(StrUtil.isNotBlank(beginTime), IotProduct::getCreateTime, beginTime);
            lqw.le(StrUtil.isNotBlank(endTime), IotProduct::getCreateTime, endTime);
            lqw.orderByDesc(IotProduct::getCreateTime);
            WriteSheet sheet = ExcelWriterWrapper.buildSheet("产品数据");
            ExcelBuilder.writer(IotProductVo.class).sheetName("产品数据").toResponse(response,
                wrapper -> writePages(wrapper, sheet,
                    (Page<IotProduct> page) -> productMapper.selectVoPage(page, lqw).getRecords()));
            return;
        }
        throw new ServiceException("不支持的批量业务类型：" + bizType);
    }

    /**
     * 查询支持的批量业务类型
     *
     * @return 业务类型列表
     */
    @Override
    public List<String> listBizTypes() {
        return List.of(BIZ_TYPE_DEVICE, BIZ_TYPE_PRODUCT);
    }

    /**
     * 写入导入任务进度/结果到缓存
     *
     * @param task 导入任务
     */
    private void cacheImportTask(ImportTask task) {
        RedisUtils.setCacheObject(IotConstants.REPORT_IMPORT_TASK_PREFIX + task.getTaskId(), task.toMap(), IMPORT_TASK_TTL);
    }

    /**
     * 分页读取数据并逐页写入同一工作表，避免大表一次性加载进内存
     *
     * @param wrapper  Excel 写出包装器
     * @param sheet    目标工作表
     * @param loader   分页加载器（入参为实体分页，返回 VO 列表）
     * @param <E>      实体类型
     * @param <V>      行数据类型（VO）
     */
    private <E, V> void writePages(ExcelWriterWrapper<V> wrapper, WriteSheet sheet, Function<Page<E>, List<V>> loader) {
        long current = 1L;
        while (true) {
            List<V> records = loader.apply(new Page<>(current, EXPORT_PAGE_SIZE));
            if (records == null || records.isEmpty()) {
                break;
            }
            wrapper.write(records, sheet);
            if (records.size() < EXPORT_PAGE_SIZE) {
                break;
            }
            current++;
        }
    }

    /**
     * 解析设备 Excel 并按设备编码新增或更新
     *
     * @param content 文件内容
     * @return 导入结果摘要
     */
    private Map<String, Object> importDevices(InputStream in, ImportTask task) {
        List<IotDeviceVo> rows = ExcelBuilder.read(in, IotDeviceVo.class).doReadSync();
        task.setTotal(rows.size());
        Map<String, Long> productIdByName = loadProductIdByName();
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (int i = 0; i < rows.size(); i++) {
            task.setProcessed(i + 1);
            IotDeviceVo row = rows.get(i);
            int lineNo = i + 2;
            String deviceCode = StrUtil.trim(row.getDeviceCode());
            if (StrUtil.isBlank(deviceCode)) {
                errors.add("第 " + lineNo + " 行：设备编码为空");
                continue;
            }
            if (StrUtil.isBlank(row.getDeviceName())) {
                errors.add("第 " + lineNo + " 行：设备名称为空");
                continue;
            }
            Long productId = null;
            if (StrUtil.isNotBlank(row.getProductName())) {
                productId = productIdByName.get(StrUtil.trim(row.getProductName()));
                if (productId == null) {
                    errors.add("第 " + lineNo + " 行：产品[" + row.getProductName() + "]不存在");
                    continue;
                }
            }
            IotDevice entity = MapstructUtils.convert(row, IotDevice.class);
            entity.setDeviceCode(deviceCode);
            entity.setProductId(productId);
            IotDevice exist = deviceMapper.selectOne(Wrappers.<IotDevice>lambdaQuery()
                .eq(IotDevice::getDeviceCode, deviceCode).last("limit 1"));
            if (exist == null) {
                entity.setProtocol(StrUtil.blankToDefault(entity.getProtocol(), "MQTT"));
                entity.setOnlineStatus(StrUtil.blankToDefault(entity.getOnlineStatus(),
                    DeviceOnlineStatusEnum.UNKNOWN.getCode()));
                entity.setStatus(StrUtil.blankToDefault(entity.getStatus(), "0"));
                deviceMapper.insert(entity);
            } else {
                entity.setDeviceId(exist.getDeviceId());
                deviceMapper.updateById(entity);
            }
            success++;
            if ((i + 1) % IMPORT_PROGRESS_STEP == 0) {
                task.update(success, errors.size());
                cacheImportTask(task);
            }
        }
        task.update(success, errors.size());
        return buildUploadResult(rows.size(), success, errors);
    }

    /**
     * 解析产品 Excel 并按产品编码新增或更新
     *
     * @param content 文件内容
     * @return 导入结果摘要
     */
    private Map<String, Object> importProducts(InputStream in, ImportTask task) {
        List<IotProductVo> rows = ExcelBuilder.read(in, IotProductVo.class).doReadSync();
        task.setTotal(rows.size());
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (int i = 0; i < rows.size(); i++) {
            task.setProcessed(i + 1);
            IotProductVo row = rows.get(i);
            int lineNo = i + 2;
            String productCode = StrUtil.trim(row.getProductCode());
            if (StrUtil.isBlank(productCode)) {
                errors.add("第 " + lineNo + " 行：产品编码为空");
                continue;
            }
            if (StrUtil.isBlank(row.getProductName())) {
                errors.add("第 " + lineNo + " 行：产品名称为空");
                continue;
            }
            IotProduct entity = MapstructUtils.convert(row, IotProduct.class);
            entity.setProductCode(productCode);
            IotProduct exist = productMapper.selectOne(Wrappers.<IotProduct>lambdaQuery()
                .eq(IotProduct::getProductCode, productCode).last("limit 1"));
            if (exist == null) {
                entity.setProtocol(StrUtil.blankToDefault(entity.getProtocol(), "MQTT"));
                entity.setDataFormat(StrUtil.blankToDefault(entity.getDataFormat(), "JSON"));
                entity.setStatus(StrUtil.blankToDefault(entity.getStatus(), "0"));
                productMapper.insert(entity);
            } else {
                entity.setProductId(exist.getProductId());
                productMapper.updateById(entity);
            }
            success++;
            if ((i + 1) % IMPORT_PROGRESS_STEP == 0) {
                task.update(success, errors.size());
                cacheImportTask(task);
            }
        }
        task.update(success, errors.size());
        return buildUploadResult(rows.size(), success, errors);
    }

    /**
     * 加载产品名称到产品ID的映射
     *
     * @return 产品名称 -> 产品ID
     */
    private Map<String, Long> loadProductIdByName() {
        List<IotProduct> products = productMapper.selectList(Wrappers.<IotProduct>lambdaQuery()
            .select(IotProduct::getProductId, IotProduct::getProductName));
        Map<String, Long> idByName = new HashMap<>(products.size());
        for (IotProduct product : products) {
            if (StrUtil.isNotBlank(product.getProductName())) {
                idByName.putIfAbsent(StrUtil.trim(product.getProductName()), product.getProductId());
            }
        }
        return idByName;
    }

    /**
     * 组装导入结果摘要
     *
     * @param total   总条数
     * @param success 成功条数
     * @param errors  失败明细
     * @return 导入结果摘要
     */
    private Map<String, Object> buildUploadResult(int total, int success, List<String> errors) {
        Map<String, Object> result = new LinkedHashMap<>(8);
        result.put("total", total);
        result.put("successCount", success);
        result.put("failCount", total - success);
        result.put("errors", errors);
        return result;
    }

    /**
     * 通过 Elasticsearch 聚合查询设备上报数据统计
     *
     * @param beginMillis 起始时间戳（毫秒），为空表示不限
     * @param endMillis   结束时间戳（毫秒），为空表示不限
     * @return 统计结果，ES 未启用或查询失败时返回 null
     */
    private EsStatistic queryElasticsearch(Long beginMillis, Long endMillis) {
        ElasticsearchClient client = elasticsearchClientProvider.getIfAvailable();
        if (client == null) {
            log.warn("[IoT] Elasticsearch 客户端不可用，报表仅展示设备维度统计");
            return null;
        }
        try {
            SearchRequest request = SearchRequest.of(s -> {
                s.index(IotConstants.ES_INDEX_DEVICE_DATA).size(0);
                if (beginMillis != null || endMillis != null) {
                    s.query(q -> q.range(r -> {
                        r.field("timestamp");
                        if (beginMillis != null) {
                            r.gte(JsonData.of(beginMillis));
                        }
                        if (endMillis != null) {
                            r.lte(JsonData.of(endMillis));
                        }
                        return r;
                    }));
                }
                s.aggregations(AGG_BY_DEVICE, a -> a.terms(t -> t.field("deviceCode").size(AGG_DEVICE_SIZE)));
                s.aggregations(AGG_BY_SOURCE, a -> a.terms(t -> t.field("source").size(AGG_SOURCE_SIZE)));
                return s;
            });
            SearchResponse<Void> response = client.search(request, Void.class);
            long total = response.hits().total() == null ? 0L : response.hits().total().value();
            List<StringTermsBucket> deviceBuckets = buckets(response, AGG_BY_DEVICE);
            List<StringTermsBucket> sourceBuckets = buckets(response, AGG_BY_SOURCE);
            return new EsStatistic(total, deviceBuckets.size(),
                summarize(sourceBuckets), summarize(deviceBuckets));
        } catch (Exception e) {
            log.warn("[IoT] Elasticsearch 聚合查询失败，报表降级为设备维度统计: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 提取 terms 聚合桶
     *
     * @param response 查询响应
     * @param name     聚合名称
     * @return 字符串 terms 桶列表
     */
    private List<StringTermsBucket> buckets(SearchResponse<Void> response, String name) {
        Aggregate aggregate = response.aggregations().get(name);
        if (aggregate == null || !aggregate.isSterms()) {
            return List.of();
        }
        return aggregate.sterms().buckets().array();
    }

    /**
     * 将聚合桶拼接为 “维度值(数量)” 形式的中文描述
     *
     * @param buckets 聚合桶
     * @return 描述文本
     */
    private String summarize(List<StringTermsBucket> buckets) {
        if (buckets.isEmpty()) {
            return "无";
        }
        return buckets.stream()
            .map(bucket -> bucket.key().stringValue() + "(" + bucket.docCount() + ")")
            .collect(Collectors.joining("；"));
    }

    /**
     * 解析前端传入的时间文本
     *
     * @param text 时间文本（yyyy-MM-dd HH:mm:ss）
     * @return 毫秒时间戳，解析失败返回 null
     */
    private Long parseMillis(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return DateUtil.parse(text).getTime();
        } catch (Exception e) {
            log.warn("[IoT] 无法解析统计时间：{}", text);
            return null;
        }
    }

    /**
     * 转换为文本
     *
     * @param value 原始值
     * @return 文本
     */
    private String asText(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * 校验批量业务类型是否受支持
     *
     * @param bizType 业务类型
     */
    private void checkBizType(String bizType) {
        if (!BIZ_TYPE_DEVICE.equals(bizType) && !BIZ_TYPE_PRODUCT.equals(bizType)) {
            throw new ServiceException("不支持的批量业务类型：" + bizType);
        }
    }

    /**
     * 分派导入逻辑
     *
     * @param bizType 业务类型
     * @param in      文件输入流
     * @param task    导入任务
     * @return 导入结果摘要
     */
    private Map<String, Object> doImport(String bizType, InputStream in, ImportTask task) {
        if (BIZ_TYPE_DEVICE.equals(bizType)) {
            return importDevices(in, task);
        }
        if (BIZ_TYPE_PRODUCT.equals(bizType)) {
            return importProducts(in, task);
        }
        throw new ServiceException("不支持的批量业务类型：" + bizType);
    }

    /**
     * 提交异步导入任务
     *
     * @param task           导入任务
     * @param sourceSupplier 输入流提供者
     * @param afterImport    导入完成后的清理动作（可空）
     */
    private void submitImport(ImportTask task, Supplier<InputStream> sourceSupplier, Runnable afterImport) {
        reportImportExecutor.execute(() -> {
            try (InputStream in = sourceSupplier.get()) {
                Map<String, Object> result = doImport(task.bizType, in, task);
                task.finish(result);
            } catch (Exception e) {
                log.error("[IoT] 异步导入失败, taskId={}, bizType={}", task.taskId, task.bizType, e);
                task.fail(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            } finally {
                cacheImportTask(task);
                if (afterImport != null) {
                    afterImport.run();
                }
            }
        });
    }

    /**
     * 创建批量导入线程池
     *
     * @return 线程池
     */
    private static ThreadPoolExecutor createImportExecutor() {
        AtomicInteger seq = new AtomicInteger(1);
        return new ThreadPoolExecutor(2, 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(256),
            r -> {
                Thread thread = new Thread(r, "report-import-" + seq.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 分片上传会话缓存 Key
     *
     * @param uploadId 上传会话ID
     * @return 缓存 Key
     */
    private String chunkSessionKey(String uploadId) {
        return IotConstants.REPORT_CHUNK_SESSION_PREFIX + uploadId;
    }

    /**
     * 读取并校验分片上传会话
     *
     * @param uploadId 上传会话ID
     * @return 会话信息
     */
    private Map<String, Object> requireChunkSession(String uploadId) {
        if (StrUtil.isBlank(uploadId)) {
            throw new ServiceException("上传ID不能为空");
        }
        Map<String, Object> session = RedisUtils.getCacheObject(chunkSessionKey(uploadId));
        if (session == null) {
            throw new ServiceException("分片上传会话不存在或已过期：" + uploadId);
        }
        return session;
    }

    /**
     * 获取会话中已接收分片列表（缺失时补建）
     *
     * @param session 会话信息
     * @return 已接收分片序号
     */
    @SuppressWarnings("unchecked")
    private List<String> receivedList(Map<String, Object> session) {
        Object received = session.get("received");
        if (received instanceof List<?> list) {
            return (List<String>) list;
        }
        List<String> created = new ArrayList<>();
        session.put("received", created);
        return created;
    }

    /**
     * 整数值转换
     *
     * @param value 原始值
     * @return 整型值
     */
    private int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
    }

    /**
     * 分片根目录
     *
     * @return 根目录
     */
    private Path chunkRoot() {
        return Paths.get(System.getProperty("java.io.tmpdir"), IotConstants.REPORT_CHUNK_TEMP_DIR);
    }

    /**
     * 分片目录
     *
     * @param uploadId 上传会话ID
     * @return 分片目录
     */
    private Path chunkDir(String uploadId) {
        return chunkRoot().resolve(uploadId);
    }

    /**
     * 合并文件路径
     *
     * @param uploadId 上传会话ID
     * @return 合并文件路径
     */
    private Path mergedFile(String uploadId) {
        return chunkRoot().resolve(uploadId + MERGED_FILE_SUFFIX);
    }

    /**
     * 删除分片目录
     *
     * @param uploadId 上传会话ID
     */
    private void deleteChunkDir(String uploadId) {
        Path dir = chunkDir(uploadId);
        if (!Files.exists(dir)) {
            return;
        }
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(this::deleteQuietly);
        } catch (IOException e) {
            log.warn("[IoT] 清理分片目录失败：{}", e.getMessage());
        }
    }

    /**
     * 安静删除文件（忽略异常）
     *
     * @param path 文件路径
     */
    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("[IoT] 删除临时文件失败：{}, {}", path, e.getMessage());
        }
    }

    /**
     * Elasticsearch 统计结果
     *
     * @param total         上报数据总量
     * @param deviceCount   上报设备数
     * @param sourceSummary 数据来源分布
     * @param deviceSummary 上报次数 TOP 设备
     */
    private record EsStatistic(long total, int deviceCount, String sourceSummary, String deviceSummary) {
    }

    /**
     * 批量导入任务（进度 + 结果，用于缓存到 Redis）
     */
    private static final class ImportTask {

        private final String taskId;
        private final String bizType;
        private final String fileName;
        private final long startTime = System.currentTimeMillis();
        private String status = STATUS_RUNNING;
        private int total;
        private int processed;
        private int successCount;
        private int failCount;
        private String message;
        private List<String> errors = new ArrayList<>();

        ImportTask(String taskId, String bizType, String fileName) {
            this.taskId = taskId;
            this.bizType = bizType;
            this.fileName = fileName;
        }

        String getTaskId() {
            return taskId;
        }

        void setTotal(int total) {
            this.total = total;
        }

        void setProcessed(int processed) {
            this.processed = processed;
        }

        void update(int successCount, int failCount) {
            this.successCount = successCount;
            this.failCount = failCount;
        }

        void finish(Map<String, Object> result) {
            this.status = STATUS_SUCCESS;
            update(processed - failCount, failCount);
            Object errs = result.get("errors");
            if (errs instanceof List<?> list) {
                this.errors = list.stream().map(String::valueOf).collect(Collectors.toList());
            }
        }

        void fail(String message) {
            this.status = STATUS_FAIL;
            this.message = message;
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>(16);
            map.put("taskId", taskId);
            map.put("bizType", bizType);
            map.put("fileName", fileName);
            map.put("status", status);
            map.put("total", total);
            map.put("processed", processed);
            map.put("successCount", successCount);
            map.put("failCount", failCount);
            map.put("progress", total == 0 ? 0 : processed * 100 / total);
            map.put("errors", errors);
            map.put("elapsedMs", System.currentTimeMillis() - startTime);
            if (message != null) {
                map.put("message", message);
            }
            return map;
        }
    }

}
