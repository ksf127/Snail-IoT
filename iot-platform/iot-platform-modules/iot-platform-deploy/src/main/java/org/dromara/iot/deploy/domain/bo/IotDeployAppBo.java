package org.dromara.iot.deploy.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.deploy.domain.IotDeployApp;

import java.io.Serializable;

/**
 * 部署应用业务对象 iot_deploy_app
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotDeployApp.class, reverseConvertGenerate = false)
public class IotDeployAppBo implements Serializable {

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 应用编码
     */
    @NotBlank(message = "应用编码不能为空")
    @Size(max = 64, message = "应用编码长度不能超过 64 个字符")
    private String appCode;

    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 128, message = "应用名称长度不能超过 128 个字符")
    private String appName;

    /**
     * 代码仓库地址
     */
    @Size(max = 255, message = "代码仓库地址长度不能超过 255 个字符")
    private String repoUrl;

    /**
     * 代码分支
     */
    @Size(max = 64, message = "代码分支长度不能超过 64 个字符")
    private String repoBranch;

    /**
     * 构建方式（maven / node）
     */
    private String buildType;

    /**
     * 构建命令（在构建目录内执行，留空则跳过源码构建）
     */
    @Size(max = 4000, message = "构建命令长度不能超过 4000 个字符")
    private String buildCommand;

    /**
     * 构建目录（相对代码根目录，默认 .）
     */
    @Size(max = 255, message = "构建目录长度不能超过 255 个字符")
    private String buildWorkdir;

    /**
     * Dockerfile 路径（相对代码根目录）
     */
    @Size(max = 255, message = "Dockerfile 路径长度不能超过 255 个字符")
    private String dockerfilePath;

    /**
     * 制品 / 镜像名称（不含 tag，构建时传入版本作为 tag）
     */
    @Size(max = 128, message = "制品名称长度不能超过 128 个字符")
    private String artifact;

    /**
     * 构建产物 JAR 路径（相对代码根目录，支持通配符，留空表示不归档）
     */
    @Size(max = 255, message = "JAR 路径长度不能超过 255 个字符")
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
     * 备注
     */
    private String remark;

}
