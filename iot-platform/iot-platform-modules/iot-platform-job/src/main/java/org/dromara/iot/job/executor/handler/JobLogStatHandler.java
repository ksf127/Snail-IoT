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
 * 内置执行器示例：任务执行日志统计
 * <p>
 * 统计近 N 天内任务执行日志的成功与失败条数。
 * 任务参数支持纯数字天数（如 {@code 1}）或 JSON（如 {@code {"days":1}}），默认统计近 1 天。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobLogStatHandler implements JobHandler {

    private final IotJobLogMapper jobLogMapper;

    /**
     * 成功状态
     */
    private static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * 失败状态
     */
    private static final String STATUS_FAIL = "FAIL";

    /**
     * 默认统计天数
     */
    private static final int DEFAULT_DAYS = 1;

    @Override
    public String name() {
        return "jobLogStat";
    }

    @Override
    public String description() {
        return "任务执行日志统计（参数：统计天数，默认 1）";
    }

    @Override
    public JobExecuteResult execute(String params) {
        int days = resolveDays(params);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Long success = jobLogMapper.selectCount(Wrappers.<IotJobLog>lambdaQuery()
            .ge(IotJobLog::getStartTime, since)
            .eq(IotJobLog::getStatus, STATUS_SUCCESS));
        Long fail = jobLogMapper.selectCount(Wrappers.<IotJobLog>lambdaQuery()
            .ge(IotJobLog::getStartTime, since)
            .eq(IotJobLog::getStatus, STATUS_FAIL));
        long successCount = success == null ? 0L : success;
        long failCount = fail == null ? 0L : fail;
        String message = String.format("近 %d 天任务执行：成功 %d 条，失败 %d 条，共 %d 条",
            days, successCount, failCount, successCount + failCount);
        log.info("jobLogStat 执行，参数：{}，结果：{}", params, message);
        return JobExecuteResult.success(message);
    }

    /**
     * 解析统计天数
     */
    private int resolveDays(String params) {
        if (StringUtils.isBlank(params)) {
            return DEFAULT_DAYS;
        }
        String text = params.trim();
        if (text.startsWith("{")) {
            int index = text.indexOf("days");
            if (index < 0) {
                return DEFAULT_DAYS;
            }
            text = text.substring(index + 4);
            int colon = text.indexOf(':');
            if (colon < 0) {
                return DEFAULT_DAYS;
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
            return DEFAULT_DAYS;
        }
        return Math.max(1, Integer.parseInt(digits.toString()));
    }

}
