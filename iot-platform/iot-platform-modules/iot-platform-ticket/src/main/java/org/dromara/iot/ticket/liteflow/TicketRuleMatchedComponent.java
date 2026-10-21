package org.dromara.iot.ticket.liteflow;

import cn.hutool.core.collection.CollUtil;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeBooleanComponent;

/**
 * 规则链条件节点：判断是否存在命中规则。
 *
 * @author iot team
 */
@LiteflowComponent("ticketRuleMatched")
public class TicketRuleMatchedComponent extends NodeBooleanComponent {

    @Override
    public boolean processBoolean() {
        TicketRuleContext context = getContextBean(TicketRuleContext.class);
        return CollUtil.isNotEmpty(context.getMatchedRules());
    }

}
