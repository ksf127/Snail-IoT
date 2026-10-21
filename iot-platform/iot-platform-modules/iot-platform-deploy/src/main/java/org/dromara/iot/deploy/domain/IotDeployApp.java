package org.dromara.iot.deploy.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 部署应用对象 iot_deploy_app
 * <p>
 * 登记参与自动化部署的应用及其代码仓库、构建方式与制品镜像，供发布时选择应用编码。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_deploy_app")
public class IotDeployApp extends BaseEntity {

    /**
     * 应用ID
     */
    @TableId(value = "app_id", type = IdType.ASSIGN_ID)
    private Long appId;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 代码仓库地址
     */
    private String repoUrl;

    /**
     * 代码分支
     */
    private String repoBranch;

    /**
     * 构建方式（maven / node）
     */
    private String buildType;

    /**
     * 构建命令（在构建目录内执行，留空则跳过源码构建）
     */
    private String buildCommand;

    /**
     * 构建目录（相对代码根目录，默认 .）
     */
    private String buildWorkdir;

    /**
     * Dockerfile 路径（相对代码根目录）
     */
    private String dockerfilePath;

    /**
     * 制品 / 镜像名称（不含 tag，构建时传入版本作为 tag）
     */
    private String artifact;

    /**
     * 构建产物 JAR 路径（相对代码根目录，支持通配符，留空表示不归档）
     */
    private String jarPath;

    /**
     * Kubernetes 工作负载名称（Deployment，如 iot-server）
     */
    private String k8sWorkload;

    /**
     * Kubernetes 容器名称（如 iot-server，留空时取 Deployment 首个容器）
     */
    private String k8sContainer;

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
