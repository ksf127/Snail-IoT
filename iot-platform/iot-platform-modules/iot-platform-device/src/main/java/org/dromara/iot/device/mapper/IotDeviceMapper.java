package org.dromara.iot.device.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.device.domain.IotDevice;
import org.dromara.iot.device.domain.vo.IotDeviceVo;

/**
 * 设备 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotDeviceMapper extends BaseMapperPlus<IotDevice, IotDeviceVo> {

}
