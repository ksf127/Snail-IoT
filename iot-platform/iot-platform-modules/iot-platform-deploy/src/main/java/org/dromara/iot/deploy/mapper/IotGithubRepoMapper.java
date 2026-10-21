package org.dromara.iot.deploy.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.deploy.domain.IotGithubRepo;
import org.dromara.iot.deploy.domain.vo.IotGithubRepoVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * GitHub 项目 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotGithubRepoMapper extends BaseMapperPlus<IotGithubRepo, IotGithubRepoVo> {
}
