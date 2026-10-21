package org.dromara.iot.ticket.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.ticket.domain.bo.IotTicketRuleBo;
import org.dromara.iot.ticket.domain.vo.IotTicketRuleVo;
import org.dromara.iot.ticket.service.IotTicketRuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工单规则配置（规则引擎数据源）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/ticket/rule")
public class IotTicketRuleController extends BaseController {

    private final IotTicketRuleService ruleService;

    /**
     * 分页查询工单规则
     */
    @SaCheckPermission("iot:ticket:rule:list")
    @GetMapping("/list")
    public R<PageResult<IotTicketRuleVo>> list(IotTicketRuleBo bo, PageQuery pageQuery) {
        return R.ok(ruleService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询启用的工单规则
     */
    @SaCheckPermission("iot:ticket:rule:list")
    @GetMapping("/enabled")
    public R<List<IotTicketRuleVo>> enabled() {
        return R.ok(ruleService.listEnabled());
    }

    /**
     * 查询工单规则详情
     */
    @SaCheckPermission("iot:ticket:rule:list")
    @GetMapping("/{ruleId}")
    public R<IotTicketRuleVo> getInfo(@PathVariable Long ruleId) {
        return R.ok(ruleService.queryById(ruleId));
    }

    /**
     * 新增工单规则
     */
    @SaCheckPermission("iot:ticket:rule:add")
    @Log(title = "工单规则", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotTicketRuleBo bo) {
        return toAjax(ruleService.createRule(bo));
    }

    /**
     * 修改工单规则
     */
    @SaCheckPermission("iot:ticket:rule:edit")
    @Log(title = "工单规则", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotTicketRuleBo bo) {
        return toAjax(ruleService.updateRule(bo));
    }

    /**
     * 删除工单规则
     */
    @SaCheckPermission("iot:ticket:rule:remove")
    @Log(title = "工单规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public R<Void> remove(@PathVariable Long[] ruleIds) {
        return toAjax(ruleService.deleteRule(List.of(ruleIds)));
    }

}
