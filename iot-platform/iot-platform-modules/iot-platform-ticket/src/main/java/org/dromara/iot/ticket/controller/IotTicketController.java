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
import org.dromara.iot.ticket.domain.bo.IotTicketBo;
import org.dromara.iot.ticket.domain.bo.IotTicketHandleBo;
import org.dromara.iot.ticket.domain.bo.IotTicketRecordBo;
import org.dromara.iot.ticket.domain.vo.IotTicketRecordVo;
import org.dromara.iot.ticket.domain.vo.IotTicketVo;
import org.dromara.iot.ticket.service.IotTicketService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工单管理（普通工单 + 规则引擎 + 流程引擎）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/ticket")
public class IotTicketController extends BaseController {

    private final IotTicketService ticketService;

    /**
     * 分页查询工单
     */
    @SaCheckPermission("iot:ticket:list")
    @GetMapping("/list")
    public R<PageResult<IotTicketVo>> list(IotTicketBo bo, PageQuery pageQuery) {
        return R.ok(ticketService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询工单流转记录
     */
    @SaCheckPermission("iot:ticket:list")
    @GetMapping("/records")
    public R<List<IotTicketRecordVo>> records(IotTicketRecordBo bo) {
        return R.ok(ticketService.listRecords(bo));
    }

    /**
     * 查询工单详情
     */
    @SaCheckPermission("iot:ticket:query")
    @GetMapping("/{ticketId}")
    public R<IotTicketVo> getInfo(@PathVariable Long ticketId) {
        return R.ok(ticketService.queryById(ticketId));
    }

    /**
     * 新建工单
     */
    @SaCheckPermission("iot:ticket:add")
    @Log(title = "工单管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotTicketBo bo) {
        return toAjax(ticketService.createTicket(bo));
    }

    /**
     * 修改工单
     */
    @SaCheckPermission("iot:ticket:edit")
    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotTicketBo bo) {
        return toAjax(ticketService.updateTicket(bo));
    }

    /**
     * 删除工单
     */
    @SaCheckPermission("iot:ticket:remove")
    @Log(title = "工单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ticketIds}")
    public R<Void> remove(@PathVariable Long[] ticketIds) {
        return toAjax(ticketService.deleteTicket(List.of(ticketIds)));
    }

    /**
     * 提交工单（规则工单触发规则引擎、流程工单发起流程）
     */
    @SaCheckPermission("iot:ticket:handle")
    @Log(title = "工单提交", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/submit/{ticketId}")
    public R<Void> submit(@PathVariable Long ticketId,
                          @RequestParam(required = false) String flowCode,
                          @RequestParam(required = false) String handler) {
        return toAjax(ticketService.submitTicket(ticketId, flowCode, handler));
    }

    /**
     * 执行（或重跑）规则引擎
     */
    @SaCheckPermission("iot:ticket:handle")
    @Log(title = "工单规则引擎", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/rule/{ticketId}")
    public R<Void> runRule(@PathVariable Long ticketId) {
        return toAjax(ticketService.runRuleEngine(ticketId));
    }

    /**
     * 人工办理工单
     */
    @SaCheckPermission("iot:ticket:handle")
    @Log(title = "工单办理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/handle")
    public R<Void> handle(@Validated @RequestBody IotTicketHandleBo bo) {
        return toAjax(ticketService.handleTicket(bo));
    }

    /**
     * 办理流程任务
     */
    @SaCheckPermission("iot:ticket:handle")
    @Log(title = "流程办理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/flow/complete")
    public R<Void> completeFlow(@Validated @RequestBody IotTicketHandleBo bo) {
        return toAjax(ticketService.completeFlowTask(bo));
    }

    /**
     * 同步流程状态回写工单
     */
    @SaCheckPermission("iot:ticket:handle")
    @Log(title = "流程状态同步", businessType = BusinessType.UPDATE)
    @PostMapping("/flow/sync/{ticketId}")
    public R<IotTicketVo> syncFlow(@PathVariable Long ticketId) {
        return R.ok(ticketService.syncFlowStatus(ticketId));
    }

}
