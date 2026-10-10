package org.dromara.iot.collect.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.iot.collect.domain.DeviceDataMessage;
import org.dromara.iot.collect.kafka.DeviceDataKafkaProducer;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.common.enums.CollectSourceEnum;
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * MQTT 设备数据订阅器
 * <p>
 * 订阅主题：iot/device/{deviceCode}/#
 * 客户端开关：mqtt.client.enabled=true 时生效（由 iot-common-mqtt 提供 MqttClientTemplate）。
 * <p>
 * 采集链路：MQTT 上报 -> 本订阅器 -> Kafka(iot-device-data) -> 落库/统计/告警。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "mqtt.client.enabled", havingValue = "true")
public class MqttDeviceDataSubscriber {

    private final MqttClientTemplate mqttClientTemplate;
    private final DeviceDataKafkaProducer kafkaProducer;

    /**
     * 应用就绪后订阅设备上报主题。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        String topic = IotConstants.MQTT_TOPIC_DEVICE_PREFIX + "+/#";
        mqttClientTemplate.subQos1(topic, (context, topicName, message, payload) -> {
            String deviceCode = resolveDeviceCode(topicName);
            if (deviceCode == null) {
                return;
            }
            DeviceDataMessage data = new DeviceDataMessage();
            data.setDeviceCode(deviceCode);
            data.setSource(CollectSourceEnum.MQTT.getCode());
            data.setPayload(new String(payload, StandardCharsets.UTF_8));
            data.setTimestamp(System.currentTimeMillis());
            kafkaProducer.send(data);
        });
        log.info("[IoT] 已订阅 MQTT 设备上报主题: {}", topic);
    }

    /**
     * 从主题中解析设备编码，主题格式：iot/device/{deviceCode}/xxx
     *
     * @param topic MQTT 主题
     * @return 设备编码，格式不匹配时返回 null
     */
    private String resolveDeviceCode(String topic) {
        String[] segments = topic.split("/");
        if (segments.length < 3) {
            log.warn("[IoT] 非法设备上报主题: {}", topic);
            return null;
        }
        return segments[2];
    }

}
