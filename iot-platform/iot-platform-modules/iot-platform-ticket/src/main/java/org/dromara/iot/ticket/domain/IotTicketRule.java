package org.dromara.iot.ticket.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 工单规则对象 iot_ticket_rule
 * <p>
 * 规则引擎（LiteFlow）的规则数据源：按匹配字段与匹配值筛选工单，
 * 命中后执行对应动作（自动分配 / 调整优先级 / 自动关闭）。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_ticket_rule")
public class IotTicketRule extends BaseEntity {

    /**
     * 规则ID
     */
    @TableId(value = "rule_id", type = IdType.ASSIGN_ID)
    private Long ruleId;

    /**
     * 规则编码（唯一）
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型（assign自动分配 / priority调整优先级 / close自动关闭）
     */
    private String ruleType;

    /**
     * 匹配字段（category/ticketType/priority/source/title）
     */
    private String matchField;

    /**
     * 匹配操作（eq/ne/contains/in）
     */
    private String matchOperator;

    /**
     * 匹配值（in 以英文逗号分隔）
     */
    private String matchValue;

    /**
     * 目标值（assign 为处理人用户ID，priority 为优先级编码）
     */
    private String targetValue;

    /**
     * 状态（0启用 1停用）
     */
    private String status;

    /**
     * 排序号（越小越先匹配）
     */
    private Integer sortNo;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
