package org.dromara.iot.device.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.iot.device.domain.IotDevice;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备视图对象 iot_device
 *
 * @author iot team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = IotDevice.class)
public class IotDeviceVo implements Serializable {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 所属产品ID
     */
    private Long productId;

    /**
     * 产品名称（关联查询填充）
     */
    @ExcelProperty(value = "产品名称")
    private String productName;

    /**
     * 设备编码
     */
    @ExcelProperty(value = "设备编码")
    private String deviceCode;

    /**
     * 设备名称
     */
    @ExcelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 接入协议
     */
    @ExcelProperty(value = "接入协议")
    private String protocol;

    /**
     * 固件版本
     */
    @ExcelProperty(value = "固件版本")
    private String firmwareVersion;

    /**
     * 在线状态（0离线 1在线 2未知）
     */
    @ExcelProperty(value = "在线状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "iot_device_online_status")
    private String onlineStatus;

    /**
     * 设备使能状态（0正常 1停用）
     */
    @ExcelProperty(value = "设备状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 最近上线时间
     */
    @ExcelProperty(value = "最近上线时间")
    private LocalDateTime lastOnlineTime;

    /**
     * 最近一次数据上报时间
     */
    @ExcelProperty(value = "最近上报时间")
    private LocalDateTime lastReportTime;

    /**
     * 设备 IP
     */
    @ExcelProperty(value = "设备IP")
    private String ipAddress;

    /**
     * 安装位置
     */
    @ExcelProperty(value = "安装位置")
    private String location;

    /**
     * 标签
     */
    private String tags;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

}
