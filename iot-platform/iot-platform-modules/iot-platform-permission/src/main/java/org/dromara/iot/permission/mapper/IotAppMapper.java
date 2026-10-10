package org.dromara.iot.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.permission.domain.IotApp;
import org.dromara.iot.permission.domain.vo.IotAppVo;

/**
 * 应用 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotAppMapper extends BaseMapperPlus<IotApp, IotAppVo> {

}
