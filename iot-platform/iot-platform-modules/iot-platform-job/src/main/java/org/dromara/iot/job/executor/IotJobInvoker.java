package org.dromara.iot.job.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.iot.job.domain.IotJobInfo;
import org.dromara.iot.job.domain.IotJobLog;
import org.dromara.iot.job.enums.JobExecuteStatusEnum;
import org.dromara.iot.job.mapper.IotJobInfoMapper;
import org.dromara.iot.job.mapper.IotJobLogMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 任务调用器
 * <p>
 * 统一入口：解析执行器、执行并落执行日志，回写任务最近触发时间。
 * 由本地调度器、手动触发接口与 SnailJob 执行器共同复用，避免三处重复实现。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IotJobInvoker {

    private final IotJobInfoMapper jobInfoMapper;
    private final IotJobLogMapper jobLogMapper;
    private final JobHandlerRegistry handlerRegistry;

    /**
     * 按任务ID执行
     *
     * @param jobId       任务ID
     * @param triggerType 触发方式
     * @return 执行日志
     */
    public IotJobLog invoke(Long jobId, String triggerType) {
        IotJobInfo info = jobId == null ? null : jobInfoMapper.selectById(jobId);
        if (info == null) {
            throw new ServiceException("任务不存在：" + jobId);
        }
        return invoke(info, triggerType);
    }

    /**
     * 按任务实体执行
     *
     * @param info        任务实体
     * @param triggerType 触发方式
     * @return 执行日志
     */
    public IotJobLog invoke(IotJobInfo info, String triggerType) {
        LocalDateTime startTime = LocalDateTime.now();
        boolean success = false;
        String result = null;
        String message = null;
        try {
            JobHandler handler = handlerRegistry.get(info.getHandlerName());
            if (handler == null) {
                throw new ServiceException("执行器不存在：" + info.getHandlerName());
            }
            JobExecuteResult executeResult = handler.execute(info.getJobParams());
            success = executeResult.isSuccess();
            result = executeResult.getMessage();
            if (!success) {
                message = executeResult.getMessage();
            }
        } catch (Exception e) {
            message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            log.error("任务执行异常，jobId={}，jobName={}", info.getJobId(), info.getJobName(), e);
        }
        LocalDateTime endTime = LocalDateTime.now();

        IotJobLog jobLog = new IotJobLog();
        jobLog.setJobId(info.getJobId());
        jobLog.setJobName(info.getJobName());
        jobLog.setTriggerType(triggerType);
        jobLog.setHandlerName(info.getHandlerName());
        jobLog.setStatus((success ? JobExecuteStatusEnum.SUCCESS : JobExecuteStatusEnum.FAIL).getCode());
        jobLog.setStartTime(startTime);
        jobLog.setEndTime(endTime);
        jobLog.setDuration(Duration.between(startTime, endTime).toMillis());
        jobLog.setResult(result);
        jobLog.setMessage(message);
        jobLogMapper.insert(jobLog);

        // 回写最近触发时间
        if (info.getJobId() != null) {
            IotJobInfo update = new IotJobInfo();
            update.setJobId(info.getJobId());
            update.setLastFireTime(startTime);
            jobInfoMapper.updateById(update);
        }
        return jobLog;
    }

}
