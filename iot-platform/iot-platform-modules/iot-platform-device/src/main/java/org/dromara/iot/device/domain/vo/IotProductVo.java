package org.dromara.iot.device.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.iot.device.domain.IotProduct;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品视图对象 iot_product
 *
 * @author iot team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = IotProduct.class)
public class IotProductVo implements Serializable {

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品编码
     */
    @ExcelProperty(value = "产品编码")
    private String productCode;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称")
    private String productName;

    /**
     * 接入协议
     */
    @ExcelProperty(value = "接入协议")
    private String protocol;

    /**
     * 设备类型
     */
    @ExcelProperty(value = "设备类型")
    private String deviceType;

    /**
     * 数据格式
     */
    @ExcelProperty(value = "数据格式")
    private String dataFormat;

    /**
     * 物模型定义（JSON 字符串）
     */
    private String thingModel;

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
