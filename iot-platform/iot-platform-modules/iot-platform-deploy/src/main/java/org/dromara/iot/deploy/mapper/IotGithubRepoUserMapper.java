package org.dromara.iot.deploy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.iot.deploy.domain.IotGithubRepoUser;

/**
 * GitHub 项目授权 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotGithubRepoUserMapper extends BaseMapper<IotGithubRepoUser> {
}
