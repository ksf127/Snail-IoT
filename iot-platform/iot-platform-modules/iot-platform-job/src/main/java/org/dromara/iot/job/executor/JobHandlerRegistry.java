package org.dromara.iot.job.executor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务执行器注册表
 * <p>
 * 启动时收集容器内全部 {@link JobHandler} 实现，按名称索引，供调度器与 SnailJob 执行器查找。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobHandlerRegistry {

    private final List<JobHandler> handlers;

    private final Map<String, JobHandler> handlerMap = new LinkedHashMap<>();

    /**
     * 构建名称索引（重复名称时后者覆盖并告警）
     */
    @PostConstruct
    public void init() {
        for (JobHandler handler : handlers) {
            JobHandler previous = handlerMap.put(handler.name(), handler);
            if (previous != null) {
                log.warn("执行器名称重复：{}（{} 覆盖 {}）", handler.name(),
                    handler.getClass().getSimpleName(), previous.getClass().getSimpleName());
            }
        }
        log.info("任务执行器注册完成，共 {} 个：{}", handlerMap.size(), handlerMap.keySet());
    }

    /**
     * 按名称获取执行器，不存在返回 null
     */
    public JobHandler get(String name) {
        return handlerMap.get(name);
    }

    /**
     * 按名称获取执行器，不存在抛出业务异常
     */
    public JobHandler require(String name) {
        JobHandler handler = handlerMap.get(name);
        if (handler == null) {
            throw new ServiceException("执行器不存在：" + name);
        }
        return handler;
    }

    /**
     * 全部执行器（名称 -> 描述），用于前端下拉
     */
    public Map<String, String> describeAll() {
        Map<String, String> result = new LinkedHashMap<>();
        handlerMap.forEach((name, handler) -> result.put(name, handler.description()));
        return Collections.unmodifiableMap(result);
    }

}
