package org.dromara.iot.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单优先级
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum TicketPriorityEnum {

    /**
     * 低
     */
    LOW("0", "低"),

    /**
     * 中
     */
    MEDIUM("1", "中"),

    /**
     * 高
     */
    HIGH("2", "高"),

    /**
     * 紧急
     */
    URGENT("3", "紧急");

    private final String code;

    private final String desc;

    public static TicketPriorityEnum fromCode(String code) {
        for (TicketPriorityEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
