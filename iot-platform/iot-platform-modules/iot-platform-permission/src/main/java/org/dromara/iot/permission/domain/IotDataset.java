package org.dromara.iot.permission.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 数据集对象 iot_dataset（数据线：数据资产）
 * <p>
 * 挂在分组下、被应用引用、受角色数据权限约束。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_dataset")
public class IotDataset extends BaseEntity {

    /**
     * 数据集ID
     */
    @TableId(value = "dataset_id", type = IdType.ASSIGN_ID)
    private Long datasetId;

    /**
     * 数据集编码
     */
    private String datasetCode;

    /**
     * 数据集名称
     */
    private String datasetName;

    /**
     * 所属分组ID
     */
    private Long groupId;

    /**
     * 数据集类型 TABLE / VIEW / API / STREAM
     */
    private String datasetType;

    /**
     * 数据来源（物理表 / 视图 / 接口地址）
     */
    private String sourceTable;

    /**
     * 数据集描述
     */
    private String description;

    /**
     * 数据行数（概览）
     */
    private Long rowCount;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

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
