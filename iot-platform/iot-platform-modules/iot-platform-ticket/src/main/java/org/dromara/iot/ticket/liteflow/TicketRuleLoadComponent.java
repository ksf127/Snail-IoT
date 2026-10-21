package org.dromara.iot.ticket.liteflow;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import org.dromara.iot.ticket.domain.IotTicketRule;
import org.dromara.iot.ticket.mapper.IotTicketRuleMapper;

import java.util.List;

/**
 * 规则链加载节点：加载启用的规则并按排序号升序排列。
 *
 * @author iot team
 */
@RequiredArgsConstructor
@LiteflowComponent("ticketRuleLoad")
public class TicketRuleLoadComponent extends NodeComponent {

    private final IotTicketRuleMapper ruleMapper;

    @Override
    public void process() {
        TicketRuleContext context = getContextBean(TicketRuleContext.class);
        List<IotTicketRule> rules = ruleMapper.selectList(Wrappers.<IotTicketRule>lambdaQuery()
            .eq(IotTicketRule::getStatus, "0")
            .orderByAsc(IotTicketRule::getSortNo));
        context.setEnabledRules(rules);
    }

}
