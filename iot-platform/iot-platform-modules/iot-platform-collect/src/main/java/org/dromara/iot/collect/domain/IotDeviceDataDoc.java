package org.dromara.iot.collect.domain;

import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.annotation.rely.IdType;
import org.dromara.iot.common.constant.IotConstants;

import java.io.Serializable;

/**
 * 设备上报数据 Elasticsearch 文档
 * <p>
 * 采集链路最终落库载体，海量时序数据的聚合统计由 ES 承担。
 *
 * @author iot team
 */
@Data
@IndexName(value = IotConstants.ES_INDEX_DEVICE_DATA)
public class IotDeviceDataDoc implements Serializable {

    /**
     * 文档 ID（由采集侧生成的雪花 ID 字符串）
     */
    @IndexId(type = IdType.CUSTOMIZE)
    private String id;

    /**
     * 设备编码
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String deviceCode;

    /**
     * 产品编码
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String productCode;

    /**
     * 采集来源 MQTT / HTTPS / MANUAL
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String source;

    /**
     * 设备上报的原始报文
     */
    @IndexField(fieldType = FieldType.TEXT)
    private String payload;

    /**
     * 上报时间（毫秒时间戳）
     */
    @IndexField(fieldType = FieldType.LONG)
    private Long timestamp;

    /**
     * 链路追踪 ID
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String traceId;

}
