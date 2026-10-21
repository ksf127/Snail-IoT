package org.dromara.iot.deploy.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.deploy.domain.IotGithubRepo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * GitHub 项目视图对象 iot_github_repo
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotGithubRepo.class)
public class IotGithubRepoVo implements Serializable {

    /**
     * GitHub 仓库ID
     */
    private Long repoId;

    /**
     * 仓库所有者登录名
     */
    private String ownerLogin;

    /**
     * 仓库名称
     */
    private String repoName;

    /**
     * 仓库全名（owner/name）
     */
    private String fullName;

    /**
     * 仓库页面地址
     */
    private String htmlUrl;

    /**
     * 仓库克隆地址
     */
    private String cloneUrl;

    /**
     * 仓库描述
     */
    private String description;

    /**
     * 主要开发语言
     */
    private String language;

    /**
     * 默认分支
     */
    private String defaultBranch;

    /**
     * 是否私有（0公开 1私有）
     */
    private String privateFlag;

    /**
     * 是否已归档（0否 1是）
     */
    private String archived;

    /**
     * 最近推送时间
     */
    private LocalDateTime pushedAt;

    /**
     * 最近同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 已授权账号数量
     */
    private Long userCount;

}
