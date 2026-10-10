package org.dromara.iot.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务器监控
 * <p>
 * 采集本节点 JVM 与操作系统基础指标，供运行监控大屏展示。
 * 完整的集群级监控可由 Spring Boot Admin / Prometheus + Actuator 承担（见 iot-platform-monitor 依赖）。
 *
 * @author iot team
 */
@RestController
@RequestMapping("/iot/monitor/server")
public class ServerMonitorController {

    /**
     * 查询当前节点服务器运行指标
     *
     * @return 服务器指标
     */
    @SaCheckPermission("iot:monitor:server")
    @GetMapping("/metrics")
    public R<Map<String, Object>> metrics() {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        RuntimeInfo runtimeInfo = new RuntimeInfo(
            runtime.totalMemory(), runtime.freeMemory(), runtime.maxMemory(),
            runtimeMxBean.getUptime(), runtimeMxBean.getInputArguments().size(),
            osBean.getAvailableProcessors(), osBean.getSystemLoadAverage(),
            System.getProperty("os.name"), System.getProperty("java.version"));

        Map<String, Object> result = new LinkedHashMap<>(8);
        result.put("os", runtimeInfo.osName());
        result.put("javaVersion", runtimeInfo.javaVersion());
        result.put("availableProcessors", runtimeInfo.availableProcessors());
        result.put("cpuLoad", runtimeInfo.cpuLoad());
        result.put("uptimeMs", runtimeInfo.uptimeMs());
        result.put("jvmTotalMemory", runtimeInfo.totalMemory());
        result.put("jvmFreeMemory", runtimeInfo.freeMemory());
        result.put("jvmMaxMemory", runtimeInfo.maxMemory());
        return R.ok(result);
    }

    /**
     * 服务器运行指标快照
     *
     * @param totalMemory          JVM 已申请内存
     * @param freeMemory           JVM 空闲内存
     * @param maxMemory            JVM 最大可用内存
     * @param uptimeMs             JVM 运行时长
     * @param jvmInputArguments    启动参数数量
     * @param availableProcessors 可用处理器数
     * @param cpuLoad              系统平均负载
     * @param osName               操作系统名称
     * @param javaVersion          Java 版本
     */
    private record RuntimeInfo(
        long totalMemory,
        long freeMemory,
        long maxMemory,
        long uptimeMs,
        int jvmInputArguments,
        int availableProcessors,
        double cpuLoad,
        String osName,
        String javaVersion
    ) {
    }

}
