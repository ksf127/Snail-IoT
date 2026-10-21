package org.dromara.iot.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单类型
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum TicketTypeEnum {

    /**
     * 普通工单：仅走人工受理与状态流转
     */
    NORMAL("normal", "普通工单"),

    /**
     * 规则工单：创建后由规则引擎（LiteFlow）自动处理（分配/定级/关闭）
     */
    RULE("rule", "规则工单"),

    /**
     * 流程工单：创建后由流程引擎（Warm-Flow）发起审批
     */
    FLOW("flow", "流程工单");

    private final String code;

    private final String desc;

    public static TicketTypeEnum fromCode(String code) {
        for (TicketTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
