package org.dromara.iot.ticket.liteflow;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.iot.ticket.domain.IotTicket;
import org.dromara.iot.ticket.domain.IotTicketRule;
import org.dromara.iot.ticket.enums.TicketRuleTypeEnum;
import org.dromara.iot.ticket.enums.TicketStatusEnum;
import org.dromara.system.api.UserService;

import java.time.LocalDateTime;

/**
 * 规则链应用节点：将命中的规则动作落到工单上（自动分配 / 调整优先级 / 自动关闭）。
 *
 * @author iot team
 */
@Slf4j
@RequiredArgsConstructor
@LiteflowComponent("ticketRuleApply")
public class TicketRuleApplyComponent extends NodeComponent {

    private final UserService userService;

    @Override
    public void process() {
        TicketRuleContext context = getContextBean(TicketRuleContext.class);
        IotTicket ticket = context.getTicket();
        boolean assigned = false;
        boolean closed = false;
        for (IotTicketRule rule : context.getMatchedRules()) {
            TicketRuleTypeEnum type = TicketRuleTypeEnum.fromCode(rule.getRuleType());
            if (type == null) {
                continue;
            }
            switch (type) {
                case ASSIGN -> {
                    Long handlerId = parseUserId(rule.getTargetValue());
                    if (handlerId != null) {
                        ticket.setHandlerId(handlerId);
                        ticket.setHandlerName(userService.selectNicknameById(handlerId));
                        assigned = true;
                    }
                }
                case PRIORITY -> ticket.setPriority(rule.getTargetValue());
                case CLOSE -> closed = true;
            }
        }
        if (closed) {
            ticket.setStatus(TicketStatusEnum.CLOSED.getCode());
            ticket.setCloseTime(LocalDateTime.now());
        } else if (assigned && TicketStatusEnum.PENDING.getCode().equals(ticket.getStatus())) {
            ticket.setStatus(TicketStatusEnum.PROCESSING.getCode());
        }
        ticket.setRuleCode(context.getMatchedRuleCode());
        ticket.setAutoProcessed("1");
        context.setAutoProcessed(true);
    }

    /**
     * 解析规则目标值中的用户ID。
     */
    private Long parseUserId(String targetValue) {
        if (StringUtils.isBlank(targetValue)) {
            return null;
        }
        try {
            return Long.valueOf(targetValue.trim());
        } catch (NumberFormatException e) {
            log.warn("工单规则自动分配目标值不是合法用户ID：{}", targetValue);
            return null;
        }
    }

}
