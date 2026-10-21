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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Apache Flink 纳管实现
 * <p>
 * 通过 JobManager REST API 接入（账号 endpoint 即 JobManager 地址，如 http://flink-jobmanager:8081）。
 * Flink 无云地域概念，{@link #listRegions} 返回空列表。
 *
 * @author iot team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlinkCloudProvider implements ICloudProvider {

    private final CloudHttpClient httpClient;

    @Override
    public String providerCode() {
        return CloudProviderEnum.FLINK.getCode();
    }

    @Override
    public CloudConnectionResult testConnection(IotCloudAccount account) {
        long start = System.currentTimeMillis();
        CloudConnectionResult result = new CloudConnectionResult();
        try {
            JsonNode root = getJson(account, "/overview");
            result.setSuccess(true);
            result.setMessage(StringUtils.format("Flink 集群连通正常，运行中作业 {} 个，可用 TaskManager {} 个",
                root.path("jobs-running").asInt(), root.path("taskmanagers").asInt()));
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        result.setCostMs(System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public List<CloudRegionDTO> listRegions(IotCloudAccount account) {
        // Flink 无地域概念
        return new ArrayList<>();
    }

    @Override
    public List<CloudResourceDTO> listResources(IotCloudAccount account, IotCloudResourceBo query) {
        JsonNode root = getJson(account, "/jobs/overview");
        List<CloudResourceDTO> list = new ArrayList<>();
        for (JsonNode node : root.path("jobs")) {
            list.add(convert(account, node));
        }
        return filter(list, query);
    }

    @Override
    public boolean stopResource(IotCloudAccount account, IotCloudResource resource) {
        checkResource(resource);
        // 优雅停止（依赖 savepoint 目录配置，未配置时 JobManager 会直接报错）
        httpClient.post(baseUrl(account) + "/jobs/" + resource.getInstanceId() + "/stop",
            jsonHeaders(), "{}");
        return true;
    }

    @Override
    public boolean deleteResource(IotCloudAccount account, IotCloudResource resource) {
        checkResource(resource);
        // 取消作业
        httpClient.patch(baseUrl(account) + "/jobs/" + resource.getInstanceId() + "?mode=cancel",
            jsonHeaders(), "{}");
        return true;
    }

    /**
     * 归一化 Flink 作业；并行度需再取一次作业详情，失败不影响列表整体
     */
    private CloudResourceDTO convert(IotCloudAccount account, JsonNode job) {
        CloudResourceDTO dto = new CloudResourceDTO();
        dto.setResourceType(CloudResourceTypeEnum.FLINK_JOB.getCode());
        dto.setInstanceId(job.path("jid").asText());
        dto.setInstanceName(job.path("name").asText(dto.getInstanceId()));
        dto.setStatus(job.path("state").asText());
        dto.setSpec(resolveParallelism(account, dto.getInstanceId()));
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("provider", CloudProviderEnum.FLINK.getCode());
        meta.put("startTime", job.path("start-time").asLong());
        meta.put("endTime", job.path("end-time").asLong());
        meta.put("duration", job.path("duration").asLong());
        meta.put("lastModification", job.path("last-modification").asLong());
        JsonNode tasks = job.path("tasks");
        meta.put("tasksTotal", tasks.path("total").asInt());
        meta.put("tasksRunning", tasks.path("running").asInt());
        meta.put("tasksFailed", tasks.path("failed").asInt());
        meta.put("tasksCanceled", tasks.path("canceled").asInt());
        dto.setBizMeta(meta);
        return dto;
    }

    /**
     * 取作业各算子并行度最大值作为规格展示
     */
    private String resolveParallelism(IotCloudAccount account, String jobId) {
        try {
            JsonNode detail = getJson(account, "/jobs/" + jobId);
            int max = 0;
            for (JsonNode vertex : detail.path("vertices")) {
                max = Math.max(max, vertex.path("parallelism").asInt());
            }
            return max > 0 ? String.valueOf(max) : null;
        } catch (Exception e) {
            log.warn("获取 Flink 作业 {} 并行度失败：{}", jobId, e.getMessage());
            return null;
        }
    }

    private JsonNode getJson(IotCloudAccount account, String path) {
        String body = httpClient.get(baseUrl(account) + path, Map.of("Accept", "application/json"));
        try {
            return JsonUtils.getJsonMapper().readTree(body);
        } catch (Exception e) {
            throw new ServiceException("Flink 响应解析失败：" + e.getMessage());
        }
    }

    private void checkResource(IotCloudResource resource) {
        if (resource == null || StringUtils.isBlank(resource.getInstanceId())) {
            throw new ServiceException("作业ID为空，无法执行操作");
        }
    }

    private Map<String, String> jsonHeaders() {
        return Map.of("Content-Type", "application/json", "Accept", "application/json");
    }

    private String baseUrl(IotCloudAccount account) {
        if (StringUtils.isBlank(account.getEndpoint())) {
            throw new ServiceException("未配置 Flink JobManager 地址");
        }
        String endpoint = account.getEndpoint();
        return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
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
