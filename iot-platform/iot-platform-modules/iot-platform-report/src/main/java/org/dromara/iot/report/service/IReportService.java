package org.dromara.iot.report.service;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 报表 服务层
 * <p>
 * 负责统计报表生成，以及批量上传（Excel 导入）与批量下载（ZIP 打包导出）。
 *
 * @author iot team
 */
public interface IReportService {

    /**
     * 设备数据统计报表
     *
     * @param query 统计条件（时间范围、产品、设备等）
     * @return 统计结果
     */
    Map<String, Object> deviceDataReport(Map<String, Object> query);

    /**
     * 批量上传（Excel 导入设备数据）
     * <p>
     * 导入进度与结果会写入 Redis 缓存，返回摘要中包含 taskId，可通过 {@link #getUploadResult} 查询。
     *
     * @param bizType  业务类型（如 device / product）
     * @param fileName 文件名
     * @param content  文件二进制内容
     * @return 导入结果摘要（含 taskId）
     */
    Map<String, Object> batchUpload(String bizType, String fileName, byte[] content);

    /**
     * 异步批量上传（大文件导入）
     * <p>
     * 立即返回 RUNNING 状态与 taskId，由后台线程解析入库，进度可通过 {@link #getUploadResult} 轮询。
     *
     * @param bizType  业务类型
     * @param fileName 文件名
     * @param content  文件二进制内容
     * @return 任务摘要（含 taskId）
     */
    Map<String, Object> batchUploadAsync(String bizType, String fileName, byte[] content);

    /**
     * 初始化分片上传会话
     *
     * @param bizType     业务类型
     * @param fileName    文件名
     * @param totalChunks 分片总数
     * @param fileSize    文件总字节数
     * @return 会话信息（含 uploadId）
     */
    Map<String, Object> initChunkUpload(String bizType, String fileName, int totalChunks, long fileSize);

    /**
     * 上传单个分片
     *
     * @param uploadId   上传会话ID
     * @param chunkIndex 分片序号（从 0 开始）
     * @param content    分片内容
     * @return 上传进度
     */
    Map<String, Object> uploadChunk(String uploadId, int chunkIndex, byte[] content);

    /**
     * 合并分片并异步导入
     *
     * @param uploadId 上传会话ID
     * @return 任务摘要（含 taskId）
     */
    Map<String, Object> mergeChunks(String uploadId);

    /**
     * 查询批量导入任务进度与结果（缓存）
     *
     * @param taskId 任务ID
     * @return 任务进度与结果
     */
    Map<String, Object> getUploadResult(String taskId);

    /**
     * 批量下载（按查询条件打包导出）
     *
     * @param bizType  业务类型
     * @param query    查询条件
     * @param response HTTP 响应
     */
    void batchDownload(String bizType, Map<String, Object> query, HttpServletResponse response);

    /**
     * 查询支持的批量业务类型
     *
     * @return 业务类型列表
     */
    List<String> listBizTypes();

}
