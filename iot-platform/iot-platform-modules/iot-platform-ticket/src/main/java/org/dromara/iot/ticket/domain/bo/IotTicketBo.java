package org.dromara.iot.ticket.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.ticket.domain.IotTicket;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 工单业务对象 iot_ticket
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotTicket.class, reverseConvertGenerate = false)
public class IotTicketBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 工单编号（留空自动生成）
     */
    @Size(max = 64, message = "工单编号长度不能超过 64 个字符")
    private String ticketNo;

    /**
     * 工单标题
     */
    @NotBlank(message = "工单标题不能为空")
    @Size(max = 128, message = "工单标题长度不能超过 128 个字符")
    private String title;

    /**
     * 工单内容
     */
    @NotBlank(message = "工单内容不能为空")
    @Size(max = 2000, message = "工单内容长度不能超过 2000 个字符")
    private String content;

    /**
     * 工单类型（normal/rule/flow）
     */
    private String ticketType;

    /**
     * 工单分类
     */
    @Size(max = 64, message = "工单分类长度不能超过 64 个字符")
    private String category;

    /**
     * 优先级（0低 1中 2高 3紧急）
     */
    private String priority;

    /**
     * 状态（0待受理 1处理中 2已完成 3已关闭 4已驳回）
     */
    private String status;

    /**
     * 来源（web/app/api）
     */
    private String source;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人名称
     */
    private String applicantName;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人名称
     */
    private String handlerName;

    /**
     * 流程定义编码（流程工单使用）
     */
    private String flowCode;

    /**
     * 期望完成时间
     */
    private LocalDateTime expectedTime;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
