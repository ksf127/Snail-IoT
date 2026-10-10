package org.dromara.iot.permission.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.iot.permission.domain.IotApp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用视图对象 iot_app
 *
 * @author iot team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = IotApp.class)
public class IotAppVo implements Serializable {

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 应用编码
     */
    @ExcelProperty(value = "应用编码")
    private String appCode;

    /**
     * 应用名称
     */
    @ExcelProperty(value = "应用名称")
    private String appName;

    /**
     * 应用类型
     */
    @ExcelProperty(value = "应用类型")
    private String appType;

    /**
     * 访问地址
     */
    @ExcelProperty(value = "访问地址")
    private String accessUrl;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 引用数据集数量（关联统计填充）
     */
    private Long datasetCount;

    /**
     * 授权角色数量（关联统计填充）
     */
    private Long roleCount;

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
