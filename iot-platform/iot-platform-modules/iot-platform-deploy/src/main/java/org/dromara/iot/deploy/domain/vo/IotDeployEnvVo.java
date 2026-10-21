package org.dromara.iot.deploy.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.deploy.domain.IotDeployEnv;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部署环境视图对象 iot_deploy_env
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotDeployEnv.class)
public class IotDeployEnvVo implements Serializable {

    /**
     * 环境ID
     */
    private Long envId;

    /**
     * 环境编码（dev / test / prod）
     */
    private String envCode;

    /**
     * 环境名称
     */
    private String envName;

    /**
     * Kubernetes 命名空间
     */
    private String k8sNamespace;

    /**
     * 前端服务 NodePort 端口
     */
    private Integer webNodePort;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 状态（0启用 1停用）
     */
    private String status;

    /**
     * 排序号（越小越靠前）
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
