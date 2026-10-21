package org.dromara.iot.deploy.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * GitHub 项目查询业务对象
 *
 * @author iot team
 */
@Data
public class IotGithubRepoBo implements Serializable {

    /**
     * 关键词（匹配仓库全名或描述）
     */
    private String keyword;

    /**
     * 开发语言
     */
    private String language;

}
