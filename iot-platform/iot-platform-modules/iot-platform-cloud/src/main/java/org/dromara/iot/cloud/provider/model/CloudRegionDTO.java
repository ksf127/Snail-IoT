package org.dromara.iot.cloud.provider.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 云平台地域
 *
 * @author iot team
 */
@Data
public class CloudRegionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地域ID，如 cn-hangzhou
     */
    private String regionId;

    /**
     * 地域名称
     */
    private String regionName;

}
