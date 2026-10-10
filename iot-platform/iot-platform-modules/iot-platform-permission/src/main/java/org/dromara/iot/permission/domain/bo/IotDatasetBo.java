package org.dromara.iot.permission.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.permission.domain.IotDataset;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据集业务对象 iot_dataset
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotDataset.class, reverseConvertGenerate = false)
public class IotDatasetBo implements Serializable {

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 数据集编码
     */
    @NotBlank(message = "数据集编码不能为空")
    @Size(max = 64, message = "数据集编码长度不能超过 64 个字符")
    private String datasetCode;

    /**
     * 数据集名称
     */
    @NotBlank(message = "数据集名称不能为空")
    @Size(max = 128, message = "数据集名称长度不能超过 128 个字符")
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
     * 备注
     */
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
