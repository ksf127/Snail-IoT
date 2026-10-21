package org.dromara.iot.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 任务信息对象 iot_job_info
 * <p>
 * 一条记录代表一个可调度的任务：既可由内置调度器（LOCAL）按 Cron/固定频率触发，
 * 也可登记为 SnailJob（SNAILJOB）分布式任务的本地视图。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_job_info")
public class IotJobInfo extends BaseEntity {

    /**
     * 任务ID
     */
    @TableId(value = "job_id", type = IdType.ASSIGN_ID)
    private Long jobId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务分组
     */
    private String jobGroup;

    /**
     * 任务类型（LOCAL自建调度 SNAILJOB分布式调度）
     */
    private String jobType;

    /**
     * 执行器名称（对应 JobHandler#name 或 SnailJob @JobExecutor#name）
     */
    private String handlerName;

    /**
     * Cron 表达式（trigger_type=CRON 时生效）
     */
    private String cronExpr;

    /**
     * 触发方式（CRON/FIXED_RATE/MANUAL）
     */
    private String triggerType;

    /**
     * 固定频率间隔（秒，trigger_type=FIXED_RATE 时生效）
     */
    private Integer intervalSeconds;

    /**
     * 任务参数（JSON 文本，透传给执行器）
     */
    private String jobParams;

    /**
     * SnailJob 任务ID（job_type=SNAILJOB 时登记）
     */
    private Long snailJobId;

    /**
     * 状态（0启用 1停用）
     */
    private String status;

    /**
     * 最近触发时间
     */
    private LocalDateTime lastFireTime;

    /**
     * 下次触发时间
     */
    private LocalDateTime nextFireTime;

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
