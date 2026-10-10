package org.dromara.iot.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.iot.report.service.IReportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 报表与批量上传下载
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/report")
public class ReportController {

    private final IReportService reportService;

    /**
     * 支持的批量业务类型
     *
     * @return 业务类型列表
     */
    @SaCheckPermission("iot:report:list")
    @GetMapping("/biz-types")
    public R<List<String>> bizTypes() {
        return R.ok(reportService.listBizTypes());
    }

    /**
     * 设备数据统计报表
     *
     * @param query 统计条件
     * @return 统计结果
     */
    @SaCheckPermission("iot:report:list")
    @GetMapping("/device-data")
    public R<Map<String, Object>> deviceDataReport(@RequestParam Map<String, Object> query) {
        return R.ok(reportService.deviceDataReport(query));
    }

    /**
     * 批量上传（Excel 导入）
     *
     * @param bizType 业务类型
     * @param file    上传文件
     * @return 导入结果摘要
     * @throws IOException 读取文件失败
     */
    @SaCheckPermission("iot:report:upload")
    @Log(title = "批量上传", businessType = BusinessType.IMPORT)
    @PostMapping("/batch-upload")
    public R<Map<String, Object>> batchUpload(@RequestParam String bizType,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        return R.ok(reportService.batchUpload(bizType, file.getOriginalFilename(), file.getBytes()));
    }

    /**
     * 查询批量导入任务进度与结果（缓存）
     *
     * @param taskId 任务ID
     * @return 任务进度与结果
     */
    @SaCheckPermission("iot:report:upload")
    @GetMapping("/upload-result/{taskId}")
    public R<Map<String, Object>> uploadResult(@PathVariable String taskId) {
        return R.ok(reportService.getUploadResult(taskId));
    }

    /**
     * 异步批量上传（大文件导入，立即返回 taskId）
     *
     * @param bizType 业务类型
     * @param file    上传文件
     * @return 任务摘要（含 taskId）
     * @throws IOException 读取文件失败
     */
    @SaCheckPermission("iot:report:upload")
    @Log(title = "异步批量上传", businessType = BusinessType.IMPORT)
    @PostMapping("/batch-upload-async")
    public R<Map<String, Object>> batchUploadAsync(@RequestParam String bizType,
                                                   @RequestParam("file") MultipartFile file) throws IOException {
        return R.ok(reportService.batchUploadAsync(bizType, file.getOriginalFilename(), file.getBytes()));
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
    @SaCheckPermission("iot:report:upload")
    @PostMapping("/chunk/init")
    public R<Map<String, Object>> chunkInit(@RequestParam String bizType,
                                            @RequestParam String fileName,
                                            @RequestParam Integer totalChunks,
                                            @RequestParam(required = false, defaultValue = "0") Long fileSize) {
        return R.ok(reportService.initChunkUpload(bizType, fileName, totalChunks, fileSize));
    }

    /**
     * 上传单个分片
     *
     * @param uploadId   上传会话ID
     * @param chunkIndex 分片序号（从 0 开始）
     * @param file       分片文件
     * @return 上传进度
     * @throws IOException 读取分片失败
     */
    @SaCheckPermission("iot:report:upload")
    @PostMapping("/chunk/part")
    public R<Map<String, Object>> chunkPart(@RequestParam String uploadId,
                                            @RequestParam Integer chunkIndex,
                                            @RequestParam("file") MultipartFile file) throws IOException {
        return R.ok(reportService.uploadChunk(uploadId, chunkIndex, file.getBytes()));
    }

    /**
     * 合并分片并异步导入
     *
     * @param uploadId 上传会话ID
     * @return 任务摘要（含 taskId）
     */
    @SaCheckPermission("iot:report:upload")
    @Log(title = "分片合并导入", businessType = BusinessType.IMPORT)
    @PostMapping("/chunk/merge")
    public R<Map<String, Object>> chunkMerge(@RequestParam String uploadId) {
        return R.ok(reportService.mergeChunks(uploadId));
    }

    /**
     * 批量下载（按条件打包导出）
     *
     * @param bizType  业务类型
     * @param query    查询条件
     * @param response HTTP 响应
     */
    @SaCheckPermission("iot:report:download")
    @Log(title = "批量下载", businessType = BusinessType.EXPORT)
    @PostMapping("/batch-download")
    public void batchDownload(@RequestParam String bizType, @RequestParam Map<String, Object> query,
                              HttpServletResponse response) {
        reportService.batchDownload(bizType, query, response);
    }

}
