package org.dromara.iot.permission.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据集-角色数据权限 iot_dataset_role
 * <p>
 * data_scope 复用 iot 数据范围：1全部 2自定义 3本部门 4本部门及以下 5仅本人。
 *
 * @author iot team
 */
@Data
@TableName("iot_dataset_role")
public class IotDatasetRole implements Serializable {

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）
     */
    private String dataScope;

}
