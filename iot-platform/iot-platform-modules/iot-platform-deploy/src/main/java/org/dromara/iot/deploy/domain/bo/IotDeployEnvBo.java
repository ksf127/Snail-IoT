package org.dromara.iot.deploy.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.deploy.domain.IotDeployEnv;

import java.io.Serializable;

/**
 * 部署环境业务对象 iot_deploy_env
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotDeployEnv.class, reverseConvertGenerate = false)
public class IotDeployEnvBo implements Serializable {

    /**
     * 环境ID
     */
    private Long envId;

    /**
     * 环境编码
     */
    @NotBlank(message = "环境编码不能为空")
    @Size(max = 32, message = "环境编码长度不能超过 32 个字符")
    private String envCode;

    /**
     * 环境名称
     */
    @NotBlank(message = "环境名称不能为空")
    @Size(max = 64, message = "环境名称长度不能超过 64 个字符")
    private String envName;

    /**
     * Kubernetes 命名空间
     */
    @NotBlank(message = "Kubernetes 命名空间不能为空")
    @Size(max = 64, message = "Kubernetes 命名空间长度不能超过 64 个字符")
    private String k8sNamespace;

    /**
     * 前端服务 NodePort 端口
     */
    private Integer webNodePort;

    /**
     * 集群名称
     */
    @Size(max = 64, message = "集群名称长度不能超过 64 个字符")
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

}
