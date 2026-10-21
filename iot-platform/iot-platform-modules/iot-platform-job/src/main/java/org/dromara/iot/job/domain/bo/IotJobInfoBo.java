package org.dromara.iot.job.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.job.domain.IotJobInfo;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务信息业务对象 iot_job_info
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotJobInfo.class, reverseConvertGenerate = false)
public class IotJobInfoBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long jobId;

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 128, message = "任务名称长度不能超过 128 个字符")
    private String jobName;

    /**
     * 任务分组
     */
    @Size(max = 64, message = "任务分组长度不能超过 64 个字符")
    private String jobGroup;

    /**
     * 任务类型（LOCAL/SNAILJOB）
     */
    @NotBlank(message = "任务类型不能为空")
    private String jobType;

    /**
     * 执行器名称
     */
    @NotBlank(message = "执行器不能为空")
    @Size(max = 128, message = "执行器名称长度不能超过 128 个字符")
    private String handlerName;

    /**
     * Cron 表达式
     */
    @Size(max = 128, message = "Cron 表达式长度不能超过 128 个字符")
    private String cronExpr;

    /**
     * 触发方式（CRON/FIXED_RATE/MANUAL）
     */
    @NotBlank(message = "触发方式不能为空")
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
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
