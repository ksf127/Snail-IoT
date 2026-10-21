package org.dromara.iot.deploy.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * GitHub 项目授权对象 iot_github_repo_user
 * <p>
 * 一条记录表示某个系统账号可见某个 GitHub 项目。
 *
 * @author iot team
 */
@Data
@TableName("iot_github_repo_user")
public class IotGithubRepoUser {

    /**
     * GitHub 仓库ID
     */
    private Long repoId;

    /**
     * 系统用户ID
     */
    private Long userId;

    /**
     * 授权时间
     */
    private LocalDateTime createTime;

}
