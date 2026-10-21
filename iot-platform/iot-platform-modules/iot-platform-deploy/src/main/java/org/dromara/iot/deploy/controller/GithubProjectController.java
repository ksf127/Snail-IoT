package org.dromara.iot.deploy.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.deploy.domain.bo.IotGithubRepoBo;
import org.dromara.iot.deploy.domain.vo.IotGithubRepoVo;
import org.dromara.iot.deploy.service.IGithubProjectService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GitHub 项目管理
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/deploy/github")
public class GithubProjectController extends BaseController {

    private final IGithubProjectService githubProjectService;

    /**
     * 分页查询 GitHub 项目
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 项目分页结果
     */
    @SaCheckPermission("iot:deploy:list")
    @GetMapping("/page")
    public R<PageResult<IotGithubRepoVo>> page(IotGithubRepoBo bo, PageQuery pageQuery) {
        return R.ok(githubProjectService.queryPageList(bo, pageQuery));
    }

    /**
     * 从 GitHub 同步仓库列表
     *
     * @return 同步的项目数量
     */
    @SaCheckPermission("iot:deploy:github:sync")
    @Log(title = "GitHub 项目", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/sync")
    public R<Integer> sync() {
        return R.ok(githubProjectService.syncRepos());
    }

    /**
     * 查询项目已授权的账号ID
     *
     * @param repoId GitHub 仓库ID
     * @return 账号ID列表
     */
    @SaCheckPermission("iot:deploy:github:auth")
    @GetMapping("/{repoId}/users")
    public R<List<Long>> users(@PathVariable Long repoId) {
        return R.ok(githubProjectService.queryAuthorizedUserIds(repoId));
    }

    /**
     * 保存项目的授权账号（全量覆盖）
     *
     * @param repoId  GitHub 仓库ID
     * @param userIds 账号ID列表
     * @return 操作结果
     */
    @SaCheckPermission("iot:deploy:github:auth")
    @Log(title = "GitHub 项目", businessType = BusinessType.UPDATE)
    @PutMapping("/{repoId}/users")
    public R<Void> updateUsers(@PathVariable Long repoId, @RequestBody List<Long> userIds) {
        githubProjectService.updateAuthorizedUsers(repoId, userIds);
        return R.ok();
    }

}
