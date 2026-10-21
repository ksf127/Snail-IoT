package org.dromara.iot.deploy.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 部署环境对象 iot_deploy_env
 * <p>
 * 登记自动化部署的目标环境及其 Kubernetes 命名空间、入口端口等隔离信息，
 * 供发布时选择目标环境并与部署脚本保持一致。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_deploy_env")
public class IotDeployEnv extends BaseEntity {

    /**
     * 环境ID
     */
    @TableId(value = "env_id", type = IdType.ASSIGN_ID)
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
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
