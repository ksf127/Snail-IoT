package org.dromara.iot.collect.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.iot.collect.domain.DeviceDataMessage;
import org.dromara.iot.common.constant.IotConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 设备数据 Kafka 生产者
 * <p>
 * 采集侧只负责「收得快」，落库、统计、告警等重逻辑由 Kafka 下游消费者异步完成，
 * 从而在设备上报洪峰时实现削峰填谷。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceDataKafkaProducer {

    private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;

    /**
     * 投递设备数据至 Kafka。
     * <p>
     * Kafka 不可用时仅记录告警，不阻断设备上报链路。
     *
     * @param message 设备数据消息
     */
    public void send(DeviceDataMessage message) {
        KafkaTemplate<String, String> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate 未就绪，设备数据未投递: {}", message.getDeviceCode());
            return;
        }
        try {
            kafkaTemplate.send(IotConstants.KAFKA_TOPIC_DEVICE_DATA, message.getDeviceCode(),
                JsonUtils.toJsonString(message));
        } catch (Exception e) {
            log.error("设备数据投递 Kafka 失败, deviceCode={}", message.getDeviceCode(), e);
        }
    }

    /**
     * 投递设备事件（上线/下线/告警）至 Kafka。
     *
     * @param deviceCode 设备编码
     * @param event      事件内容
     */
    public void sendEvent(String deviceCode, Object event) {
        KafkaTemplate<String, String> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate 未就绪，设备事件未投递: {}", deviceCode);
            return;
        }
        try {
            kafkaTemplate.send(IotConstants.KAFKA_TOPIC_DEVICE_EVENT, deviceCode, JsonUtils.toJsonString(event));
        } catch (Exception e) {
            log.error("设备事件投递 Kafka 失败, deviceCode={}", deviceCode, e);
        }
    }

}
