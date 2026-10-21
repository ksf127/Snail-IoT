package org.dromara.iot.job.executor.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.iot.job.domain.IotJobLog;
import org.dromara.iot.job.executor.JobExecuteResult;
import org.dromara.iot.job.executor.JobHandler;
import org.dromara.iot.job.mapper.IotJobLogMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 内置执行器：任务执行日志清理
 * <p>
 * 任务参数支持两种形式：纯数字天数（如 {@code 7}）或 JSON（如 {@code {"days":7}}），默认保留 7 天。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanJobLogHandler implements JobHandler {

    private final IotJobLogMapper jobLogMapper;

    /**
     * 默认保留天数
     */
    private static final int DEFAULT_KEEP_DAYS = 7;

    @Override
    public String name() {
        return "cleanJobLog";
    }

    @Override
    public String description() {
        return "清理历史任务执行日志（参数：保留天数，默认 7）";
    }

    @Override
    public JobExecuteResult execute(String params) {
        int keepDays = resolveKeepDays(params);
        int deleted = jobLogMapper.delete(Wrappers.<IotJobLog>lambdaQuery()
            .lt(IotJobLog::getStartTime, LocalDateTime.now().minusDays(keepDays)));
        String message = String.format("已清理 %d 天前的执行日志，共 %d 条", keepDays, deleted);
        log.info("cleanJobLog 执行，参数：{}，结果：{}", params, message);
        return JobExecuteResult.success(message);
    }

    /**
     * 解析保留天数
     */
    private int resolveKeepDays(String params) {
        if (StringUtils.isBlank(params)) {
            return DEFAULT_KEEP_DAYS;
        }
        String text = params.trim();
        if (text.startsWith("{")) {
            int index = text.indexOf("days");
            if (index < 0) {
                return DEFAULT_KEEP_DAYS;
            }
            text = text.substring(index + 4);
            int colon = text.indexOf(':');
            if (colon < 0) {
                return DEFAULT_KEEP_DAYS;
            }
            text = text.substring(colon + 1);
        }
        StringBuilder digits = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.length() == 0) {
            return DEFAULT_KEEP_DAYS;
        }
        return Math.max(1, Integer.parseInt(digits.toString()));
    }

}
