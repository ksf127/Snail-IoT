package org.dromara.iot.pay.channel;

import org.dromara.iot.pay.channel.model.PayNotifyResult;
import org.dromara.iot.pay.channel.model.PrePayResult;
import org.dromara.iot.pay.channel.model.RefundResult;
import org.dromara.iot.pay.domain.IotPayChannelConfig;
import org.dromara.iot.pay.domain.IotPayOrder;
import org.dromara.iot.pay.domain.IotPayRefund;
import org.dromara.iot.pay.enums.PayChannelEnum;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 模拟支付渠道
 * <p>
 * 本地自洽实现，无需真实商户号：预下单直接返回可识别的二维码串，
 * 回调按 {@code mockStatus=SUCCESS} 视为支付成功，退款恒成功。
 *
 * @author iot team
 */
@Component
public class MockPayChannel implements IPayChannel {

    /**
     * 回调参数：支付结果，SUCCESS 视为成功
     */
    public static final String PARAM_STATUS = "mockStatus";

    /**
     * 回调参数：平台支付单号
     */
    public static final String PARAM_ORDER_NO = "orderNo";

    @Override
    public String channelCode() {
        return PayChannelEnum.MOCK.getCode();
    }

    @Override
    public PrePayResult prePay(IotPayOrder order, IotPayChannelConfig config) {
        String prePayId = "MOCK_" + order.getOrderNo();
        String codeUrl = "mock://pay/" + order.getOrderNo();
        return PrePayResult.builder()
            .prePayId(prePayId)
            .codeUrl(codeUrl)
            .rawData("{\"prePayId\":\"" + prePayId + "\",\"codeUrl\":\"" + codeUrl + "\"}")
            .build();
    }

    @Override
    public PayNotifyResult parseNotify(IotPayChannelConfig config, Map<String, String> params, String body) {
        String status = params == null ? null : params.get(PARAM_STATUS);
        boolean paid = "SUCCESS".equalsIgnoreCase(status);
        String orderNo = params == null ? null : params.get(PARAM_ORDER_NO);
        return PayNotifyResult.builder()
            .paid(paid)
            .orderNo(orderNo)
            .tradeNo("MOCK_TRADE_" + orderNo)
            .rawData(body)
            .build();
    }

    @Override
    public RefundResult refund(IotPayOrder order, IotPayRefund refund, IotPayChannelConfig config) {
        return RefundResult.builder()
            .success(true)
            .rawData("{\"refundNo\":\"" + refund.getRefundNo() + "\",\"result\":\"SUCCESS\"}")
            .build();
    }

}
