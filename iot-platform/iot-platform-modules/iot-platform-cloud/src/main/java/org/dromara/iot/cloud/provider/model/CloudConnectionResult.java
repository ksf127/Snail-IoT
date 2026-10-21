package org.dromara.iot.cloud.provider.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 云平台连通性检测结果
 *
 * @author iot team
 */
@Data
public class CloudConnectionResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否连通
     */
    private boolean success;

    /**
     * 结果说明
     */
    private String message;

    /**
     * 耗时（毫秒）
     */
    private long costMs;

}
