package org.dromara.iot.cloud.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.iot.cloud.domain.IotCloudAccount;
import org.dromara.iot.cloud.domain.IotCloudResource;
import org.dromara.iot.cloud.domain.bo.IotCloudResourceBo;
import org.dromara.iot.cloud.enums.CloudProviderEnum;
import org.dromara.iot.cloud.enums.CloudResourceTypeEnum;
import org.dromara.iot.cloud.provider.ICloudProvider;
import org.dromara.iot.cloud.provider.model.CloudConnectionResult;
import org.dromara.iot.cloud.provider.model.CloudRegionDTO;
import org.dromara.iot.cloud.provider.model.CloudResourceDTO;
import org.dromara.iot.cloud.provider.support.CloudHttpClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 阿里云云平台纳管实现
 * <p>
 * 通过 ECS OpenAPI（RPC 风格，Version 2014-05-26）接入，使用 AccessKey 做 HMAC-SHA1 签名。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunCloudProvider implements ICloudProvider {

    /**
     * 默认接入端点
     */
    private static final String DEFAULT_ENDPOINT = "https://ecs.aliyuncs.com";

    /**
     * ECS OpenAPI 版本
     */
    private static final String API_VERSION = "2014-05-26";

    private static final String DEFAULT_REGION = "cn-hangzhou";

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final CloudHttpClient httpClient;

    @Override
    public String providerCode() {
        return CloudProviderEnum.ALIYUN.getCode();
    }

    @Override
    public CloudConnectionResult testConnection(IotCloudAccount account) {
        long start = System.currentTimeMillis();
        CloudConnectionResult result = new CloudConnectionResult();
        try {
            invoke(account, "DescribeRegions", new LinkedHashMap<>());
            result.setSuccess(true);
            result.setMessage("阿里云 ECS OpenAPI 连通正常");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        result.setCostMs(System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public List<CloudRegionDTO> listRegions(IotCloudAccount account) {
        JsonNode root = invoke(account, "DescribeRegions", new LinkedHashMap<>());
        List<CloudRegionDTO> list = new ArrayList<>();
        JsonNode regions = root.path("Regions").path("Region");
        for (JsonNode node : regions) {
            CloudRegionDTO dto = new CloudRegionDTO();
            dto.setRegionId(node.path("RegionId").asText());
            dto.setRegionName(node.path("LocalName").asText(dto.getRegionId()));
            list.add(dto);
        }
        return list;
    }

    @Override
    public List<CloudResourceDTO> listResources(IotCloudAccount account, IotCloudResourceBo query) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("RegionId", resolveRegion(account, query));
        if (query != null && StringUtils.isNotBlank(query.getKeyword())) {
            params.put("InstanceName", query.getKeyword());
        }
        JsonNode root = invoke(account, "DescribeInstances", params);
        List<CloudResourceDTO> list = new ArrayList<>();
        for (JsonNode node : root.path("Instances").path("Instance")) {
            list.add(convert(node, resolveRegion(account, query)));
        }
        return filter(list, query);
    }

    @Override
    public boolean startResource(IotCloudAccount account, IotCloudResource resource) {
        return operate(account, "StartInstance", resource);
    }

    @Override
    public boolean stopResource(IotCloudAccount account, IotCloudResource resource) {
        return operate(account, "StopInstance", resource);
    }

    @Override
    public boolean deleteResource(IotCloudAccount account, IotCloudResource resource) {
        return operate(account, "DeleteInstance", resource);
    }

    /**
     * 执行生命周期类操作（启动/停止/释放）
     */
    private boolean operate(IotCloudAccount account, String action, IotCloudResource resource) {
        if (resource == null || StringUtils.isBlank(resource.getInstanceId())) {
            throw new ServiceException("资源实例ID为空，无法执行操作");
        }
        Map<String, String> params = new LinkedHashMap<>();
        params.put("InstanceId", resource.getInstanceId());
        invoke(account, action, params);
        return true;
    }

    /**
     * 发起一次 OpenAPI 调用并返回响应根节点；响应含 Code 字段视为失败
     */
    private JsonNode invoke(IotCloudAccount account, String action, Map<String, String> bizParams) {
        Map<String, String> params = new TreeMap<>();
        params.put("Action", action);
        params.put("Format", "JSON");
        params.put("Version", API_VERSION);
        params.put("AccessKeyId", account.getAccessKey());
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureVersion", "1.0");
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("Timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(TIMESTAMP_FORMAT));
        if (bizParams != null) {
            bizParams.forEach((k, v) -> {
                if (StringUtils.isNotBlank(v)) {
                    params.put(k, v);
                }
            });
        }
        params.put("Signature", sign(account.getAccessSecret(), params));
        String query = params.entrySet().stream()
            .map(e -> percentEncode(e.getKey()) + "=" + percentEncode(e.getValue()))
            .reduce((a, b) -> a + "&" + b)
            .orElse("");
        String url = endpoint(account) + "/?" + query;
        String body = httpClient.get(url, Map.of("Accept", "application/json"));
        try {
            JsonNode root = JsonUtils.getJsonMapper().readTree(body);
            if (root.has("Code")) {
                throw new ServiceException(StringUtils.format("阿里云接口调用失败：[{}] {}",
                    root.path("Code").asText(), root.path("Message").asText()));
            }
            return root;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("阿里云响应解析失败：" + e.getMessage());
        }
    }

    /**
     * HMAC-SHA1 签名
     */
    private String sign(String accessSecret, Map<String, String> params) {
        String canonicalized = params.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(e -> percentEncode(e.getKey()) + "=" + percentEncode(e.getValue()))
            .reduce((a, b) -> a + "&" + b)
            .orElse("");
        String stringToSign = "GET&" + percentEncode("/") + "&" + percentEncode(canonicalized);
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec((accessSecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] bytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new ServiceException("阿里云签名失败：" + e.getMessage());
        }
    }

    /**
     * 阿里云要求的 RFC 3986 编码
     */
    private String percentEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~");
    }

    /**
     * 归一化 ECS 实例
     */
    private CloudResourceDTO convert(JsonNode node, String regionId) {
        CloudResourceDTO dto = new CloudResourceDTO();
        dto.setResourceType(CloudResourceTypeEnum.ECS.getCode());
        dto.setInstanceId(node.path("InstanceId").asText());
        dto.setInstanceName(node.path("InstanceName").asText(dto.getInstanceId()));
        dto.setRegionId(node.path("RegionId").asText(regionId));
        dto.setStatus(node.path("Status").asText());
        dto.setSpec(node.path("InstanceType").asText());
        dto.setPrivateIp(firstIp(node.path("VpcAttributes").path("PrivateIpAddress").path("IpAddress"),
            node.path("InnerIpAddress").path("IpAddress")));
        dto.setPublicIp(firstIp(node.path("PublicIpAddress").path("IpAddress"),
            node.path("EipAddress").path("IpAddress")));
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("provider", CloudProviderEnum.ALIYUN.getCode());
        meta.put("zoneId", node.path("ZoneId").asText());
        meta.put("osName", node.path("OSName").asText());
        meta.put("instanceChargeType", node.path("InstanceChargeType").asText());
        meta.put("creationTime", node.path("CreationTime").asText());
        dto.setBizMeta(meta);
        return dto;
    }

    /**
     * 取第一个非空 IP
     */
    private String firstIp(JsonNode... candidates) {
        for (JsonNode node : candidates) {
            if (node.isArray() && node.size() > 0) {
                String ip = node.get(0).asText();
                if (StringUtils.isNotBlank(ip)) {
                    return ip;
                }
            }
        }
        return null;
    }

    private String resolveRegion(IotCloudAccount account, IotCloudResourceBo query) {
        if (query != null && StringUtils.isNotBlank(query.getRegionId())) {
            return query.getRegionId();
        }
        return StringUtils.isNotBlank(account.getRegionId()) ? account.getRegionId() : DEFAULT_REGION;
    }

    private String endpoint(IotCloudAccount account) {
        String endpoint = StringUtils.isNotBlank(account.getEndpoint()) ? account.getEndpoint() : DEFAULT_ENDPOINT;
        return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
    }

    /**
     * 按查询条件做二次过滤（云端已做部分过滤，此处保持与其它 Provider 一致语义）
     */
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
