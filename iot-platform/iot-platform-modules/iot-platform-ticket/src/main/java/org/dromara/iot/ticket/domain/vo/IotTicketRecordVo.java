package org.dromara.iot.ticket.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.ticket.domain.IotTicketRecord;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单流转记录视图对象 iot_ticket_record
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotTicketRecord.class)
public class IotTicketRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 动作
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
     * 流程任务ID
     */
    private Long processTaskId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
