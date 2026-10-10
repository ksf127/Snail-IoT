import request from '@/utils/request';

/**
 * 兼容 iot-ui 的角色下拉接口：查询全部可选角色。
 * 用于 IoT 应用 / 数据集授权时的角色选择。
 */
export function roleOptionSelect() {
  return request({ url: '/system/role/optionselect', method: 'get' });
}
