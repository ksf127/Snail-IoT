package org.dromara.iot.device.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 设备对象 iot_device
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_device")
public class IotDevice extends BaseEntity {

    /**
     * 设备ID
     */
    @TableId(value = "device_id", type = IdType.ASSIGN_ID)
    private Long deviceId;

    /**
     * 所属产品ID
     */
    private Long productId;

    /**
     * 设备编码（全局唯一，设备侧用于鉴权上报）
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 接入协议 MQTT / HTTPS / MODBUS / OTHER
     */
    private String protocol;

    /**
     * 设备密钥（用于 MQTT/HTTPS 上报鉴权）
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
     * 最近上线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 最近一次数据上报时间
     */
    private LocalDateTime lastReportTime;

    /**
     * 设备 IP
     */
    private String ipAddress;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 标签（JSON 数组，便于按业务属性检索）
     */
    private String tags;

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
