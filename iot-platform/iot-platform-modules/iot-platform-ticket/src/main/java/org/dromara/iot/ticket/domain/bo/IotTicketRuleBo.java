package org.dromara.iot.ticket.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.ticket.domain.IotTicketRule;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 工单规则业务对象 iot_ticket_rule
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotTicketRule.class, reverseConvertGenerate = false)
public class IotTicketRuleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则编码（唯一）
     */
    @NotBlank(message = "规则编码不能为空")
    @Size(max = 64, message = "规则编码长度不能超过 64 个字符")
    private String ruleCode;

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 128, message = "规则名称长度不能超过 128 个字符")
    private String ruleName;

    /**
     * 规则类型（assign自动分配 / priority调整优先级 / close自动关闭）
     */
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    /**
     * 匹配字段（category/ticketType/priority/source/title）
     */
    @NotBlank(message = "匹配字段不能为空")
    private String matchField;

    /**
     * 匹配操作（eq/ne/contains/in）
     */
    @NotBlank(message = "匹配操作不能为空")
    private String matchOperator;

    /**
     * 匹配值
     */
    @Size(max = 255, message = "匹配值长度不能超过 255 个字符")
    private String matchValue;

    /**
     * 目标值
     */
    @Size(max = 255, message = "目标值长度不能超过 255 个字符")
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
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
