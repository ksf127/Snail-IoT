package org.dromara.iot.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单状态
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum TicketStatusEnum {

    /**
     * 待受理
     */
    PENDING("0", "待受理"),

    /**
     * 处理中
     */
    PROCESSING("1", "处理中"),

    /**
     * 已完成
     */
    FINISHED("2", "已完成"),

    /**
     * 已关闭
     */
    CLOSED("3", "已关闭"),

    /**
     * 已驳回
     */
    REJECTED("4", "已驳回");

    private final String code;

    private final String desc;

    public static TicketStatusEnum fromCode(String code) {
        for (TicketStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
