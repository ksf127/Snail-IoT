import service from '@/utils/request';

/**
 * 兼容 iot-ui 的请求实例：
 * - 默认导出 plus-ui 的 axios 实例（响应拦截器已返回后端 body，形状与 iot-ui 一致）
 * - 提供 iot-ui 风格的 download(url, params, method)，返回二进制 Blob 由调用方自行落盘
 */
export default service;

/**
 * 通用下载：用于导出与批量下载等返回文件流的接口。
 * 后端导出接口均以 query string 接收查询条件。
 *
 * @param url    接口地址
 * @param params 查询参数
 * @param method 请求方法，默认 post
 * @returns 文件流 Blob
 */
export async function download(url: string, params: Record<string, any> = {}, method: 'post' | 'get' = 'post'): Promise<Blob> {
  return service({
    url,
    method,
    params,
    responseType: 'blob'
  }) as unknown as Promise<Blob>;
}
