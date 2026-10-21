package org.dromara.iot.ticket.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 工单流转记录查询对象
 *
 * @author iot team
 */
@Data
public class IotTicketRecordBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 动作
     */
    private String action;

    /**
     * 操作人名称
     */
    private String operatorName;

}
