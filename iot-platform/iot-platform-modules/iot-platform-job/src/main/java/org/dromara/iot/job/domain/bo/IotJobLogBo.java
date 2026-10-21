package org.dromara.iot.job.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.job.domain.IotJobLog;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务执行日志业务对象 iot_job_log
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotJobLog.class, reverseConvertGenerate = false)
public class IotJobLogBo implements Serializable {

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
    @Size(max = 128, message = "任务名称长度不能超过 128 个字符")
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
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
