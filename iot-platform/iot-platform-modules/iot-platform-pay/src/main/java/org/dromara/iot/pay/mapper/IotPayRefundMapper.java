package org.dromara.iot.pay.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.pay.domain.IotPayRefund;
import org.dromara.iot.pay.domain.vo.IotPayRefundVo;

/**
 * 支付退款单 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotPayRefundMapper extends BaseMapperPlus<IotPayRefund, IotPayRefundVo> {

}
