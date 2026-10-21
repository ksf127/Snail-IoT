package org.dromara.iot.cloud.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.cloud.domain.IotCloudAccount;
import org.dromara.iot.cloud.domain.vo.IotCloudAccountVo;

/**
 * 云账号 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotCloudAccountMapper extends BaseMapperPlus<IotCloudAccount, IotCloudAccountVo> {

}
