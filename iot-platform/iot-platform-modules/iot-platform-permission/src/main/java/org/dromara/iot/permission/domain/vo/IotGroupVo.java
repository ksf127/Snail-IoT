package org.dromara.iot.permission.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.iot.permission.domain.IotGroup;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据分组视图对象 iot_group
 *
 * @author iot team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = IotGroup.class)
public class IotGroupVo implements Serializable {

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 分组编码
     */
    @ExcelProperty(value = "分组编码")
    private String groupCode;

    /**
     * 分组名称
     */
    @ExcelProperty(value = "分组名称")
    private String groupName;

    /**
     * 父分组ID（0 为顶级）
     */
    private Long parentId;

    /**
     * 父分组名称（关联查询填充）
     */
    private String parentName;

    /**
     * 显示顺序
     */
    @ExcelProperty(value = "显示顺序")
    private Integer orderNum;

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
