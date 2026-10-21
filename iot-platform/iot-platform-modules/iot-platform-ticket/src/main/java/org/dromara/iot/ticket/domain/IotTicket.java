package org.dromara.iot.ticket.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 工单对象 iot_ticket
 * <p>
 * 支持三类工单：普通工单（人工流转）、规则工单（LiteFlow 规则引擎自动处理）、
 * 流程工单（Warm-Flow 流程引擎审批）。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_ticket")
public class IotTicket extends BaseEntity {

    /**
     * 工单ID
     */
    @TableId(value = "ticket_id", type = IdType.ASSIGN_ID)
    private Long ticketId;

    /**
     * 工单编号（唯一）
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
     * 规则编码（规则工单命中的规则）
     */
    private String ruleCode;

    /**
     * 是否被规则引擎自动处理（0否 1是）
     */
    private String autoProcessed;

    /**
     * 流程定义编码（流程工单使用）
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
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
