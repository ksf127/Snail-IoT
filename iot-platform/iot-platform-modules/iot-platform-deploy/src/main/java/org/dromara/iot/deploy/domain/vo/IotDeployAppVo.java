package org.dromara.iot.deploy.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.deploy.domain.IotDeployApp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部署应用视图对象 iot_deploy_app
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotDeployApp.class)
public class IotDeployAppVo implements Serializable {

    /**
     * 应用ID
     */
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
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
