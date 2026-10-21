package org.dromara.iot.ticket.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.ticket.domain.IotTicketRule;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单规则视图对象 iot_ticket_rule
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotTicketRule.class)
public class IotTicketRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型（assign/priority/close）
     */
    private String ruleType;

    /**
     * 匹配字段
     */
    private String matchField;

    /**
     * 匹配操作
     */
    private String matchOperator;

    /**
     * 匹配值
     */
    private String matchValue;

    /**
     * 目标值
     */
    private String targetValue;

    /**
     * 状态（0启用 1停用）
     */
    private String status;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
