package org.dromara.iot.job.executor.handler;

import lombok.extern.slf4j.Slf4j;
import org.dromara.iot.job.executor.JobExecuteResult;
import org.dromara.iot.job.executor.JobHandler;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Duration;

/**
 * 内置执行器示例：系统负载上报
 * <p>
 * 采集当前 JVM 的堆内存、CPU 核数与运行时长并输出，用于验证执行器在定时场景下的可用性。
 *
 * @author iot team
 */
@Slf4j
@Component
public class SystemLoadReportHandler implements JobHandler {

    private static final long MB = 1024L * 1024L;

    @Override
    public String name() {
        return "systemLoadReport";
    }

    @Override
    public String description() {
        return "系统负载上报（JVM 内存 / CPU 核数 / 运行时长）";
    }

    @Override
    public JobExecuteResult execute(String params) {
        Runtime runtime = Runtime.getRuntime();
        long maxMb = runtime.maxMemory() / MB;
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / MB;
        int processors = runtime.availableProcessors();
        long uptimeSeconds = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()).getSeconds();
        double usedPercent = maxMb == 0 ? 0D : (usedMb * 100.0 / maxMb);
        String message = String.format("JVM 内存 %dMB/%dMB（%.1f%%），CPU 核数 %d，运行时长 %ds",
            usedMb, maxMb, usedPercent, processors, uptimeSeconds);
        log.info("systemLoadReport 执行，参数：{}，结果：{}", params, message);
        return JobExecuteResult.success(message);
    }

}
