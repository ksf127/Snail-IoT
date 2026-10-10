package org.dromara.iot.pay.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.pay.domain.IotPayChannelConfig;
import org.dromara.iot.pay.domain.vo.IotPayChannelConfigVo;

/**
 * 支付渠道配置 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotPayChannelConfigMapper extends BaseMapperPlus<IotPayChannelConfig, IotPayChannelConfigVo> {

}
