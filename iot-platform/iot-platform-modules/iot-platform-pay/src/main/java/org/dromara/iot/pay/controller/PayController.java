package org.dromara.iot.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.pay.domain.bo.IotPayOrderBo;
import org.dromara.iot.pay.domain.bo.IotPayRefundBo;
import org.dromara.iot.pay.domain.vo.IotPayChannelConfigVo;
import org.dromara.iot.pay.domain.vo.IotPayOrderVo;
import org.dromara.iot.pay.domain.vo.IotPayRefundVo;
import org.dromara.iot.pay.service.IotPayService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
public class PayController {

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
     * 渠道异步回调（免鉴权，由渠道服务器调用）
     * <p>
     * 兼容表单方式回调；应答内容直接返回给渠道（如 "success"）。
     */
    @PostMapping("/notify/{channel}")
    public String notify(@PathVariable String channel, @RequestParam Map<String, String> params) {
        String body = params.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));
        return payService.handleNotify(channel, params, body);
    }

}
