package org.dromara.iot.cloud.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.cloud.domain.IotCloudAccount;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云账号视图对象 iot_cloud_account
 * <p>
 * 出于安全考虑，不向前端返回 {@code accessSecret}。
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotCloudAccount.class)
public class IotCloudAccountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 云账号ID
     */
    private Long accountId;

    /**
     * 账号名称
     */
    private String accountName;

    /**
     * 云平台
     */
    private String provider;

    /**
     * 默认地域ID
     */
    private String regionId;

    /**
     * 访问密钥ID
     */
    private String accessKey;

    /**
     * 接入端点
     */
    private String endpoint;

    /**
     * 状态（0启用 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
