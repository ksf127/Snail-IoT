package org.dromara.iot.cloud.provider.impl;

import org.dromara.common.core.utils.StringUtils;
import org.dromara.iot.cloud.domain.IotCloudAccount;
import org.dromara.iot.cloud.domain.IotCloudResource;
import org.dromara.iot.cloud.domain.bo.IotCloudResourceBo;
import org.dromara.iot.cloud.enums.CloudProviderEnum;
import org.dromara.iot.cloud.enums.CloudResourceTypeEnum;
import org.dromara.iot.cloud.provider.ICloudProvider;
import org.dromara.iot.cloud.provider.model.CloudConnectionResult;
import org.dromara.iot.cloud.provider.model.CloudRegionDTO;
import org.dromara.iot.cloud.provider.model.CloudResourceDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内置模拟云平台
 * <p>
 * 本地自洽实现，不依赖任何真实凭证与网络，用于功能验证与联调。
 *
 * @author iot team
 */
@Component
public class MockCloudProvider implements ICloudProvider {

    private static final String DEFAULT_REGION = "cn-hangzhou";

    @Override
    public String providerCode() {
        return CloudProviderEnum.MOCK.getCode();
    }

    @Override
    public CloudConnectionResult testConnection(IotCloudAccount account) {
        long start = System.currentTimeMillis();
        CloudConnectionResult result = new CloudConnectionResult();
        result.setSuccess(true);
        result.setMessage("内置模拟云平台连通正常（无需真实凭证）");
        result.setCostMs(System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public List<CloudRegionDTO> listRegions(IotCloudAccount account) {
        List<CloudRegionDTO> regions = new ArrayList<>();
        regions.add(region("cn-hangzhou", "华东1（杭州）"));
        regions.add(region("cn-beijing", "华北2（北京）"));
        regions.add(region("cn-shenzhen", "华南1（深圳）"));
        return regions;
    }

    @Override
    public List<CloudResourceDTO> listResources(IotCloudAccount account, IotCloudResourceBo query) {
        List<CloudResourceDTO> list = new ArrayList<>();
        for (int index = 1; index <= 3; index++) {
            list.add(build(account, index));
        }
        return filter(list, query);
    }

    @Override
    public CloudResourceDTO createResource(IotCloudAccount account, IotCloudResourceBo bo) {
        CloudResourceDTO dto = new CloudResourceDTO();
        dto.setResourceType(StringUtils.isNotBlank(bo.getResourceType())
            ? bo.getResourceType() : CloudResourceTypeEnum.ECS.getCode());
        dto.setInstanceId("i-mock-new-" + System.currentTimeMillis());
        dto.setInstanceName(StringUtils.isNotBlank(bo.getInstanceName()) ? bo.getInstanceName() : "mock-ecs-new");
        dto.setRegionId(StringUtils.isNotBlank(bo.getRegionId()) ? bo.getRegionId() : resolveRegion(account));
        dto.setStatus("Running");
        dto.setSpec(StringUtils.isNotBlank(bo.getSpec()) ? bo.getSpec() : "ecs.g6.large");
        dto.setPrivateIp("10.0.0.100");
        dto.setPublicIp("47.98.1.100");
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("provider", "mock");
        meta.put("createBy", "mock-provider");
        dto.setBizMeta(meta);
        return dto;
    }

    @Override
    public boolean deleteResource(IotCloudAccount account, IotCloudResource resource) {
        return true;
    }

    @Override
    public boolean startResource(IotCloudAccount account, IotCloudResource resource) {
        return true;
    }

    @Override
    public boolean stopResource(IotCloudAccount account, IotCloudResource resource) {
        return true;
    }

    /**
     * 构造一个模拟 ECS 实例
     */
    private CloudResourceDTO build(IotCloudAccount account, int index) {
        CloudResourceDTO dto = new CloudResourceDTO();
        dto.setResourceType(CloudResourceTypeEnum.ECS.getCode());
        dto.setInstanceId("i-mock-" + account.getAccountId() + "-" + String.format("%02d", index));
        dto.setInstanceName("mock-ecs-" + index);
        dto.setRegionId(resolveRegion(account));
        dto.setStatus(index == 2 ? "Stopped" : "Running");
        dto.setSpec(index == 3 ? "ecs.g6.xlarge" : "ecs.g6.large");
        dto.setPrivateIp("10.0.0." + (10 + index));
        dto.setPublicIp("47.98.1." + (10 + index));
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("provider", "mock");
        meta.put("zoneId", dto.getRegionId() + "-b");
        meta.put("osName", "Alibaba Cloud Linux 3");
        dto.setBizMeta(meta);
        return dto;
    }

    private String resolveRegion(IotCloudAccount account) {
        return StringUtils.isNotBlank(account.getRegionId()) ? account.getRegionId() : DEFAULT_REGION;
    }

    private CloudRegionDTO region(String regionId, String regionName) {
        CloudRegionDTO dto = new CloudRegionDTO();
        dto.setRegionId(regionId);
        dto.setRegionName(regionName);
        return dto;
    }

    private List<CloudResourceDTO> filter(List<CloudResourceDTO> list, IotCloudResourceBo query) {
        if (query == null) {
            return list;
        }
        return list.stream()
            .filter(r -> StringUtils.isBlank(query.getResourceType()) || query.getResourceType().equals(r.getResourceType()))
            .filter(r -> StringUtils.isBlank(query.getStatus()) || query.getStatus().equals(r.getStatus()))
            .filter(r -> StringUtils.isBlank(query.getKeyword())
                || (r.getInstanceId() != null && r.getInstanceId().contains(query.getKeyword()))
                || (r.getInstanceName() != null && r.getInstanceName().contains(query.getKeyword())))
            .toList();
    }

}
