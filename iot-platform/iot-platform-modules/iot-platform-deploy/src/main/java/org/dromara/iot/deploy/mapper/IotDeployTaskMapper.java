package org.dromara.iot.deploy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.deploy.domain.IotDeployTask;
import org.dromara.iot.deploy.domain.vo.IotDeployTaskVo;

/**
 * 部署任务 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotDeployTaskMapper extends BaseMapperPlus<IotDeployTask, IotDeployTaskVo> {

}
