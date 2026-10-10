package org.dromara.iot.pay.channel;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付渠道工厂
 * <p>
 * 汇集所有 {@link IPayChannel} 实现，按渠道标识路由。
 *
 * @author iot team
 */
@Component
public class PayChannelFactory {

    private final Map<String, IPayChannel> channelMap = new HashMap<>();

    public PayChannelFactory(List<IPayChannel> channels) {
        for (IPayChannel channel : channels) {
            channelMap.put(channel.channelCode(), channel);
        }
    }

    /**
     * 按渠道标识获取渠道实现
     */
    public IPayChannel getChannel(String channelCode) {
        IPayChannel channel = channelMap.get(channelCode);
        if (channel == null) {
            throw new ServiceException(StringUtils.format("不支持的支付渠道：{}", channelCode));
        }
        return channel;
    }

}
