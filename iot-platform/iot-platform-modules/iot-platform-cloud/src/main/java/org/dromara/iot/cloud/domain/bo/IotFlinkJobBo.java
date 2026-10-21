package org.dromara.iot.cloud.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.cloud.domain.IotFlinkJob;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Flink 作业业务对象 iot_flink_job
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotFlinkJob.class, reverseConvertGenerate = false)
public class IotFlinkJobBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 作业ID
     */
    private Long jobId;

    /**
     * 云账号ID
     */
    private Long accountId;

    /**
     * 作业名称
     */
    private String jobName;

    /**
     * Flink 作业ID
     */
    private String flinkJobId;

    /**
     * 作业状态
     */
    private String status;

    /**
     * 并行度
     */
    private Integer parallelism;

    /**
     * 启动时间（毫秒时间戳）
     */
    private Long startTime;

    /**
     * 运行时长（毫秒）
     */
    private Long duration;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /**
     * 关键字（匹配作业名称 / 作业ID）
     */
    private String keyword;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
