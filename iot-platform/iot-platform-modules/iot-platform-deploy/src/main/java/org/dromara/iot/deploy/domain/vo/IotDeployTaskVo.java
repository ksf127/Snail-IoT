package org.dromara.iot.deploy.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.deploy.domain.IotDeployTask;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部署任务视图对象 iot_deploy_task
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotDeployTask.class)
public class IotDeployTaskVo implements Serializable {

    /**
     * 部署任务ID
     */
    private Long taskId;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用名称（由应用登记表补充，仅用于展示）
     */
    private String appName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 目标环境（dev / test / prod）
     */
    private String targetEnv;

    /**
     * 任务类型（deploy发布 build构建）
     */
    private String taskType;

    /**
     * Jenkins 构建号（构建任务成功后可据此下载 JAR）
     */
    private Integer buildNumber;

    /**
     * 状态（0待执行 1执行中 2成功 3失败）
     */
    private String status;

    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;

    /**
     * 执行完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
