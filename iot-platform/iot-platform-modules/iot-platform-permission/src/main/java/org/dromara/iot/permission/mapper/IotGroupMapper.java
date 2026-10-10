package org.dromara.iot.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.permission.domain.IotGroup;
import org.dromara.iot.permission.domain.vo.IotGroupVo;

/**
 * 数据分组 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotGroupMapper extends BaseMapperPlus<IotGroup, IotGroupVo> {

}
