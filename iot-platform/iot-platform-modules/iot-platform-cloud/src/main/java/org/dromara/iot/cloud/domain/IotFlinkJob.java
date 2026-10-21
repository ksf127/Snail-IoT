package org.dromara.iot.cloud.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * Flink 作业对象 iot_flink_job
 * <p>
 * 由 Flink Provider 从 JobManager REST 同步的作业明细，作为资源清单（iot_cloud_resource）的补充视图。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_flink_job")
public class IotFlinkJob extends BaseEntity {

    /**
     * 作业ID
     */
    @TableId(value = "job_id", type = IdType.ASSIGN_ID)
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
     * Flink 作业ID（JobManager 返回的 jid）
     */
    private String flinkJobId;

    /**
     * 作业状态（RUNNING/FINISHED/FAILED/CANCELED 等）
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
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
