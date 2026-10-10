package org.dromara.iot.permission.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应用-角色授权关系 iot_app_role
 *
 * @author iot team
 */
@Data
@TableName("iot_app_role")
public class IotAppRole implements Serializable {

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 角色ID
     */
    private Long roleId;

}
