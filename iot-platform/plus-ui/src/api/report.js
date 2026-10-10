import request, { download } from '@/utils/iotRequest'

/**
 * 支持的批量业务类型。
 *
 * @returns {Promise} 业务类型列表
 */
export function listBizTypes() {
  return request({ url: '/iot/report/biz-types', method: 'get' })
}

/**
 * 设备数据统计报表。
 *
 * @param {object} query 统计条件
 * @returns {Promise} 统计结果
 */
export function deviceDataReport(query) {
  return request({ url: '/iot/report/device-data', method: 'get', params: query })
}

/**
 * 批量上传（Excel 导入）。
 *
 * @param {string} bizType 业务类型
 * @param {File}   file    上传文件
 * @returns {Promise} 导入结果摘要
 */
export function batchUpload(bizType, file) {
  const formData = new FormData()
  formData.append('bizType', bizType)
  formData.append('file', file)
  return request({
    url: '/iot/report/batch-upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 异步批量上传（大文件导入，立即返回 taskId）。
 *
 * @param {string} bizType 业务类型
 * @param {File}   file    上传文件
 * @returns {Promise} 任务摘要
 */
export function batchUploadAsync(bizType, file) {
  const formData = new FormData()
  formData.append('bizType', bizType)
  formData.append('file', file)
  return request({
    url: '/iot/report/batch-upload-async',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 初始化分片上传会话。
 *
 * @param {object} params 会话参数（bizType / fileName / totalChunks / fileSize）
 * @returns {Promise} 会话信息（含 uploadId）
 */
export function initChunkUpload(params) {
  return request({ url: '/iot/report/chunk/init', method: 'post', params })
}

/**
 * 上传单个分片。
 *
 * @param {string} uploadId   上传会话ID
 * @param {number} chunkIndex 分片序号
 * @param {Blob}   blob       分片内容
 * @param {string} fileName   原始文件名
 * @returns {Promise} 上传进度
 */
export function uploadChunkPart(uploadId, chunkIndex, blob, fileName) {
  const formData = new FormData()
  formData.append('file', blob, fileName || `chunk-${chunkIndex}`)
  return request({
    url: '/iot/report/chunk/part',
    method: 'post',
    params: { uploadId, chunkIndex },
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 合并分片并异步导入。
 *
 * @param {string} uploadId 上传会话ID
 * @returns {Promise} 任务摘要
 */
export function mergeChunks(uploadId) {
  return request({ url: '/iot/report/chunk/merge', method: 'post', params: { uploadId } })
}

const CHUNK_SIZE = 5 * 1024 * 1024

/**
 * 大文件分片上传并触发后台导入。
 *
 * @param {string}   bizType    业务类型
 * @param {File}     file       待上传文件
 * @param {Function} onProgress 进度回调（0-100）
 * @returns {Promise} 导入任务摘要
 */
export async function chunkUploadFile(bizType, file, onProgress) {
  const totalChunks = Math.max(1, Math.ceil(file.size / CHUNK_SIZE))
  const initRes = await initChunkUpload({
    bizType,
    fileName: file.name,
    totalChunks,
    fileSize: file.size
  })
  const uploadId = initRes.data.uploadId
  for (let index = 0; index < totalChunks; index++) {
    const blob = file.slice(index * CHUNK_SIZE, (index + 1) * CHUNK_SIZE)
    await uploadChunkPart(uploadId, index, blob, file.name)
    if (onProgress) {
      onProgress(Math.round(((index + 1) / totalChunks) * 100))
    }
  }
  return mergeChunks(uploadId)
}

/**
 * 查询批量导入任务进度与结果。
 *
 * @param {string} taskId 任务ID
 * @returns {Promise} 任务进度与结果
 */
export function getUploadResult(taskId) {
  return request({ url: `/iot/report/upload-result/${taskId}`, method: 'get' })
}

/**
 * 批量下载（按条件打包导出）。
 *
 * @param {string} bizType 业务类型
 * @param {object} query   查询条件
 * @returns {Promise<Blob>} 文件流
 */
export function batchDownload(bizType, query = {}) {
  return download('/iot/report/batch-download', { bizType, ...query })
}

/**
 * 导出设备数据报表。
 *
 * @param {object} query 查询条件
 * @returns {Promise<Blob>} 文件流
 */
export function exportDeviceData(query) {
  return download('/iot/device/export', query)
}
