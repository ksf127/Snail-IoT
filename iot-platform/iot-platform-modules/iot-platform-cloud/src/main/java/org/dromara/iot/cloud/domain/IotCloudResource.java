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
 * 云资源对象 iot_cloud_resource
 * <p>
 * 由各 Provider 从云平台同步而来的纳管资源清单（ECS 实例、Flink 作业等）。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_cloud_resource")
public class IotCloudResource extends BaseEntity {

    /**
     * 资源ID
     */
    @TableId(value = "resource_id", type = IdType.ASSIGN_ID)
    private Long resourceId;

    /**
     * 云账号ID
     */
    private Long accountId;

    /**
     * 云平台（mock/aliyun/flink）
     */
    private String provider;

    /**
     * 资源类型（ecs/flink-job）
     */
    private String resourceType;

    /**
     * 云平台实例ID
     */
    private String instanceId;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 地域ID
     */
    private String regionId;

    /**
     * 状态（原样透出云平台状态）
     */
    private String status;

    /**
     * 规格（ECS InstanceType / Flink 并行度）
     */
    private String spec;

    /**
     * 内网IP
     */
    private String privateIp;

    /**
     * 公网IP
     */
    private String publicIp;

    /**
     * 业务扩展信息（JSON 文本）
     */
    private String bizMeta;

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
