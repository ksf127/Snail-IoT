package org.dromara.iot.pay.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.pay.domain.IotPayOrder;
import org.dromara.iot.pay.domain.vo.IotPayOrderVo;

/**
 * 支付订单 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotPayOrderMapper extends BaseMapperPlus<IotPayOrder, IotPayOrderVo> {

}
