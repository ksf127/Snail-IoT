package org.dromara.iot.cloud.provider.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 纳管资源（由各 Provider 从云平台归一化输出）
 *
 * @author iot team
 */
@Data
public class CloudResourceDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资源类型（见 {@code CloudResourceTypeEnum}）
     */
    private String resourceType;

    /**
     * 云平台实例ID（ECS InstanceId / Flink JobId）
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
     * 业务扩展信息（持久化为 JSON 文本）
     */
    private Map<String, Object> bizMeta;

}
