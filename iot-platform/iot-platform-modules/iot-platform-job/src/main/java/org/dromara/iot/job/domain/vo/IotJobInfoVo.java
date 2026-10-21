package org.dromara.iot.job.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.job.domain.IotJobInfo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务信息视图对象 iot_job_info
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotJobInfo.class)
public class IotJobInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
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
     * 任务类型（LOCAL/SNAILJOB）
     */
    private String jobType;

    /**
     * 执行器名称
     */
    private String handlerName;

    /**
     * Cron 表达式
     */
    private String cronExpr;

    /**
     * 触发方式（CRON/FIXED_RATE/MANUAL）
     */
    private String triggerType;

    /**
     * 固定频率间隔（秒）
     */
    private Integer intervalSeconds;

    /**
     * 任务参数（JSON 文本）
     */
    private String jobParams;

    /**
     * SnailJob 任务ID
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
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
