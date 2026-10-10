package org.dromara.iot.device.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.device.domain.IotDevice;
import org.dromara.iot.device.domain.bo.IotDeviceBo;
import org.dromara.iot.device.domain.vo.IotDeviceVo;

import java.util.Collection;
import java.util.List;

/**
 * 设备 服务层
 *
 * @author iot team
 */
public interface IIotDeviceService {

    /**
     * 查询设备详情
     *
     * @param deviceId 设备ID
     * @return 设备视图对象
     */
    IotDeviceVo queryById(Long deviceId);

    /**
     * 分页查询设备列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<IotDeviceVo> queryPageList(IotDeviceBo bo, PageQuery pageQuery);

    /**
     * 查询设备列表（不分页）
     *
     * @param bo 查询条件
     * @return 设备列表
     */
    List<IotDeviceVo> queryList(IotDeviceBo bo);

    /**
     * 新增设备
     *
     * @param bo 设备业务对象
     * @return 是否成功
     */
    Boolean insertByBo(IotDeviceBo bo);

    /**
     * 修改设备
     *
     * @param bo 设备业务对象
     * @return 是否成功
     */
    Boolean updateByBo(IotDeviceBo bo);

    /**
     * 批量删除设备
     *
     * @param ids     设备ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 按在线状态统计设备数量
     *
     * @param onlineStatus 在线状态（0离线 1在线 2未知），为空表示全部
     * @return 设备数量
     */
    long countByOnlineStatus(String onlineStatus);

    /**
     * 按设备编码查询设备（含密钥等敏感字段，仅限服务端内部校验使用）
     *
     * @param deviceCode 设备编码
     * @return 设备实体，不存在时返回 null
     */
    IotDevice queryByDeviceCode(String deviceCode);

    /**
     * 刷新设备在线状态
     * <p>
     * 上线时同步记录最近上线/最近上报时间，下线时仅更新在线状态。
     *
     * @param deviceId     设备ID
     * @param onlineStatus 在线状态（0离线 1在线 2未知）
     * @return 是否更新成功
     */
    boolean refreshOnline(Long deviceId, String onlineStatus);

}
