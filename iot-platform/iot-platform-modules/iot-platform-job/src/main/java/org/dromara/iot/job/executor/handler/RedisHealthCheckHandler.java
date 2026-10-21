package org.dromara.iot.job.executor.handler;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.iot.job.executor.JobExecuteResult;
import org.dromara.iot.job.executor.JobHandler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 内置执行器示例：Redis 巡检
 * <p>
 * 通过写入并读回一个带过期时间的探针键，校验 Redis 读写链路是否正常。
 * 任务参数支持纯文本键名（如 {@code iot:job:redis-health}）或 JSON（如 {@code {"key":"iot:job:redis-health"}}），默认使用内置键名。
 *
 * @author iot team
 */
@Slf4j
@Component
public class RedisHealthCheckHandler implements JobHandler {

    private static final String DEFAULT_KEY = "iot:job:redis-health";

    @Override
    public String name() {
        return "redisHealthCheck";
    }

    @Override
    public String description() {
        return "Redis 巡检（探针键读写校验，参数可指定键名）";
    }

    @Override
    public JobExecuteResult execute(String params) {
        String key = resolveKey(params);
        String marker = LocalDateTime.now().toString();
        try {
            RedisUtils.setCacheObject(key, marker, Duration.ofMinutes(10));
            String value = RedisUtils.getCacheObject(key);
            if (!marker.equals(value)) {
                String message = String.format("Redis 巡检异常：探针键 %s 读回值不一致（期望 %s，实际 %s）", key, marker, value);
                log.warn("redisHealthCheck 执行失败，参数：{}，结果：{}", params, message);
                return JobExecuteResult.failure(message);
            }
            String message = String.format("Redis 巡检正常，探针键 %s 读写校验通过", key);
            log.info("redisHealthCheck 执行，参数：{}，结果：{}", params, message);
            return JobExecuteResult.success(message);
        } catch (Exception e) {
            String message = String.format("Redis 巡检异常：%s", e.getMessage());
            log.error("redisHealthCheck 执行异常，参数：{}", params, e);
            return JobExecuteResult.failure(message);
        }
    }

    /**
     * 解析探针键名
     */
    private String resolveKey(String params) {
        if (StringUtils.isBlank(params)) {
            return DEFAULT_KEY;
        }
        String text = params.trim();
        if (text.startsWith("{")) {
            int index = text.indexOf("\"key\"");
            if (index < 0) {
                index = text.indexOf("key");
            }
            if (index < 0) {
                return DEFAULT_KEY;
            }
            text = text.substring(index);
            int colon = text.indexOf(':');
            if (colon < 0) {
                return DEFAULT_KEY;
            }
            text = text.substring(colon + 1).replace("\"", "").trim();
            int end = text.indexOf('}');
            if (end >= 0) {
                text = text.substring(0, end).trim();
            }
            if (text.isEmpty()) {
                return DEFAULT_KEY;
            }
        }
        return text;
    }

}
