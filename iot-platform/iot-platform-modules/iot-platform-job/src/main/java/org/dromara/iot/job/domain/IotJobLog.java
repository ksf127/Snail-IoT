package org.dromara.iot.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 任务执行日志对象 iot_job_log
 * <p>
 * 每次调度触发（自动或手动）都会落一条记录，便于排查与统计。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_job_log")
public class IotJobLog extends BaseEntity {

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    private Long logId;

    /**
     * 任务ID
     */
    private Long jobId;

    /**
     * 任务名称（快照，任务删除后仍可追溯）
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
     * 执行状态（SUCCESS成功 FAIL失败）
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

}
