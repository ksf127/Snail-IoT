package org.dromara.iot.deploy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.deploy.domain.IotDeployApp;
import org.dromara.iot.deploy.domain.vo.IotDeployAppVo;

/**
 * 部署应用 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotDeployAppMapper extends BaseMapperPlus<IotDeployApp, IotDeployAppVo> {

}
