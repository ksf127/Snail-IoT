package org.dromara.iot.deploy.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.deploy.domain.IotDeployTask;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 部署任务业务对象 iot_deploy_task
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotDeployTask.class, reverseConvertGenerate = false)
public class IotDeployTaskBo implements Serializable {

    /**
     * 部署任务ID
     */
    private Long taskId;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 版本号
     */
    private String version;

    /**
     * 目标环境（dev / test / prod）
     */
    private String targetEnv;

    /**
     * 任务类型（deploy发布 build构建）
     */
    private String taskType;

    /**
     * 状态（0待执行 1执行中 2成功 3失败）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
