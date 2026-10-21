package org.dromara.iot.ticket.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.ticket.domain.IotTicket;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单视图对象 iot_ticket
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotTicket.class)
public class IotTicketVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 工单编号
     */
    private String ticketNo;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 工单内容
     */
    private String content;

    /**
     * 工单类型（normal/rule/flow）
     */
    private String ticketType;

    /**
     * 工单分类
     */
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
     * 来源
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
     * 规则编码
     */
    private String ruleCode;

    /**
     * 是否被规则引擎自动处理（0否 1是）
     */
    private String autoProcessed;

    /**
     * 流程定义编码
     */
    private String flowCode;

    /**
     * 流程实例ID
     */
    private Long processInstanceId;

    /**
     * 流程业务状态
     */
    private String businessStatus;

    /**
     * 期望完成时间
     */
    private LocalDateTime expectedTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
