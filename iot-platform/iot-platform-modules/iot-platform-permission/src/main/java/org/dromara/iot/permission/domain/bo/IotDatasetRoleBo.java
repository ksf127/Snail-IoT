package org.dromara.iot.permission.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据集数据权限业务对象（数据集对角色开放的数据范围）
 *
 * @author iot team
 */
@Data
public class IotDatasetRoleBo implements Serializable {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）
     */
    private String dataScope;

}
