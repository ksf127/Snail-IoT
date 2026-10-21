package org.dromara.iot.deploy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.deploy.domain.IotDeployEnv;
import org.dromara.iot.deploy.domain.vo.IotDeployEnvVo;

/**
 * 部署环境 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotDeployEnvMapper extends BaseMapperPlus<IotDeployEnv, IotDeployEnvVo> {

}
