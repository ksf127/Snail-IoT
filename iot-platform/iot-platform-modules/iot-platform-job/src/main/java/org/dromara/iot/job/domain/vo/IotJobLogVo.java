package org.dromara.iot.job.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.job.domain.IotJobLog;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务执行日志视图对象 iot_job_log
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotJobLog.class)
public class IotJobLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 任务ID
     */
    private Long jobId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 触发方式（CRON/FIXED_RATE/MANUAL）
     */
    private String triggerType;

    /**
     * 执行器名称
     */
    private String handlerName;

    /**
     * 执行状态（SUCCESS/FAIL）
     */
    private String status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 执行结果
     */
    private String result;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
