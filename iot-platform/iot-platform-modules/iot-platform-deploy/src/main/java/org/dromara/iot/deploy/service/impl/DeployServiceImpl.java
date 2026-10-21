package org.dromara.iot.deploy.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.deploy.domain.IotDeployApp;
import org.dromara.iot.deploy.domain.IotDeployEnv;
import org.dromara.iot.deploy.domain.IotDeployTask;
import org.dromara.iot.deploy.domain.bo.IotDeployAppBo;
import org.dromara.iot.deploy.domain.bo.IotDeployEnvBo;
import org.dromara.iot.deploy.domain.bo.IotDeployTaskBo;
import org.dromara.iot.deploy.domain.vo.IotDeployAppVo;
import org.dromara.iot.deploy.domain.vo.IotDeployEnvVo;
import org.dromara.iot.deploy.domain.vo.IotDeployTaskVo;
import org.dromara.iot.deploy.enums.DeployStatusEnum;
import org.dromara.iot.deploy.mapper.IotDeployAppMapper;
import org.dromara.iot.deploy.mapper.IotDeployEnvMapper;
import org.dromara.iot.deploy.mapper.IotDeployTaskMapper;
import org.dromara.iot.deploy.service.IDeployService;
import org.dromara.iot.deploy.service.KubernetesDeployService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 自动化部署 服务层实现
 * <p>
 * - 发布任务落 iot_deploy_task 表，状态机：0待执行 -> 1执行中 -> 2成功 / 3失败；
 * - 以「应用编码 + 目标环境」为粒度加 Redis 分布式锁，避免同一应用同环境并发发布；
 * - 执行编排在独立守护线程池中异步进行，逐阶段写入部署日志。
 *
 * @author iot team
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DeployServiceImpl implements IDeployService {

    /**
     * 日志时间戳格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 滚动更新就绪等待超时（秒）
     */
    private static final long ROLLOUT_TIMEOUT_SECONDS = 300L;

    /**
     * 任务类型：发布
     */
    private static final String TASK_TYPE_DEPLOY = "deploy";

    /**
     * 任务类型：构建
     */
    private static final String TASK_TYPE_BUILD = "build";

    /**
     * 部署执行线程池（守护线程，避免阻塞应用关闭）
     */
    private static final ThreadPoolExecutor DEPLOY_EXECUTOR = createExecutor();

    private final IotDeployTaskMapper baseMapper;

    private final IotDeployAppMapper appMapper;

    private final IotDeployEnvMapper envMapper;

    private final KubernetesDeployService kubernetesDeployService;

    /**
     * 分页查询部署任务
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 部署任务分页结果
     */
    @Override
    public PageResult<IotDeployTaskVo> queryPageList(IotDeployTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotDeployTask> lqw = buildQueryWrapper(bo);
        Page<IotDeployTaskVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        fillAppName(page.getRecords());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 触发一次部署发布
     *
     * @param bo 发布参数（appCode / version / targetEnv）
     * @return 部署任务ID
     */
    @Override
    public Long release(IotDeployTaskBo bo) {
        String appCode = StrUtil.trim(bo.getAppCode());
        String version = StrUtil.trim(bo.getVersion());
        String targetEnv = StrUtil.trim(bo.getTargetEnv());
        if (StrUtil.hasBlank(appCode, version, targetEnv)) {
            throw new ServiceException("应用编码、版本号、目标环境均不能为空");
        }
        Long envCount = envMapper.selectCount(Wrappers.<IotDeployEnv>lambdaQuery()
            .eq(IotDeployEnv::getEnvCode, targetEnv)
            .eq(IotDeployEnv::getStatus, "0"));
        if (envCount == null || envCount == 0) {
            throw new ServiceException("目标环境不存在或已停用：" + targetEnv);
        }
        String lockKey = IotConstants.DEPLOY_TASK_LOCK_PREFIX + appCode + ":" + targetEnv;
        if (!RedisUtils.setObjectIfAbsent(lockKey, LocalDateTime.now(),
            Duration.ofSeconds(IotConstants.DEPLOY_TASK_LOCK_TTL_SECONDS))) {
            throw new ServiceException("应用 " + appCode + " 在 " + targetEnv + " 环境已有部署任务执行中，请稍后重试");
        }
        IotDeployTask task = new IotDeployTask();
        task.setAppCode(appCode);
        task.setVersion(version);
        task.setTargetEnv(targetEnv);
        task.setTaskType(TASK_TYPE_DEPLOY);
        task.setStatus(DeployStatusEnum.PENDING.getCode());
        task.setRemark(bo.getRemark());
        try {
            baseMapper.insert(task);
        } catch (RuntimeException e) {
            RedisUtils.deleteObject(lockKey);
            throw e;
        }
        Long taskId = task.getTaskId();
        DEPLOY_EXECUTOR.execute(() -> execute(taskId, lockKey));
        return taskId;
    }

    /**
     * 登记一条构建任务
     * <p>
     * 页面先触发 Jenkins 构建，再调用本方法落一条任务记录，使多项目的构建活动
     * 与发布任务在「部署任务」列表中统一可查。
     *
     * @param bo 构建参数（appCode / version / targetEnv）
     * @return 任务ID
     */
    @Override
    public Long createBuildTask(IotDeployTaskBo bo) {
        String appCode = StrUtil.trim(bo.getAppCode());
        String version = StrUtil.trim(bo.getVersion());
        String targetEnv = StrUtil.trim(bo.getTargetEnv());
        if (StrUtil.hasBlank(appCode, version, targetEnv)) {
            throw new ServiceException("应用编码、版本号、目标环境均不能为空");
        }
        Long appCount = appMapper.selectCount(Wrappers.<IotDeployApp>lambdaQuery()
            .eq(IotDeployApp::getAppCode, appCode));
        if (appCount == null || appCount == 0) {
            throw new ServiceException("应用不存在：" + appCode);
        }
        IotDeployTask task = new IotDeployTask();
        task.setAppCode(appCode);
        task.setVersion(version);
        task.setTargetEnv(targetEnv);
        task.setTaskType(TASK_TYPE_BUILD);
        task.setStatus(DeployStatusEnum.RUNNING.getCode());
        task.setStartTime(LocalDateTime.now());
        task.setRemark("构建应用");
        baseMapper.insert(task);
        return task.getTaskId();
    }

    /**
     * 回写构建任务结果
     *
     * @param taskId      任务ID
     * @param status      结果状态（2成功 3失败）
     * @param buildNumber Jenkins 构建号（可空）
     */
    @Override
    public void finishBuildTask(Long taskId, String status, Integer buildNumber) {
        if (!DeployStatusEnum.SUCCESS.getCode().equals(status)
            && !DeployStatusEnum.FAIL.getCode().equals(status)) {
            return;
        }
        IotDeployTask task = baseMapper.selectById(taskId);
        if (task == null || !TASK_TYPE_BUILD.equals(task.getTaskType())) {
            return;
        }
        task.setStatus(status);
        task.setBuildNumber(buildNumber);
        task.setFinishTime(LocalDateTime.now());
        baseMapper.updateById(task);
    }

    /**
     * 查询部署日志
     *
     * @param taskId 部署任务ID
     * @return 部署日志文本
     */
    @Override
    public String queryLog(Long taskId) {
        IotDeployTask task = baseMapper.selectById(taskId);
        return task == null || task.getDeployLog() == null ? "" : task.getDeployLog();
    }

    /**
     * 查询已登记的应用列表
     *
     * @return 应用列表
     */
    @Override
    public List<IotDeployAppVo> listApps() {
        return appMapper.selectVoList(Wrappers.<IotDeployApp>lambdaQuery()
            .eq(IotDeployApp::getStatus, "0")
            .orderByAsc(IotDeployApp::getSortNo));
    }

    /**
     * 查询已登记的环境列表
     *
     * @return 环境列表
     */
    @Override
    public List<IotDeployEnvVo> listEnvs() {
        return envMapper.selectVoList(Wrappers.<IotDeployEnv>lambdaQuery()
            .eq(IotDeployEnv::getStatus, "0")
            .orderByAsc(IotDeployEnv::getSortNo));
    }

    /**
     * 分页查询部署应用
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 应用分页结果
     */
    @Override
    public PageResult<IotDeployAppVo> queryAppPageList(IotDeployAppBo bo, PageQuery pageQuery) {
        Page<IotDeployAppVo> page = appMapper.selectVoPage(pageQuery.build(), buildAppQueryWrapper(bo));
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询部署应用详情
     *
     * @param appId 应用ID
     * @return 应用视图对象
     */
    @Override
    public IotDeployAppVo queryAppById(Long appId) {
        return appMapper.selectVoById(appId);
    }

    /**
     * 新增部署应用
     *
     * @param bo 应用业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertAppByBo(IotDeployAppBo bo) {
        validateAppCodeUnique(bo.getAppCode(), null);
        IotDeployApp add = MapstructUtils.convert(bo, IotDeployApp.class);
        return appMapper.insert(add) > 0;
    }

    /**
     * 修改部署应用
     *
     * @param bo 应用业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateAppByBo(IotDeployAppBo bo) {
        validateAppCodeUnique(bo.getAppCode(), bo.getAppId());
        IotDeployApp update = MapstructUtils.convert(bo, IotDeployApp.class);
        return appMapper.updateById(update) > 0;
    }

    /**
     * 批量删除部署应用
     *
     * @param ids 应用ID集合
     * @return 是否成功
     */
    @Override
    public Boolean deleteAppByIds(Collection<Long> ids) {
        return appMapper.deleteByIds(ids) > 0;
    }

    /**
     * 分页查询部署环境
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 环境分页结果
     */
    @Override
    public PageResult<IotDeployEnvVo> queryEnvPageList(IotDeployEnvBo bo, PageQuery pageQuery) {
        Page<IotDeployEnvVo> page = envMapper.selectVoPage(pageQuery.build(), buildEnvQueryWrapper(bo));
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询部署环境详情
     *
     * @param envId 环境ID
     * @return 环境视图对象
     */
    @Override
    public IotDeployEnvVo queryEnvById(Long envId) {
        return envMapper.selectVoById(envId);
    }

    /**
     * 新增部署环境
     *
     * @param bo 环境业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertEnvByBo(IotDeployEnvBo bo) {
        validateEnvCodeUnique(bo.getEnvCode(), null);
        IotDeployEnv add = MapstructUtils.convert(bo, IotDeployEnv.class);
        return envMapper.insert(add) > 0;
    }

    /**
     * 修改部署环境
     *
     * @param bo 环境业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateEnvByBo(IotDeployEnvBo bo) {
        validateEnvCodeUnique(bo.getEnvCode(), bo.getEnvId());
        IotDeployEnv update = MapstructUtils.convert(bo, IotDeployEnv.class);
        return envMapper.updateById(update) > 0;
    }

    /**
     * 批量删除部署环境
     *
     * @param ids 环境ID集合
     * @return 是否成功
     */
    @Override
    public Boolean deleteEnvByIds(Collection<Long> ids) {
        return envMapper.deleteByIds(ids) > 0;
    }

    /**
     * 校验应用编码唯一性
     *
     * @param appCode 应用编码
     * @param appId   应用ID（修改时排除自身）
     */
    private void validateAppCodeUnique(String appCode, Long appId) {
        Long count = appMapper.selectCount(Wrappers.<IotDeployApp>lambdaQuery()
            .eq(IotDeployApp::getAppCode, StrUtil.trim(appCode))
            .ne(appId != null, IotDeployApp::getAppId, appId));
        if (count != null && count > 0) {
            throw new ServiceException("应用编码已存在：" + appCode);
        }
    }

    /**
     * 校验环境编码唯一性
     *
     * @param envCode 环境编码
     * @param envId   环境ID（修改时排除自身）
     */
    private void validateEnvCodeUnique(String envCode, Long envId) {
        Long count = envMapper.selectCount(Wrappers.<IotDeployEnv>lambdaQuery()
            .eq(IotDeployEnv::getEnvCode, StrUtil.trim(envCode))
            .ne(envId != null, IotDeployEnv::getEnvId, envId));
        if (count != null && count > 0) {
            throw new ServiceException("环境编码已存在：" + envCode);
        }
    }

    /**
     * 构建部署应用查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotDeployApp> buildAppQueryWrapper(IotDeployAppBo bo) {
        LambdaQueryWrapper<IotDeployApp> lqw = Wrappers.lambdaQuery();
        lqw.like(StrUtil.isNotBlank(bo.getAppCode()), IotDeployApp::getAppCode, bo.getAppCode());
        lqw.like(StrUtil.isNotBlank(bo.getAppName()), IotDeployApp::getAppName, bo.getAppName());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotDeployApp::getStatus, bo.getStatus());
        lqw.orderByAsc(IotDeployApp::getSortNo);
        return lqw;
    }

    /**
     * 构建部署环境查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotDeployEnv> buildEnvQueryWrapper(IotDeployEnvBo bo) {
        LambdaQueryWrapper<IotDeployEnv> lqw = Wrappers.lambdaQuery();
        lqw.like(StrUtil.isNotBlank(bo.getEnvCode()), IotDeployEnv::getEnvCode, bo.getEnvCode());
        lqw.like(StrUtil.isNotBlank(bo.getEnvName()), IotDeployEnv::getEnvName, bo.getEnvName());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotDeployEnv::getStatus, bo.getStatus());
        lqw.orderByAsc(IotDeployEnv::getSortNo);
        return lqw;
    }

    /**
     * 异步执行部署编排
     * <p>
     * 六个阶段依次真实执行：校验发布参数 -> 解析制品坐标 -> 预检目标环境 -> 下发制品
     * -> 重启实例 -> 健康检查。任一阶段抛出异常即整体标记为失败，并保留阶段性日志。
     *
     * @param taskId  部署任务ID
     * @param lockKey 并发锁 Key
     */
    private void execute(Long taskId, String lockKey) {
        IotDeployTask task = baseMapper.selectById(taskId);
        if (task == null) {
            RedisUtils.deleteObject(lockKey);
            return;
        }
        StringBuilder logBuilder = new StringBuilder();
        task.setStatus(DeployStatusEnum.RUNNING.getCode());
        task.setStartTime(LocalDateTime.now());
        appendLog(logBuilder, "开始执行部署任务，应用=" + task.getAppCode()
            + "，版本=" + task.getVersion() + "，目标环境=" + task.getTargetEnv());
        persist(task, logBuilder);
        try {
            // 1. 校验发布参数
            beginStage(logBuilder, task, "校验发布参数");
            IotDeployApp app = appMapper.selectOne(Wrappers.<IotDeployApp>lambdaQuery()
                .eq(IotDeployApp::getAppCode, task.getAppCode()));
            if (app == null) {
                throw new ServiceException("未找到已登记的应用：" + task.getAppCode());
            }
            IotDeployEnv env = envMapper.selectOne(Wrappers.<IotDeployEnv>lambdaQuery()
                .eq(IotDeployEnv::getEnvCode, task.getTargetEnv()));
            if (env == null || StrUtil.isBlank(env.getK8sNamespace())) {
                throw new ServiceException("未找到目标环境或未配置命名空间：" + task.getTargetEnv());
            }
            String namespace = env.getK8sNamespace();
            String workload = StrUtil.trim(app.getK8sWorkload());
            String container = StrUtil.trim(app.getK8sContainer());
            if (StrUtil.isBlank(workload)) {
                throw new ServiceException("应用 " + app.getAppCode() + " 未配置 Kubernetes 工作负载（Deployment）名称");
            }
            endStage(logBuilder, task, "校验发布参数",
                "应用=" + app.getAppName() + "，命名空间=" + namespace + "，工作负载=" + workload);

            // 2. 解析制品坐标
            beginStage(logBuilder, task, "解析制品坐标");
            String image = resolveImage(app.getArtifact(), task.getVersion());
            endStage(logBuilder, task, "解析制品坐标", "目标镜像=" + image);

            // 3. 预检目标环境
            beginStage(logBuilder, task, "预检目标环境");
            if (!kubernetesDeployService.namespaceExists(namespace)) {
                throw new ServiceException("目标命名空间不存在：" + namespace);
            }
            V1Deployment deployment = kubernetesDeployService.readDeployment(namespace, workload);
            endStage(logBuilder, task, "预检目标环境",
                "工作负载副本数=" + kubernetesDeployService.desiredReplicas(deployment)
                    + "，当前镜像=" + kubernetesDeployService.currentImage(deployment, container));

            // 4. 下发制品
            beginStage(logBuilder, task, "下发制品");
            String previousImage = kubernetesDeployService.applyImage(namespace, workload, container, image);
            endStage(logBuilder, task, "下发制品", "容器镜像由 " + previousImage + " 更新为 " + image);

            // 5. 重启实例
            beginStage(logBuilder, task, "重启实例");
            kubernetesDeployService.restart(namespace, workload);
            endStage(logBuilder, task, "重启实例", "已触发滚动重启");

            // 6. 健康检查
            beginStage(logBuilder, task, "健康检查");
            kubernetesDeployService.waitRolloutReady(namespace, workload, ROLLOUT_TIMEOUT_SECONDS);
            endStage(logBuilder, task, "健康检查", "滚动更新完成，副本全部就绪");

            task.setStatus(DeployStatusEnum.SUCCESS.getCode());
            appendLog(logBuilder, "部署成功");
        } catch (ApiException e) {
            log.error("[IoT] 部署任务执行失败（Kubernetes API）, taskId={}", taskId, e);
            task.setStatus(DeployStatusEnum.FAIL.getCode());
            appendLog(logBuilder, "部署失败：Kubernetes API 调用异常（HTTP " + e.getCode() + "）"
                + StrUtil.blankToDefault(e.getResponseBody(), ""));
        } catch (Throwable e) {
            log.error("[IoT] 部署任务执行失败, taskId={}", taskId, e);
            task.setStatus(DeployStatusEnum.FAIL.getCode());
            appendLog(logBuilder, "部署失败：" + e.getClass().getSimpleName() + ": "
                + StrUtil.blankToDefault(e.getMessage(), "（无异常信息）"));
        } finally {
            task.setFinishTime(LocalDateTime.now());
            persist(task, logBuilder);
            RedisUtils.deleteObject(lockKey);
        }
    }

    /**
     * 解析目标镜像坐标
     * <p>
     * 版本号若已是完整镜像（含仓库路径或标签）则直接采用；否则以应用登记的制品作为仓库
     * 拼接「:版本号」，登记制品中已有的标签会被替换。
     *
     * @param artifact 应用登记的制品 / 镜像
     * @param version  发布版本号
     * @return 目标镜像坐标
     */
    private String resolveImage(String artifact, String version) {
        String trimmedVersion = StrUtil.trim(version);
        if (trimmedVersion.contains("/") || trimmedVersion.contains(":")) {
            return trimmedVersion;
        }
        String repository = StrUtil.trim(artifact);
        int slash = repository.lastIndexOf('/');
        int colon = repository.lastIndexOf(':');
        if (colon > slash) {
            repository = repository.substring(0, colon);
        }
        return repository + ":" + trimmedVersion;
    }

    /**
     * 开始一个编排阶段：写日志并落库
     *
     * @param logBuilder 日志缓存
     * @param task       部署任务
     * @param stage      阶段名称
     */
    private void beginStage(StringBuilder logBuilder, IotDeployTask task, String stage) {
        appendLog(logBuilder, "【" + stage + "】开始");
        persist(task, logBuilder);
    }

    /**
     * 结束一个编排阶段：写日志并落库
     *
     * @param logBuilder 日志缓存
     * @param task       部署任务
     * @param stage      阶段名称
     * @param detail     阶段结果说明
     */
    private void endStage(StringBuilder logBuilder, IotDeployTask task, String stage, String detail) {
        appendLog(logBuilder, "【" + stage + "】完成" + (StrUtil.isBlank(detail) ? "" : "：" + detail));
        persist(task, logBuilder);
    }

    /**
     * 将当前日志与任务状态落库
     *
     * @param task       部署任务
     * @param logBuilder 日志缓存
     */
    private void persist(IotDeployTask task, StringBuilder logBuilder) {
        task.setDeployLog(logBuilder.toString());
        baseMapper.updateById(task);
    }

    /**
     * 补充任务列表中的应用名称
     *
     * @param records 任务列表
     */
    private void fillAppName(List<IotDeployTaskVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Map<String, String> appNames = appMapper.selectList(Wrappers.<IotDeployApp>lambdaQuery()
                .select(IotDeployApp::getAppCode, IotDeployApp::getAppName))
            .stream()
            .collect(Collectors.toMap(IotDeployApp::getAppCode, IotDeployApp::getAppName, (a, b) -> a));
        records.forEach(record -> record.setAppName(appNames.get(record.getAppCode())));
    }

    /**
     * 构建部署任务查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotDeployTask> buildQueryWrapper(IotDeployTaskBo bo) {
        LambdaQueryWrapper<IotDeployTask> lqw = Wrappers.lambdaQuery();
        lqw.like(StrUtil.isNotBlank(bo.getAppCode()), IotDeployTask::getAppCode, bo.getAppCode());
        lqw.like(StrUtil.isNotBlank(bo.getVersion()), IotDeployTask::getVersion, bo.getVersion());
        lqw.eq(StrUtil.isNotBlank(bo.getTargetEnv()), IotDeployTask::getTargetEnv, bo.getTargetEnv());
        lqw.eq(StrUtil.isNotBlank(bo.getTaskType()), IotDeployTask::getTaskType, bo.getTaskType());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotDeployTask::getStatus, bo.getStatus());
        lqw.orderByDesc(IotDeployTask::getCreateTime);
        return lqw;
    }

    /**
     * 追加一行带时间戳的部署日志
     *
     * @param builder 日志缓存
     * @param message 日志内容
     */
    private void appendLog(StringBuilder builder, String message) {
        builder.append('[').append(LocalDateTime.now().format(TIME_FORMATTER)).append("] ")
            .append(message).append(System.lineSeparator());
    }

    /**
     * 创建部署执行线程池
     *
     * @return 线程池
     */
    private static ThreadPoolExecutor createExecutor() {
        AtomicInteger seq = new AtomicInteger(1);
        return new ThreadPoolExecutor(2, 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(256),
            r -> {
                Thread thread = new Thread(r, "iot-deploy-" + seq.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
