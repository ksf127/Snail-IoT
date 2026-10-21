package org.dromara.iot.job.executor;

import java.io.Serializable;

/**
 * 任务执行结果
 *
 * @author iot team
 */
public class JobExecuteResult implements Serializable {

    /**
     * 是否执行成功
     */
    private final boolean success;

    /**
     * 结果描述
     */
    private final String message;

    private JobExecuteResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * 构造成功结果
     */
    public static JobExecuteResult success(String message) {
        return new JobExecuteResult(true, message);
    }

    /**
     * 构造失败结果
     */
    public static JobExecuteResult failure(String message) {
        return new JobExecuteResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

}
