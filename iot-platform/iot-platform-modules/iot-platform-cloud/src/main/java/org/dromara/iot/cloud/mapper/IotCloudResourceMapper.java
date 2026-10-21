package org.dromara.iot.cloud.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.cloud.domain.IotCloudResource;
import org.dromara.iot.cloud.domain.vo.IotCloudResourceVo;

/**
 * 云资源 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotCloudResourceMapper extends BaseMapperPlus<IotCloudResource, IotCloudResourceVo> {

}
