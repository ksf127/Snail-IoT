package org.dromara.iot.deploy.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * GitHub 项目对象 iot_github_repo
 * <p>
 * 从 GitHub 公开 API 同步的仓库信息，repo_id 直接使用 GitHub 仓库ID，便于重复同步时幂等更新。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_github_repo")
public class IotGithubRepo extends BaseEntity {

    /**
     * GitHub 仓库ID
     */
    @TableId(value = "repo_id", type = IdType.INPUT)
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
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

}
