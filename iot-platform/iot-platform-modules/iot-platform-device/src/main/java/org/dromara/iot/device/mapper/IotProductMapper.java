package org.dromara.iot.device.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.device.domain.IotProduct;
import org.dromara.iot.device.domain.vo.IotProductVo;

/**
 * 产品 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotProductMapper extends BaseMapperPlus<IotProduct, IotProductVo> {

}
