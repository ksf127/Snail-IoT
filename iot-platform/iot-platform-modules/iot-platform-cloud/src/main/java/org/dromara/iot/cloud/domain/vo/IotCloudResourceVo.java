package org.dromara.iot.cloud.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.cloud.domain.IotCloudResource;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云资源视图对象 iot_cloud_resource
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotCloudResource.class)
public class IotCloudResourceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 云账号ID
     */
    private Long accountId;

    /**
     * 云平台
     */
    private String provider;

    /**
     * 资源类型
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
     * 状态
     */
    private String status;

    /**
     * 规格
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
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
