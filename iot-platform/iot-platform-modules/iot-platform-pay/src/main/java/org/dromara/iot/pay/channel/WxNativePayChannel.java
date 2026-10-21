package org.dromara.iot.pay.channel;

import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.kit.AesUtil;
import com.ijpay.core.kit.PayKit;
import com.ijpay.wxpay.WxPayV3Api;
import com.ijpay.wxpay.WxPayV3ApiConfig;
import com.ijpay.wxpay.WxPayV3ApiConfigKit;
import com.ijpay.wxpay.model.v3.Amount;
import com.ijpay.wxpay.model.v3.RefundAmount;
import com.ijpay.wxpay.model.v3.RefundModel;
import com.ijpay.wxpay.model.v3.UnifiedOrderModel;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.iot.pay.channel.model.PayNotifyResult;
import org.dromara.iot.pay.channel.model.PrePayResult;
import org.dromara.iot.pay.channel.model.RefundResult;
import org.dromara.iot.pay.domain.IotPayChannelConfig;
import org.dromara.iot.pay.domain.IotPayOrder;
import org.dromara.iot.pay.domain.IotPayRefund;
import org.dromara.iot.pay.enums.PayChannelEnum;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Map;

/**
 * 微信 Native 扫码支付渠道（APIv3）
 * <p>
 * 基于 IJPay 的 {@code WxPayV3Api} 实现预下单、回调解密、退款。
 * <p>
 * 渠道配置字段映射（复用 {@code iot_pay_channel_config} 现有列）：
 * <ul>
 *   <li>{@code merchantId} —— 微信支付商户号 mchId</li>
 *   <li>{@code appId} —— 公众号/应用 appId</li>
 *   <li>{@code privateKey} —— 商户 API 私钥（PEM 内容，非路径）</li>
 *   <li>{@code apiKey} —— APIv3 密钥（回调解密用）</li>
 *   <li>{@code certPath} —— 商户证书序列号 serialNo</li>
 *   <li>{@code notifyUrl} —— 支付/退款异步回调地址</li>
 * </ul>
 * <p>
 * 回调安全性：微信 V3 回调报文使用 APIv3 密钥做 AES-GCM 加密，GCM 认证标签可有效校验报文来源与完整性，
 * 故无需再获取平台证书做验签。
 *
 * @author iot team
 */
@Slf4j
@Component
public class WxNativePayChannel implements IPayChannel {

    /**
     * 微信支付默认币种
     */
    private static final String DEFAULT_CURRENCY = "CNY";

    @Override
    public String channelCode() {
        return PayChannelEnum.WX_NATIVE.getCode();
    }

    @Override
    public PrePayResult prePay(IotPayOrder order, IotPayChannelConfig config) {
        WxPayV3Api api = buildApi(config);
        UnifiedOrderModel model = UnifiedOrderModel.builder()
            .appid(config.getAppId())
            .mchid(config.getMerchantId())
            .description(StringUtils.blankToDefault(order.getSubject(), order.getOrderNo()))
            .out_trade_no(order.getOrderNo())
            .notify_url(config.getNotifyUrl())
            .amount(Amount.builder()
                .total(toFen(order.getAmount()))
                .currency(currency(order))
                .build())
            .build();
        IJPayHttpResponse response;
        try {
            response = api.nativePay(model);
        } catch (Exception e) {
            throw new ServiceException("微信支付下单失败：" + e.getMessage());
        }
        if (response.getStatus() != 200) {
            throw new ServiceException("微信支付下单失败：" + response.getBody());
        }
        String codeUrl = text(JsonUtils.getJsonMapper().readTree(response.getBody()), "code_url");
        return PrePayResult.builder()
            .prePayId(order.getOrderNo())
            .codeUrl(codeUrl)
            .rawData(response.getBody())
            .build();
    }

    @Override
    public PayNotifyResult parseNotify(IotPayChannelConfig config, Map<String, String> params, String body) {
        if (StringUtils.isBlank(body)) {
            return PayNotifyResult.builder().paid(false).rawData(body).build();
        }
        try {
            JsonNode resource = JsonUtils.getJsonMapper().readTree(body).get("resource");
            if (resource == null) {
                return PayNotifyResult.builder().paid(false).rawData(body).build();
            }
            AesUtil aesUtil = new AesUtil(config.getApiKey().getBytes(StandardCharsets.UTF_8));
            String plaintext = aesUtil.decryptToString(
                text(resource, "associated_data").getBytes(StandardCharsets.UTF_8),
                text(resource, "nonce").getBytes(StandardCharsets.UTF_8),
                text(resource, "ciphertext"));
            JsonNode payNode = JsonUtils.getJsonMapper().readTree(plaintext);
            boolean paid = "SUCCESS".equals(text(payNode, "trade_state"));
            return PayNotifyResult.builder()
                .paid(paid)
                .orderNo(text(payNode, "out_trade_no"))
                .tradeNo(text(payNode, "transaction_id"))
                .rawData(body)
                .build();
        } catch (Exception e) {
            log.warn("微信支付回调解析失败：{}", e.getMessage());
            return PayNotifyResult.builder().paid(false).rawData(body).build();
        }
    }

    @Override
    public RefundResult refund(IotPayOrder order, IotPayRefund refund, IotPayChannelConfig config) {
        WxPayV3Api api = buildApi(config);
        RefundModel model = RefundModel.builder()
            .out_trade_no(order.getOrderNo())
            .out_refund_no(refund.getRefundNo())
            .reason(refund.getReason())
            .notify_url(config.getNotifyUrl())
            .amount(RefundAmount.builder()
                .total(toFen(order.getAmount()))
                .refund(toFen(refund.getRefundAmount()))
                .currency(currency(order))
                .build())
            .build();
        IJPayHttpResponse response;
        try {
            response = api.refund(model);
        } catch (Exception e) {
            throw new ServiceException("微信支付退款失败：" + e.getMessage());
        }
        String resultStatus = response.getStatus() == 200
            ? text(JsonUtils.getJsonMapper().readTree(response.getBody()), "status") : null;
        boolean success = "SUCCESS".equals(resultStatus) || "PROCESSING".equals(resultStatus);
        if (!success) {
            log.warn("微信支付退款未成功：status={}，body={}", response.getStatus(), response.getBody());
        }
        return RefundResult.builder()
            .success(success)
            .rawData(response.getBody())
            .build();
    }

    /**
     * 构建微信 V3 支付客户端
     */
    private WxPayV3Api buildApi(IotPayChannelConfig config) {
        PrivateKey privateKey;
        try {
            privateKey = PayKit.getPrivateKeyByKeyContent(config.getPrivateKey(), "RSA");
        } catch (Exception e) {
            throw new ServiceException("微信支付私钥解析失败：" + e.getMessage());
        }
        WxPayV3ApiConfig apiConfig = WxPayV3ApiConfig.builder()
            .mchId(config.getMerchantId())
            .appId(config.getAppId())
            .serialNo(config.getCertPath())
            .privateKey(privateKey)
            .build();
        WxPayV3ApiConfigKit.putApiConfig(apiConfig);
        return new WxPayV3Api(apiConfig);
    }

    /**
     * 元转分
     */
    private int toFen(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    /**
     * 币种，缺省 CNY
     */
    private String currency(IotPayOrder order) {
        return StringUtils.blankToDefault(order.getCurrency(), DEFAULT_CURRENCY);
    }

    /**
     * 读取 JSON 节点文本，缺失或 null 时返回空串
     */
    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? "" : value.asString();
    }

}
