package org.dromara.iot.ticket.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 工单流转记录对象 iot_ticket_record
 * <p>
 * 记录工单全生命周期的每一次动作（创建/受理/规则处理/流程流转/完成/关闭等）。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_ticket_record")
public class IotTicketRecord extends BaseEntity {

    /**
     * 记录ID
     */
    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    private Long recordId;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 动作（create/accept/handle/rule/flow-start/flow-complete/finish/close/reject/comment）
     */
    private String action;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 处理意见 / 说明
     */
    private String message;

    /**
     * 流程任务ID（流程工单流转时记录）
     */
    private Long processTaskId;

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
