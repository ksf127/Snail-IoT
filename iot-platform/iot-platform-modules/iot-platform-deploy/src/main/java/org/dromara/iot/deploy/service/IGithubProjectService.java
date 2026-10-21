package org.dromara.iot.deploy.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.deploy.domain.bo.IotGithubRepoBo;
import org.dromara.iot.deploy.domain.vo.IotGithubRepoVo;

import java.util.List;

/**
 * GitHub 项目管理 服务层
 *
 * @author iot team
 */
public interface IGithubProjectService {

    /**
     * 分页查询 GitHub 项目
     * <p>
     * 超级管理员可查看全部项目，其他账号仅能查看已授权给自己的项目。
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 项目分页结果
     */
    PageResult<IotGithubRepoVo> queryPageList(IotGithubRepoBo bo, PageQuery pageQuery);

    /**
     * 从 GitHub 公开 API 同步仓库列表
     *
     * @return 同步的项目数量
     */
    int syncRepos();

    /**
     * 查询项目已授权的账号ID
     *
     * @param repoId GitHub 仓库ID
     * @return 账号ID列表
     */
    List<Long> queryAuthorizedUserIds(Long repoId);

    /**
     * 保存项目的授权账号（全量覆盖）
     *
     * @param repoId  GitHub 仓库ID
     * @param userIds 账号ID列表
     */
    void updateAuthorizedUsers(Long repoId, List<Long> userIds);

}
