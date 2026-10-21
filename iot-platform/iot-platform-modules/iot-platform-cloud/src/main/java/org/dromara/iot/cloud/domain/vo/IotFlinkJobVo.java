package org.dromara.iot.cloud.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.cloud.domain.IotFlinkJob;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Flink 作业视图对象 iot_flink_job
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotFlinkJob.class)
public class IotFlinkJobVo implements Serializable {

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
     * 最近同步时间
     */
    private LocalDateTime lastSyncTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
