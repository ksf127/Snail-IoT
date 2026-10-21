package org.dromara.iot.ticket.liteflow;

import lombok.Data;
import org.dromara.iot.ticket.domain.IotTicket;
import org.dromara.iot.ticket.domain.IotTicketRule;

import java.util.ArrayList;
import java.util.List;

/**
 * 工单规则引擎（LiteFlow）上下文
 * <p>
 * 承载待处理工单、启用的规则集合与匹配结果，供规则链各节点读写。
 *
 * @author iot team
 */
@Data
public class TicketRuleContext {

    /**
     * 待处理工单
     */
    private IotTicket ticket;

    /**
     * 启用的规则（按 sortNo 升序）
     */
    private List<IotTicketRule> enabledRules = new ArrayList<>();

    /**
     * 命中的规则
     */
    private List<IotTicketRule> matchedRules = new ArrayList<>();

    /**
     * 首个命中的规则编码
     */
    private String matchedRuleCode;

    /**
     * 是否发生规则自动处理
     */
    private boolean autoProcessed;

    public TicketRuleContext(IotTicket ticket) {
        this.ticket = ticket;
    }

}
