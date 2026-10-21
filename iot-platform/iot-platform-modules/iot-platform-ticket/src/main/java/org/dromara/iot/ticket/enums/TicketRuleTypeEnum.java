package org.dromara.iot.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单规则类型（规则引擎处理动作）
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum TicketRuleTypeEnum {

    /**
     * 自动分配处理人（targetValue 为处理人用户ID）
     */
    ASSIGN("assign", "自动分配"),

    /**
     * 调整优先级（targetValue 为优先级编码）
     */
    PRIORITY("priority", "调整优先级"),

    /**
     * 自动关闭工单
     */
    CLOSE("close", "自动关闭");

    private final String code;

    private final String desc;

    public static TicketRuleTypeEnum fromCode(String code) {
        for (TicketRuleTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
