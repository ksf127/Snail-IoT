package org.dromara.iot.pay.channel;

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
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
 * 支付宝当面付（扫码）渠道
 * <p>
 * 基于 IJPay 的 {@code AliPayApi} 实现预下单、回调验签、退款。
 * <p>
 * 渠道配置字段映射（复用 {@code iot_pay_channel_config} 现有列）：
 * <ul>
 *   <li>{@code appId} —— 支付宝应用 AppId</li>
 *   <li>{@code privateKey} —— 应用私钥（PEM 内容，非路径）</li>
 *   <li>{@code publicKey} —— 支付宝公钥（回调验签用）</li>
 *   <li>{@code notifyUrl} —— 支付异步回调地址</li>
 *   <li>{@code gatewayUrl} —— 网关地址（可选，为空用生产网关；沙箱填 https://openapi-sandbox.dl.alipaydev.com/gateway.do）</li>
 * </ul>
 * <p>
 * 回调安全性：使用支付宝公钥对回调表单参数做 RSA2 验签，验签失败视为非法请求。
 *
 * @author iot team
 */
@Slf4j
@Component
public class AliQrPayChannel implements IPayChannel {

    /**
     * 支付宝生产网关
     */
    private static final String DEFAULT_SERVICE_URL = "https://openapi.alipay.com/gateway.do";

    /**
     * 字符集
     */
    private static final String CHARSET = "UTF-8";

    /**
     * 签名算法
     */
    private static final String SIGN_TYPE = "RSA2";

    @Override
    public String channelCode() {
        return PayChannelEnum.ALI_QR.getCode();
    }

    @Override
    public PrePayResult prePay(IotPayOrder order, IotPayChannelConfig config) {
        initConfig(config);
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(order.getOrderNo());
        model.setTotalAmount(order.getAmount().toPlainString());
        model.setSubject(StringUtils.blankToDefault(order.getSubject(), order.getOrderNo()));
        AlipayTradePrecreateResponse response;
        try {
            response = AliPayApi.tradePrecreatePayToResponse(model, config.getNotifyUrl());
        } catch (AlipayApiException e) {
            throw new ServiceException("支付宝下单失败：" + e.getMessage());
        }
        if (!response.isSuccess()) {
            throw new ServiceException("支付宝下单失败：" + response.getSubMsg());
        }
        return PrePayResult.builder()
            .prePayId(order.getOrderNo())
            .codeUrl(response.getQrCode())
            .rawData(response.getBody())
            .build();
    }

    @Override
    public PayNotifyResult parseNotify(IotPayChannelConfig config, Map<String, String> params, String body) {
        if (params == null || params.isEmpty()) {
            return PayNotifyResult.builder().paid(false).rawData(body).build();
        }
        try {
            if (!AlipaySignature.rsaCheckV1(params, config.getPublicKey(), CHARSET, SIGN_TYPE)) {
                log.warn("支付宝回调验签失败：{}", params);
                return PayNotifyResult.builder().paid(false).rawData(body).build();
            }
        } catch (AlipayApiException e) {
            log.warn("支付宝回调验签异常：{}", e.getMessage());
            return PayNotifyResult.builder().paid(false).rawData(body).build();
        }
        String tradeStatus = params.get("trade_status");
        boolean paid = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
        return PayNotifyResult.builder()
            .paid(paid)
            .orderNo(params.get("out_trade_no"))
            .tradeNo(params.get("trade_no"))
            .rawData(body)
            .build();
    }

    @Override
    public RefundResult refund(IotPayOrder order, IotPayRefund refund, IotPayChannelConfig config) {
        initConfig(config);
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(order.getOrderNo());
        model.setRefundAmount(refund.getRefundAmount().toPlainString());
        model.setOutRequestNo(refund.getRefundNo());
        model.setRefundReason(refund.getReason());
        AlipayTradeRefundResponse response;
        try {
            response = AliPayApi.tradeRefundToResponse(model);
        } catch (AlipayApiException e) {
            throw new ServiceException("支付宝退款失败：" + e.getMessage());
        }
        boolean success = response.isSuccess() || "Y".equals(response.getFundChange());
        if (!success) {
            log.warn("支付宝退款未成功：{}", response.getSubMsg());
        }
        return RefundResult.builder()
            .success(success)
            .rawData(response.getBody())
            .build();
    }

    /**
     * 构建并装载支付宝客户端配置（线程级）
     */
    private void initConfig(IotPayChannelConfig config) {
        AliPayApiConfig apiConfig = AliPayApiConfig.builder()
            .setAppId(config.getAppId())
            .setPrivateKey(config.getPrivateKey())
            .setAliPayPublicKey(config.getPublicKey())
            .setServiceUrl(StringUtils.blankToDefault(config.getGatewayUrl(), DEFAULT_SERVICE_URL))
            .setCharset(CHARSET)
            .setSignType(SIGN_TYPE)
            .build();
        AliPayApiConfigKit.setThreadLocalAliPayApiConfig(apiConfig);
    }

}
