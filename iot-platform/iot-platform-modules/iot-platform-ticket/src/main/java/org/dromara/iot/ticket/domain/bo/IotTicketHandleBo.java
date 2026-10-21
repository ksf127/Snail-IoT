package org.dromara.iot.ticket.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 工单办理业务对象
 *
 * @author iot team
 */
@Data
public class IotTicketHandleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    @NotNull(message = "工单ID不能为空")
    private Long ticketId;

    /**
     * 办理动作（accept受理 / handle处理 / finish完成 / close关闭 / reject驳回）
     */
    @NotBlank(message = "办理动作不能为空")
    private String action;

    /**
     * 处理人ID（用于受理时指定处理人，留空取当前登录用户）
     */
    private Long handlerId;

    /**
     * 处理人名称
     */
    private String handlerName;

    /**
     * 处理意见 / 说明
     */
    @Size(max = 1000, message = "处理意见长度不能超过 1000 个字符")
    private String message;

    /**
     * 调整后的优先级（0低 1中 2高 3紧急）
     */
    private String priority;

    /**
     * 流程任务ID（流程工单办理时携带）
     */
    private Long processTaskId;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
