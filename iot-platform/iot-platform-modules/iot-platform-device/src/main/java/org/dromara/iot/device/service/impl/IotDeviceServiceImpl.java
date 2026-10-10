package org.dromara.iot.device.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.common.enums.DeviceOnlineStatusEnum;
import org.dromara.iot.device.domain.IotDevice;
import org.dromara.iot.device.domain.bo.IotDeviceBo;
import org.dromara.iot.device.domain.vo.IotDeviceVo;
import org.dromara.iot.device.mapper.IotDeviceMapper;
import org.dromara.iot.device.service.IIotDeviceService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 设备 服务层实现
 *
 * @author iot team
 */
@RequiredArgsConstructor
@Service
public class IotDeviceServiceImpl implements IIotDeviceService {

    private final IotDeviceMapper baseMapper;

    /**
     * 查询设备详情
     *
     * @param deviceId 设备ID
     * @return 设备视图对象
     */
    @Override
    public IotDeviceVo queryById(Long deviceId) {
        return baseMapper.selectVoById(deviceId);
    }

    /**
     * 分页查询设备列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<IotDeviceVo> queryPageList(IotDeviceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotDevice> lqw = buildQueryWrapper(bo);
        Page<IotDeviceVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询设备列表（不分页）
     *
     * @param bo 查询条件
     * @return 设备列表
     */
    @Override
    public List<IotDeviceVo> queryList(IotDeviceBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    /**
     * 新增设备
     *
     * @param bo 设备业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(IotDeviceBo bo) {
        IotDevice add = MapstructUtils.convert(bo, IotDevice.class);
        return baseMapper.insert(add) > 0;
    }

    /**
     * 修改设备
     *
     * @param bo 设备业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(IotDeviceBo bo) {
        IotDevice update = MapstructUtils.convert(bo, IotDevice.class);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 批量删除设备
     *
     * @param ids     设备ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // TODO 业务校验：在线设备不允许删除
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 按在线状态统计设备数量
     *
     * @param onlineStatus 在线状态（0离线 1在线 2未知），为空表示全部
     * @return 设备数量
     */
    @Override
    public long countByOnlineStatus(String onlineStatus) {
        LambdaQueryWrapper<IotDevice> lqw = Wrappers.lambdaQuery();
        lqw.eq(StrUtil.isNotBlank(onlineStatus), IotDevice::getOnlineStatus, onlineStatus);
        return baseMapper.selectCount(lqw);
    }

    /**
     * 按设备编码查询设备（含密钥等敏感字段，仅限服务端内部校验使用）
     *
     * @param deviceCode 设备编码
     * @return 设备实体，不存在时返回 null
     */
    @Override
    public IotDevice queryByDeviceCode(String deviceCode) {
        if (StrUtil.isBlank(deviceCode)) {
            return null;
        }
        return baseMapper.selectOne(Wrappers.<IotDevice>lambdaQuery()
            .eq(IotDevice::getDeviceCode, deviceCode).last("limit 1"));
    }

    /**
     * 刷新设备在线状态
     * <p>
     * 上线时同步记录最近上线/最近上报时间，下线时仅更新在线状态。
     *
     * @param deviceId     设备ID
     * @param onlineStatus 在线状态（0离线 1在线 2未知）
     * @return 是否更新成功
     */
    @Override
    public boolean refreshOnline(Long deviceId, String onlineStatus) {
        if (deviceId == null || StrUtil.isBlank(onlineStatus)) {
            return false;
        }
        boolean online = DeviceOnlineStatusEnum.ONLINE.getCode().equals(onlineStatus);
        Date now = new Date();
        LambdaUpdateWrapper<IotDevice> luw = Wrappers.lambdaUpdate();
        luw.eq(IotDevice::getDeviceId, deviceId)
            .set(IotDevice::getOnlineStatus, onlineStatus)
            .set(online, IotDevice::getLastOnlineTime, now)
            .set(online, IotDevice::getLastReportTime, now);
        return baseMapper.update(null, luw) > 0;
    }

    /**
     * 构建设备查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotDevice> buildQueryWrapper(IotDeviceBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IotDevice> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getProductId() != null, IotDevice::getProductId, bo.getProductId());
        lqw.like(StrUtil.isNotBlank(bo.getDeviceCode()), IotDevice::getDeviceCode, bo.getDeviceCode());
        lqw.like(StrUtil.isNotBlank(bo.getDeviceName()), IotDevice::getDeviceName, bo.getDeviceName());
        lqw.eq(StrUtil.isNotBlank(bo.getProtocol()), IotDevice::getProtocol, bo.getProtocol());
        lqw.eq(StrUtil.isNotBlank(bo.getOnlineStatus()), IotDevice::getOnlineStatus, bo.getOnlineStatus());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotDevice::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            IotDevice::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(IotDevice::getCreateTime);
        return lqw;
    }

}
