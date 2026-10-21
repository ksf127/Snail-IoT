package org.dromara.iot.job.executor.handler;

import lombok.extern.slf4j.Slf4j;
import org.dromara.iot.job.executor.JobExecuteResult;
import org.dromara.iot.job.executor.JobHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 内置执行器示例：系统心跳
 * <p>
 * 用于验证任务管理平台链路是否通畅。
 *
 * @author iot team
 */
@Slf4j
@Component
public class HeartbeatJobHandler implements JobHandler {

    @Override
    public String name() {
        return "heartbeatJob";
    }

    @Override
    public String description() {
        return "系统心跳检测（内置示例）";
    }

    @Override
    public JobExecuteResult execute(String params) {
        String message = "心跳正常，时间：" + LocalDateTime.now();
        log.info("heartbeatJob 执行，参数：{}，结果：{}", params, message);
        return JobExecuteResult.success(message);
    }

}
