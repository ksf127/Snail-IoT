package org.dromara.iot.permission.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.iot.permission.domain.IotDataset;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据集视图对象 iot_dataset
 *
 * @author iot team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = IotDataset.class)
public class IotDatasetVo implements Serializable {

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 数据集编码
     */
    @ExcelProperty(value = "数据集编码")
    private String datasetCode;

    /**
     * 数据集名称
     */
    @ExcelProperty(value = "数据集名称")
    private String datasetName;

    /**
     * 所属分组ID
     */
    private Long groupId;

    /**
     * 所属分组名称（关联查询填充）
     */
    @ExcelProperty(value = "所属分组")
    private String groupName;

    /**
     * 数据集类型
     */
    @ExcelProperty(value = "数据集类型")
    private String datasetType;

    /**
     * 数据来源
     */
    @ExcelProperty(value = "数据来源")
    private String sourceTable;

    /**
     * 数据集描述
     */
    @ExcelProperty(value = "描述")
    private String description;

    /**
     * 数据行数（概览）
     */
    @ExcelProperty(value = "数据行数")
    private Long rowCount;

    /**
     * 状态（0正常 1停用）
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

}
