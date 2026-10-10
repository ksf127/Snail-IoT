package org.dromara.iot.permission.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据集-角色数据权限视图对象
 *
 * @author iot team
 */
@Data
public class IotDatasetRoleVo implements Serializable {

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称（关联查询填充）
     */
    private String roleName;

    /**
     * 角色权限字符
     */
    private String roleKey;

    /**
     * 数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）
     */
    private String dataScope;

}
