package org.dromara.iot.ticket.liteflow;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.dromara.common.core.exception.ServiceException;

/**
 * 规则链准备节点：校验上下文与待处理工单。
 *
 * @author iot team
 */
@LiteflowComponent("ticketRulePrepare")
public class TicketRulePrepareComponent extends NodeComponent {

    @Override
    public void process() {
        TicketRuleContext context = getContextBean(TicketRuleContext.class);
        if (context == null || context.getTicket() == null) {
            throw new ServiceException("工单规则处理上下文缺失工单信息");
        }
    }

}
