package org.dromara.iot.collect.kafka;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.iot.collect.domain.DeviceDataMessage;
import org.dromara.iot.collect.domain.IotDeviceDataDoc;
import org.dromara.iot.collect.esmapper.IotDeviceDataEsMapper;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.common.enums.DeviceOnlineStatusEnum;
import org.dromara.iot.device.domain.IotDevice;
import org.dromara.iot.device.service.IIotDeviceService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 设备数据 Kafka 消费者
 * <p>
 * 承接 {@link DeviceDataKafkaProducer} 投递的采集数据，完成：
 * <ol>
 *     <li>原始报文落库至 Elasticsearch（海量时序数据，支撑报表聚合）</li>
 *     <li>设备在线状态回写（Redis 心跳 + 数据库状态切换）</li>
 * </ol>
 * Easy-Es 未启用（easy-es.enable=false）时不落库，仅保留状态回写，保证链路可用。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceDataKafkaConsumer {

    /**
     * 设备在线心跳有效期（分钟），超时未上报即视为离线
     */
    private static final int ONLINE_TTL_MINUTES = 5;

    private final ObjectProvider<IotDeviceDataEsMapper> esMapperProvider;
    private final IIotDeviceService deviceService;

    /**
     * ES 索引是否已就绪，避免每次上报都探测索引
     */
    private final AtomicBoolean indexReady = new AtomicBoolean(false);

    /**
     * 消费设备上报数据。
     *
     * @param message Kafka 消息体（DeviceDataMessage 的 JSON）
     * @param ack     手动确认句柄
     */
    @KafkaListener(topics = IotConstants.KAFKA_TOPIC_DEVICE_DATA, groupId = "iot-platform")
    public void onMessage(String message, Acknowledgment ack) {
        try {
            DeviceDataMessage data = JsonUtils.parseObject(message, DeviceDataMessage.class);
            if (data == null || StrUtil.isBlank(data.getDeviceCode())) {
                log.warn("[IoT] 非法设备上报消息，已丢弃: {}", message);
                return;
            }
            saveToEs(data);
            writeBackOnlineStatus(data);
        } catch (Exception e) {
            log.error("[IoT] 设备数据消费失败: {}", message, e);
        } finally {
            ack.acknowledge();
        }
    }

    /**
     * 原始报文落库 Elasticsearch。
     *
     * @param data 设备数据消息
     */
    private void saveToEs(DeviceDataMessage data) {
        IotDeviceDataEsMapper esMapper = esMapperProvider.getIfAvailable();
        if (esMapper == null) {
            return;
        }
        try {
            if (!indexReady.get()) {
                if (!esMapper.existsIndex(IotConstants.ES_INDEX_DEVICE_DATA)) {
                    esMapper.createIndex();
                }
                indexReady.set(true);
            }
            IotDeviceDataDoc doc = new IotDeviceDataDoc();
            doc.setId(IdUtil.fastSimpleUUID());
            doc.setDeviceCode(data.getDeviceCode());
            doc.setProductCode(data.getProductCode());
            doc.setSource(data.getSource());
            doc.setPayload(data.getPayload());
            doc.setTimestamp(data.getTimestamp() == null ? System.currentTimeMillis() : data.getTimestamp());
            doc.setTraceId(data.getTraceId());
            esMapper.insert(doc);
        } catch (Exception e) {
            // 索引创建失败或集群不可用时，下次消费重新探测
            indexReady.set(false);
            log.error("[IoT] 设备数据写入 Elasticsearch 失败, deviceCode={}", data.getDeviceCode(), e);
        }
    }

    /**
     * 回写设备在线状态。
     * <p>
     * 以 Redis 心跳判定在线会话：心跳缺失说明设备刚上线，此时才落库并刷新在线时间，
     * 避免每条上报都触发数据库更新造成写放大。
     *
     * @param data 设备数据消息
     */
    private void writeBackOnlineStatus(DeviceDataMessage data) {
        String key = IotConstants.DEVICE_ONLINE_PREFIX + data.getDeviceCode();
        long now = System.currentTimeMillis();
        Object heartbeat = RedisUtils.getCacheObject(key);
        RedisUtils.setCacheObject(key, now, Duration.ofMinutes(ONLINE_TTL_MINUTES));
        if (heartbeat != null) {
            return;
        }
        IotDevice device = deviceService.queryByDeviceCode(data.getDeviceCode());
        if (device == null) {
            log.warn("[IoT] 上报数据对应设备不存在, deviceCode={}", data.getDeviceCode());
            return;
        }
        deviceService.refreshOnline(device.getDeviceId(), DeviceOnlineStatusEnum.ONLINE.getCode());
        log.info("[IoT] 设备上线, deviceCode={}", data.getDeviceCode());
    }

}
