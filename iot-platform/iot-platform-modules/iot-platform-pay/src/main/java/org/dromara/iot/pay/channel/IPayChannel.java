package org.dromara.iot.pay.channel;

import org.dromara.iot.pay.channel.model.PayNotifyResult;
import org.dromara.iot.pay.channel.model.PrePayResult;
import org.dromara.iot.pay.channel.model.RefundResult;
import org.dromara.iot.pay.domain.IotPayChannelConfig;
import org.dromara.iot.pay.domain.IotPayOrder;
import org.dromara.iot.pay.domain.IotPayRefund;

import java.util.Map;

/**
 * 支付渠道抽象
 * <p>
 * 各渠道实现统一的预下单、回调解析、退款能力；上层服务只依赖本接口。
 *
 * @author iot team
 */
public interface IPayChannel {

    /**
     * 渠道标识（与 {@code PayChannelEnum.code} 对应）
     */
    String channelCode();

    /**
     * 预下单，返回预支付ID与二维码链接
     *
     * @param order  支付订单
     * @param config 渠道配置
     */
    PrePayResult prePay(IotPayOrder order, IotPayChannelConfig config);

    /**
     * 校验并解析异步回调
     *
     * @param config 渠道配置
     * @param params 表单参数
     * @param body   原始报文
     */
    PayNotifyResult parseNotify(IotPayChannelConfig config, Map<String, String> params, String body);

    /**
     * 发起退款
     *
     * @param order  原支付订单
     * @param refund 退款单
     * @param config 渠道配置
     */
    RefundResult refund(IotPayOrder order, IotPayRefund refund, IotPayChannelConfig config);

}
