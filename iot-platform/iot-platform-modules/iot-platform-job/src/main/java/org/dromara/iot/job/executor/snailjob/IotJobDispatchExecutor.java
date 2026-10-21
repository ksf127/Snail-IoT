package org.dromara.iot.job.executor.snailjob;

import cn.hutool.core.lang.Dict;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.iot.job.domain.IotJobLog;
import org.dromara.iot.job.enums.JobExecuteStatusEnum;
import org.dromara.iot.job.enums.JobTriggerTypeEnum;
import org.dromara.iot.job.executor.IotJobInvoker;
import org.springframework.stereotype.Component;

/**
 * SnailJob 执行器：统一分发
 * <p>
 * 在 SnailJob 后台登记任务时，执行器名称填 {@code iotJobDispatch}，
 * 任务参数填本平台的任务ID（如 {@code {"jobId":1900000000000007101}} 或纯数字 {@code 1900000000000007101}）。
 * 触发后由本执行器转交 {@link IotJobInvoker} 执行对应的业务执行器并落执行日志。
 * <p>
 * 注意：本执行器仅在 {@code snail-job.enabled=true} 且存在 SnailJob 服务端时被注册。
 *
 * @author iot team
 */
@Component
@RequiredArgsConstructor
@JobExecutor(name = "iotJobDispatch")
public class IotJobDispatchExecutor {

    private final IotJobInvoker jobInvoker;

    /**
     * 执行 SnailJob 分发的任务
     *
     * @param jobArgs SnailJob 任务参数
     * @return 执行结果
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        String raw = jobArgs.getJobParams() == null ? null : String.valueOf(jobArgs.getJobParams());
        SnailJobLog.LOCAL.info("iotJobDispatch 触发，snailJobId={}，params={}", jobArgs.getJobId(), raw);

        Long iotJobId = resolveJobId(raw);
        if (iotJobId == null) {
            String message = "任务参数缺少 jobId，无法定位本地任务：" + raw;
            SnailJobLog.REMOTE.error(message);
            return ExecuteResult.failure(null, message);
        }

        IotJobLog jobLog = jobInvoker.invoke(iotJobId, JobTriggerTypeEnum.SNAILJOB.getCode());
        String detail = JobExecuteStatusEnum.SUCCESS.getCode().equals(jobLog.getStatus())
            ? jobLog.getResult() : jobLog.getMessage();
        if (JobExecuteStatusEnum.SUCCESS.getCode().equals(jobLog.getStatus())) {
            SnailJobLog.REMOTE.info("任务执行成功，jobId={}，结果：{}", iotJobId, detail);
            return ExecuteResult.success(detail);
        }
        SnailJobLog.REMOTE.error("任务执行失败，jobId={}，原因：{}", iotJobId, detail);
        return ExecuteResult.failure(null, detail);
    }

    /**
     * 从任务参数中解析本平台的任务ID，支持 JSON 对象与纯数字两种形式
     */
    private Long resolveJobId(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String text = raw.trim();
        if (JsonUtils.isJsonObject(text)) {
            Dict dict = JsonUtils.parseMap(text);
            return dict == null ? null : dict.getLong("jobId");
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
