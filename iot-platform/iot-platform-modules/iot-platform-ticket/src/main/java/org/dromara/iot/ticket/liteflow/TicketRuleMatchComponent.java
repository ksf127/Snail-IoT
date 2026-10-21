package org.dromara.iot.ticket.liteflow;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.iot.ticket.domain.IotTicket;
import org.dromara.iot.ticket.domain.IotTicketRule;

import java.util.Arrays;
import java.util.Objects;

/**
 * 规则链匹配节点：逐条评估启用规则，收集命中的规则。
 *
 * @author iot team
 */
@LiteflowComponent("ticketRuleMatch")
public class TicketRuleMatchComponent extends NodeComponent {

    @Override
    public void process() {
        TicketRuleContext context = getContextBean(TicketRuleContext.class);
        for (IotTicketRule rule : context.getEnabledRules()) {
            if (matches(context.getTicket(), rule)) {
                context.getMatchedRules().add(rule);
                if (context.getMatchedRuleCode() == null) {
                    context.setMatchedRuleCode(rule.getRuleCode());
                }
            }
        }
    }

    /**
     * 判断工单是否满足规则条件。
     */
    private boolean matches(IotTicket ticket, IotTicketRule rule) {
        String fieldValue = resolveField(ticket, rule.getMatchField());
        if (fieldValue == null) {
            return false;
        }
        String expected = rule.getMatchValue();
        String operator = rule.getMatchOperator();
        if (StringUtils.isBlank(operator)) {
            return false;
        }
        return switch (operator) {
            case "eq" -> Objects.equals(fieldValue, expected);
            case "ne" -> !Objects.equals(fieldValue, expected);
            case "contains" -> StringUtils.isNotBlank(expected) && fieldValue.contains(expected);
            case "in" -> StringUtils.isNotBlank(expected)
                && Arrays.stream(expected.split(",")).map(String::trim).anyMatch(fieldValue::equals);
            default -> false;
        };
    }

    /**
     * 按规则匹配字段名解析工单字段值。
     */
    private String resolveField(IotTicket ticket, String matchField) {
        if (StringUtils.isBlank(matchField)) {
            return null;
        }
        return switch (matchField) {
            case "category" -> ticket.getCategory();
            case "ticketType" -> ticket.getTicketType();
            case "priority" -> ticket.getPriority();
            case "source" -> ticket.getSource();
            case "title" -> ticket.getTitle();
            default -> null;
        };
    }

}
