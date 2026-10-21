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
 * 部署任务对象 iot_deploy_task
 * <p>
 * 记录一次应用制品发布编排的任务信息、状态与执行日志。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_deploy_task")
public class IotDeployTask extends BaseEntity {

    /**
     * 部署任务ID
     */
    @TableId(value = "task_id", type = IdType.ASSIGN_ID)
    private Long taskId;

    /**
     * 应用编码
     */
    private String appCode;

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
     * 部署日志（含各阶段时间戳）
     */
    private String deployLog;

    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;

    /**
     * 执行完成时间
     */
    private LocalDateTime finishTime;

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
