package org.dromara.iot.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.pay.domain.bo.IotPayChannelConfigBo;
import org.dromara.iot.pay.domain.bo.IotPayOrderBo;
import org.dromara.iot.pay.domain.bo.IotPayRefundBo;
import org.dromara.iot.pay.domain.vo.IotPayChannelConfigVo;
import org.dromara.iot.pay.domain.vo.IotPayOrderVo;
import org.dromara.iot.pay.domain.vo.IotPayRefundVo;
import org.dromara.iot.pay.service.IotPayService;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聚合支付
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/pay")
public class PayController extends BaseController {

    private final IotPayService payService;

    /**
     * 分页查询支付单
     */
    @SaCheckPermission("iot:pay:list")
    @GetMapping("/list")
    public R<PageResult<IotPayOrderVo>> list(IotPayOrderBo bo, PageQuery pageQuery) {
        return R.ok(payService.queryPageList(bo, pageQuery));
    }

    /**
     * 分页查询退款单
     */
    @SaCheckPermission("iot:pay:list")
    @GetMapping("/refund/list")
    public R<PageResult<IotPayRefundVo>> refundList(IotPayRefundBo bo, PageQuery pageQuery) {
        return R.ok(payService.queryRefundPageList(bo, pageQuery));
    }

    /**
     * 统一下单（预下单）
     */
    @SaCheckPermission("iot:pay:create")
    @Log(title = "支付下单", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public R<IotPayOrderVo> create(@Validated @RequestBody IotPayOrderBo bo) {
        return R.ok(payService.createOrder(bo));
    }

    /**
     * 按支付单号查单
     */
    @SaCheckPermission("iot:pay:query")
    @GetMapping("/query/{orderNo}")
    public R<IotPayOrderVo> query(@PathVariable String orderNo) {
        return R.ok(payService.queryByOrderNo(orderNo));
    }

    /**
     * 发起退款
     */
    @SaCheckPermission("iot:pay:refund")
    @Log(title = "支付退款", businessType = BusinessType.UPDATE)
    @PostMapping("/refund")
    public R<IotPayRefundVo> refund(@Validated @RequestBody IotPayRefundBo bo) {
        return R.ok(payService.refund(bo));
    }

    /**
     * 查询渠道配置
     */
    @SaCheckPermission("iot:pay:config")
    @GetMapping("/channels")
    public R<List<IotPayChannelConfigVo>> channels() {
        return R.ok(payService.listChannels());
    }

    /**
     * 分页查询渠道配置
     */
    @SaCheckPermission("iot:pay:config")
    @GetMapping("/channel/page")
    public R<PageResult<IotPayChannelConfigVo>> channelPage(IotPayChannelConfigBo bo, PageQuery pageQuery) {
        return R.ok(payService.queryChannelConfigPageList(bo, pageQuery));
    }

    /**
     * 查询渠道配置详情
     */
    @SaCheckPermission("iot:pay:config")
    @GetMapping("/channel/{configId}")
    public R<IotPayChannelConfigVo> getChannelConfig(@PathVariable Long configId) {
        return R.ok(payService.queryChannelConfigById(configId));
    }

    /**
     * 新增渠道配置
     */
    @SaCheckPermission("iot:pay:config")
    @Log(title = "渠道配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/channel")
    public R<Void> addChannelConfig(@Validated @RequestBody IotPayChannelConfigBo bo) {
        return toAjax(payService.createChannelConfig(bo));
    }

    /**
     * 修改渠道配置
     */
    @SaCheckPermission("iot:pay:config")
    @Log(title = "渠道配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/channel")
    public R<Void> updateChannelConfig(@Validated @RequestBody IotPayChannelConfigBo bo) {
        return toAjax(payService.updateChannelConfig(bo));
    }

    /**
     * 删除渠道配置
     */
    @SaCheckPermission("iot:pay:config")
    @Log(title = "渠道配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/channel/{configIds}")
    public R<Void> removeChannelConfig(@PathVariable Long[] configIds) {
        return toAjax(payService.deleteChannelConfig(List.of(configIds)));
    }

    /**
     * 渠道异步回调（免鉴权，由渠道服务器调用）
     * <p>
     * 同时兼容表单回调（如支付宝，参数在 {@code params}）与 JSON 报文回调（如微信 V3，原文在 {@code body}）；
     * 应答内容直接返回给渠道（如 "success"）。
     */
    @PostMapping("/notify/{channel}")
    public String notify(@PathVariable String channel,
                         @RequestParam(required = false) Map<String, String> params,
                         @RequestBody(required = false) String body) {
        if (StringUtils.isBlank(body)) {
            body = params == null ? "" : params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        }
        return payService.handleNotify(channel, params, body);
    }

}
