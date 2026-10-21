package org.dromara.iot.pay.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.pay.domain.IotPayProduct;
import org.dromara.iot.pay.domain.vo.IotPayProductVo;

/**
 * 支付商品 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotPayProductMapper extends BaseMapperPlus<IotPayProduct, IotPayProductVo> {

}
