package org.dromara.iot.cloud.provider;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.iot.cloud.domain.IotCloudAccount;
import org.dromara.iot.cloud.domain.IotCloudResource;
import org.dromara.iot.cloud.domain.bo.IotCloudResourceBo;
import org.dromara.iot.cloud.provider.model.CloudConnectionResult;
import org.dromara.iot.cloud.provider.model.CloudRegionDTO;
import org.dromara.iot.cloud.provider.model.CloudResourceDTO;

import java.util.List;

/**
 * 云平台纳管抽象
 * <p>
 * 各云平台以 Provider 形式接入，向上层屏蔽具体云厂商差异：
 * 上层服务只依赖本接口，通过 {@link CloudProviderFactory} 按 {@link #providerCode()} 路由。
 *
 * @author iot team
 */
public interface ICloudProvider {

    /**
     * 平台标识（与 {@code CloudProviderEnum.code} 对应）
     */
    String providerCode();

    /**
     * 连通性检测（校验凭证与端点是否可用）
     */
    CloudConnectionResult testConnection(IotCloudAccount account);

    /**
     * 查询地域列表
     */
    List<CloudRegionDTO> listRegions(IotCloudAccount account);

    /**
     * 查询（同步）纳管资源清单
     *
     * @param account 云账号
     * @param query   过滤条件（关键字/状态/资源类型），可为空
     */
    List<CloudResourceDTO> listResources(IotCloudAccount account, IotCloudResourceBo query);

    /**
     * 创建资源（默认不支持）
     */
    default CloudResourceDTO createResource(IotCloudAccount account, IotCloudResourceBo bo) {
        throw new ServiceException(StringUtils.format("云平台[{}]不支持创建资源", providerCode()));
    }

    /**
     * 删除/释放资源（默认不支持）
     */
    default boolean deleteResource(IotCloudAccount account, IotCloudResource resource) {
        throw new ServiceException(StringUtils.format("云平台[{}]不支持删除资源", providerCode()));
    }

    /**
     * 启动资源（默认不支持）
     */
    default boolean startResource(IotCloudAccount account, IotCloudResource resource) {
        throw new ServiceException(StringUtils.format("云平台[{}]不支持启动资源", providerCode()));
    }

    /**
     * 停止资源（默认不支持）
     */
    default boolean stopResource(IotCloudAccount account, IotCloudResource resource) {
        throw new ServiceException(StringUtils.format("云平台[{}]不支持停止资源", providerCode()));
    }

}
