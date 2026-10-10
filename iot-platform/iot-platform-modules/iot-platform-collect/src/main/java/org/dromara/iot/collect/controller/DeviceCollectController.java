package org.dromara.iot.collect.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.iot.collect.domain.DeviceDataMessage;
import org.dromara.iot.collect.kafka.DeviceDataKafkaProducer;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.common.enums.CollectSourceEnum;
import org.dromara.iot.device.domain.IotDevice;
import org.dromara.iot.device.service.IIotDeviceService;
import org.redisson.api.RateType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTPS 设备数据上报入口
 * <p>
 * 设备侧（无 MQTT 能力，如摄像头、PLC 网关）通过 HTTPS 直连上报。
 * 鉴权方式：设备编码 + 设备密钥（deviceSecret 请求头），并按设备维度限流。
 *
 * @author iot team
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/collect/https")
public class DeviceCollectController {

    /**
     * 设备状态：正常
     */
    private static final String DEVICE_STATUS_NORMAL = "0";

    private final DeviceDataKafkaProducer kafkaProducer;
    private final IIotDeviceService deviceService;

    /**
     * HTTPS 上报设备数据
     *
     * @param deviceCode   设备编码
     * @param deviceSecret 设备密钥（请求头 deviceSecret）
     * @param payload      设备上报报文（JSON 字符串）
     * @return 受理结果
     */
    @SaIgnore
    @PostMapping("/{deviceCode}")
    public R<Void> report(@PathVariable String deviceCode,
                          @RequestHeader(value = "deviceSecret", required = false) String deviceSecret,
                          @RequestBody String payload) {
        IotDevice device = deviceService.queryByDeviceCode(deviceCode);
        if (device == null) {
            return R.fail("设备不存在");
        }
        if (!DEVICE_STATUS_NORMAL.equals(device.getStatus())) {
            return R.fail("设备已停用");
        }
        if (StrUtil.isBlank(device.getDeviceSecret()) || !StrUtil.equals(device.getDeviceSecret(), deviceSecret)) {
            return R.fail("设备密钥校验失败");
        }
        long permits = RedisUtils.rateLimiter(IotConstants.COLLECT_LIMIT_PREFIX + deviceCode, RateType.OVERALL,
            IotConstants.COLLECT_LIMIT_RATE, IotConstants.COLLECT_LIMIT_INTERVAL);
        if (permits < 0) {
            return R.fail("上报过于频繁，请稍后再试");
        }

        DeviceDataMessage data = new DeviceDataMessage();
        data.setDeviceCode(deviceCode);
        data.setSource(CollectSourceEnum.HTTPS.getCode());
        data.setPayload(payload);
        data.setTimestamp(System.currentTimeMillis());
        kafkaProducer.send(data);
        return R.ok();
    }

}
