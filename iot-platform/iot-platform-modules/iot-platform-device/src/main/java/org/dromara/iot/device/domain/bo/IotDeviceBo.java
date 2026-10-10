package org.dromara.iot.device.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.device.domain.IotDevice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备业务对象 iot_device
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotDevice.class, reverseConvertGenerate = false)
public class IotDeviceBo implements Serializable {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 所属产品ID
     */
    @NotNull(message = "所属产品不能为空")
    private Long productId;

    /**
     * 设备编码
     */
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 64, message = "设备编码长度不能超过 64 个字符")
    private String deviceCode;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 128, message = "设备名称长度不能超过 128 个字符")
    private String deviceName;

    /**
     * 接入协议 MQTT / HTTPS / MODBUS / OTHER
     */
    private String protocol;

    /**
     * 设备密钥
     */
    private String deviceSecret;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 在线状态（0离线 1在线 2未知）
     */
    private String onlineStatus;

    /**
     * 设备使能状态（0正常 1停用）
     */
    private String status;

    /**
     * 设备 IP
     */
    private String ipAddress;

    /**
     * 安装位置
     */
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
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
