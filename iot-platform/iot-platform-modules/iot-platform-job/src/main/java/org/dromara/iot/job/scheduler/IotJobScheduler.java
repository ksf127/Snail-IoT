package org.dromara.iot.job.scheduler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.iot.job.domain.IotJobInfo;
import org.dromara.iot.job.enums.JobTriggerTypeEnum;
import org.dromara.iot.job.enums.JobTypeEnum;
import org.dromara.iot.job.executor.IotJobInvoker;
import org.dromara.iot.job.mapper.IotJobInfoMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 本地任务调度器
 * <p>
 * 基于 {@link ThreadPoolTaskScheduler} 实现进程内调度，仅接管 jobType=LOCAL 且 triggerType 为
 * CRON / FIXED_RATE 的启用任务。SNAILJOB 任务交由 SnailJob 服务端调度，MANUAL 任务只能手动触发。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IotJobScheduler {

    private final IotJobInfoMapper jobInfoMapper;

    private final IotJobInvoker jobInvoker;

    /**
     * 已注册的调度句柄，key 为任务ID
     */
    private final Map<Long, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    private final ThreadPoolTaskScheduler taskScheduler = buildScheduler();

    private ThreadPoolTaskScheduler buildScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("iot-job-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 注册或刷新任务调度（已注册的先取消）
     */
    public void schedule(IotJobInfo info) {
        if (info == null || info.getJobId() == null) {
            return;
        }
        cancel(info.getJobId());
        JobTriggerTypeEnum triggerType = JobTriggerTypeEnum.fromCode(info.getTriggerType());
        if (triggerType == null) {
            return;
        }
        Runnable task = () -> jobInvoker.invoke(info, triggerType.getCode());
        ScheduledFuture<?> future;
        if (JobTriggerTypeEnum.CRON == triggerType) {
            if (info.getCronExpr() == null || info.getCronExpr().isBlank()) {
                log.warn("任务缺少 Cron 表达式，跳过注册，jobId={}", info.getJobId());
                return;
            }
            future = taskScheduler.schedule(task, new CronTrigger(info.getCronExpr()));
        } else if (JobTriggerTypeEnum.FIXED_RATE == triggerType) {
            if (info.getIntervalSeconds() == null || info.getIntervalSeconds() <= 0) {
                log.warn("任务缺少合法的固定频率间隔，跳过注册，jobId={}", info.getJobId());
                return;
            }
            future = taskScheduler.scheduleAtFixedRate(task, Duration.ofSeconds(info.getIntervalSeconds()));
        } else {
            return;
        }
        if (future != null) {
            futures.put(info.getJobId(), future);
            log.info("任务已注册，jobId={}，jobName={}，triggerType={}", info.getJobId(), info.getJobName(), triggerType.getCode());
        }
    }

    /**
     * 取消任务调度
     */
    public void cancel(Long jobId) {
        if (jobId == null) {
            return;
        }
        ScheduledFuture<?> future = futures.remove(jobId);
        if (future != null) {
            future.cancel(false);
            log.info("任务已取消，jobId={}", jobId);
        }
    }

    /**
     * 清空全部调度
     */
    public void clear() {
        futures.keySet().forEach(this::cancel);
        futures.clear();
    }

    /**
     * 是否已注册
     */
    public boolean isScheduled(Long jobId) {
        return jobId != null && futures.containsKey(jobId);
    }

    /**
     * 应用启动完成后加载已启用的本地任务
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initOnStartup() {
        List<IotJobInfo> jobs;
        try {
            jobs = jobInfoMapper.selectList(Wrappers.<IotJobInfo>lambdaQuery()
                .eq(IotJobInfo::getJobType, JobTypeEnum.LOCAL.getCode())
                .eq(IotJobInfo::getStatus, "0")
                .in(IotJobInfo::getTriggerType, JobTriggerTypeEnum.CRON.getCode(), JobTriggerTypeEnum.FIXED_RATE.getCode()));
        } catch (Exception e) {
            log.error("加载本地调度任务失败", e);
            return;
        }
        for (IotJobInfo job : jobs) {
            try {
                schedule(job);
            } catch (Exception e) {
                log.error("注册本地调度任务失败，jobId={}", job.getJobId(), e);
            }
        }
        log.info("本地调度任务加载完成，共 {} 个", futures.size());
    }

}
