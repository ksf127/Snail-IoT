package org.dromara.iot.job.executor;

/**
 * 任务执行器 SPI
 * <p>
 * 业务方实现该接口并声明为 Spring Bean，即可被任务管理平台调度。
 * {@link #name()} 需与 iot_job_info.handler_name 一致。
 *
 * @author iot team
 */
public interface JobHandler {

    /**
     * 执行器名称（唯一）
     */
    String name();

    /**
     * 执行器描述（用于前端下拉提示）
     */
    default String description() {
        return "";
    }

    /**
     * 执行任务
     *
     * @param params 任务参数（JSON 文本，可能为 null）
     * @return 执行结果
     */
    JobExecuteResult execute(String params);

}
