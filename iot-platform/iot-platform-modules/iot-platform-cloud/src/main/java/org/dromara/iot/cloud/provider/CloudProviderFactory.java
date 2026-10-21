package org.dromara.iot.cloud.provider;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 云平台 Provider 工厂
 * <p>
 * 汇集所有 {@link ICloudProvider} 实现，按平台标识路由。
 *
 * @author iot team
 */
@Component
public class CloudProviderFactory {

    private final Map<String, ICloudProvider> providerMap = new HashMap<>();

    public CloudProviderFactory(List<ICloudProvider> providers) {
        for (ICloudProvider provider : providers) {
            providerMap.put(provider.providerCode(), provider);
        }
    }

    /**
     * 按平台标识获取 Provider
     */
    public ICloudProvider getProvider(String providerCode) {
        ICloudProvider provider = providerMap.get(providerCode);
        if (provider == null) {
            throw new ServiceException(StringUtils.format("不支持的云平台：{}", providerCode));
        }
        return provider;
    }

    /**
     * 已接入的平台标识集合
     */
    public Set<String> providerCodes() {
        return providerMap.keySet();
    }

}
