package org.dromara.iot.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.iot.device.service.IIotDeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 设备监控
 * <p>
 * 提供设备在线率等监控指标，供监控大屏与告警规则使用。
 *
 * @author iot team
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/monitor/device")
public class DeviceMonitorController {

    private final IIotDeviceService deviceService;

    /**
     * 设备在线状态总览
     *
     * @return 设备在线指标
     */
    @SaCheckPermission("iot:monitor:device")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        long total = deviceService.countByOnlineStatus(null);
        long online = deviceService.countByOnlineStatus("1");
        long offline = deviceService.countByOnlineStatus("0");
        long unknown = deviceService.countByOnlineStatus("2");

        Map<String, Object> result = new LinkedHashMap<>(8);
        result.put("total", total);
        result.put("online", online);
        result.put("offline", offline);
        result.put("unknown", unknown);
        result.put("onlineRate", total == 0 ? 0D : (double) online / total);
        return R.ok(result);
    }

}
