package org.dromara.iot.deploy.service;

import cn.hutool.core.util.StrUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kubernetes 操作封装
 * <p>
 * 供自动化部署模块下发制品使用：读取 Deployment、更新容器镜像、写入 restartedAt 触发
 * 滚动重启、轮询等待滚动更新完成。
 * <p>
 * 认证方式自动适配：部署在集群内时使用 ServiceAccount（挂载令牌），本地调试时回退到
 * kubeconfig（默认 ~/.kube/config）。
 *
 * @author iot team
 */
@Slf4j
@Component
public class KubernetesDeployService {

    /**
     * 触发滚动重启的 Pod 模板注解（与 kubectl rollout restart 行为一致）
     */
    private static final String RESTART_ANNOTATION = "kubectl.kubernetes.io/restartedAt";

    /**
     * restartedAt 注解时间格式（RFC3339）
     */
    private static final DateTimeFormatter RESTART_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * 滚动更新状态轮询间隔（毫秒）
     */
    private static final long ROLLOUT_POLL_INTERVAL_MILLIS = 3000L;

    private ApiClient apiClient;

    private AppsV1Api appsV1Api;

    private CoreV1Api coreV1Api;

    /**
     * 懒加载并缓存 Kubernetes 客户端（首次调用时按运行环境选择认证方式）
     */
    private synchronized void ensureClient() {
        if (apiClient != null) {
            return;
        }
        boolean inCluster = StrUtil.isNotBlank(System.getenv("KUBERNETES_SERVICE_HOST"));
        try {
            ApiClient client = inCluster ? Config.fromCluster() : Config.defaultClient();
            client.setConnectTimeout(10_000);
            client.setReadTimeout(30_000);
            this.apiClient = client;
            this.appsV1Api = new AppsV1Api(client);
            this.coreV1Api = new CoreV1Api(client);
            log.info("[IoT] Kubernetes 客户端初始化完成（认证方式={}）", inCluster ? "集群内 ServiceAccount" : "本地 kubeconfig");
        } catch (IOException e) {
            throw new IllegalStateException("Kubernetes 客户端初始化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 判断命名空间是否存在
     *
     * @param namespace 命名空间
     * @return 是否存在
     * @throws ApiException 非 404 的访问异常
     */
    public boolean namespaceExists(String namespace) throws ApiException {
        ensureClient();
        try {
            coreV1Api.readNamespace(namespace).execute();
            return true;
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    /**
     * 读取 Deployment
     *
     * @param namespace 命名空间
     * @param name      Deployment 名称
     * @return Deployment 对象
     * @throws ApiException 读取失败（含 404）
     */
    public V1Deployment readDeployment(String namespace, String name) throws ApiException {
        ensureClient();
        return appsV1Api.readNamespacedDeployment(name, namespace).execute();
    }

    /**
     * 更新 Deployment 指定容器的镜像
     * <p>
     * 采用「读取-修改-整体替换」而非 patch，避免 patch 内容类型在不同客户端版本下的歧义。
     *
     * @param namespace     命名空间
     * @param name          Deployment 名称
     * @param containerName 容器名称（留空时取首个容器）
     * @param image         目标镜像
     * @return 更新前的镜像
     * @throws ApiException 更新失败
     */
    public String applyImage(String namespace, String name, String containerName, String image) throws ApiException {
        ensureClient();
        V1Deployment deployment = readDeployment(namespace, name);
        V1Container container = locateContainer(deployment, containerName);
        String previous = container.getImage();
        container.setImage(image);
        appsV1Api.replaceNamespacedDeployment(name, namespace, deployment).execute();
        return previous;
    }

    /**
     * 通过写入 restartedAt 注解触发滚动重启
     *
     * @param namespace 命名空间
     * @param name      Deployment 名称
     * @throws ApiException 更新失败
     */
    public void restart(String namespace, String name) throws ApiException {
        ensureClient();
        V1Deployment deployment = readDeployment(namespace, name);
        V1ObjectMeta templateMeta = templateMeta(deployment);
        Map<String, String> annotations = templateMeta.getAnnotations();
        if (annotations == null) {
            annotations = new HashMap<>(4);
            templateMeta.setAnnotations(annotations);
        }
        annotations.put(RESTART_ANNOTATION, OffsetDateTime.now().format(RESTART_TIME_FORMATTER));
        appsV1Api.replaceNamespacedDeployment(name, namespace, deployment).execute();
    }

    /**
     * 轮询等待滚动更新完成
     *
     * @param namespace      命名空间
     * @param name           Deployment 名称
     * @param timeoutSeconds 超时时间（秒）
     * @throws ApiException         读取失败
     * @throws IllegalStateException 超时或线程中断
     */
    public void waitRolloutReady(String namespace, String name, long timeoutSeconds) throws ApiException {
        ensureClient();
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (true) {
            V1Deployment deployment = readDeployment(namespace, name);
            V1DeploymentStatus status = deployment.getStatus();
            Long generation = deployment.getMetadata() == null ? null : deployment.getMetadata().getGeneration();
            int desired = desiredReplicas(deployment);
            if (rolloutComplete(status, generation, desired)) {
                log.info("[IoT] Deployment {}/{} 滚动更新完成，副本={}", namespace, name, desired);
                return;
            }
            if (System.currentTimeMillis() >= deadline) {
                throw new IllegalStateException("等待 Deployment " + namespace + "/" + name + " 滚动更新超时（"
                    + timeoutSeconds + "s，期望副本=" + desired + "，当前状态=" + describeStatus(status) + "）");
            }
            sleepRolloutInterval();
        }
    }

    /**
     * 读取 Deployment 期望副本数（未显式配置时按 1 处理）
     *
     * @param deployment Deployment 对象
     * @return 期望副本数
     */
    public int desiredReplicas(V1Deployment deployment) {
        Integer replicas = deployment.getSpec() == null ? null : deployment.getSpec().getReplicas();
        return replicas == null ? 1 : replicas;
    }

    /**
     * 读取 Deployment 指定容器的当前镜像（仅用于日志展示，异常场景返回 "-"）
     *
     * @param deployment    Deployment 对象
     * @param containerName 容器名称（留空时取首个容器）
     * @return 当前镜像
     */
    public String currentImage(V1Deployment deployment, String containerName) {
        List<V1Container> containers = containersOf(deployment);
        if (containers == null || containers.isEmpty()) {
            return "-";
        }
        return containers.stream()
            .filter(c -> StrUtil.isBlank(containerName) || containerName.equals(c.getName()))
            .map(V1Container::getImage)
            .findFirst()
            .orElse("-");
    }

    /**
     * 判断滚动更新是否已完成
     */
    private boolean rolloutComplete(V1DeploymentStatus status, Long generation, int desired) {
        if (status == null || status.getObservedGeneration() == null || generation == null) {
            return false;
        }
        if (status.getObservedGeneration() < generation) {
            return false;
        }
        Integer updated = status.getUpdatedReplicas();
        Integer available = status.getAvailableReplicas();
        Integer unavailable = status.getUnavailableReplicas();
        return updated != null && updated == desired
            && available != null && available >= desired
            && (unavailable == null || unavailable == 0);
    }

    /**
     * 定位目标容器
     */
    private V1Container locateContainer(V1Deployment deployment, String containerName) {
        List<V1Container> containers = containersOf(deployment);
        if (containers == null || containers.isEmpty()) {
            throw new IllegalStateException("Deployment 未定义任何容器");
        }
        if (StrUtil.isBlank(containerName)) {
            return containers.get(0);
        }
        return containers.stream()
            .filter(c -> containerName.equals(c.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Deployment 中未找到容器：" + containerName));
    }

    /**
     * 读取 Deployment 的 Pod 模板容器列表
     */
    private List<V1Container> containersOf(V1Deployment deployment) {
        V1PodTemplateSpec template = deployment.getSpec() == null ? null : deployment.getSpec().getTemplate();
        return template == null || template.getSpec() == null ? null : template.getSpec().getContainers();
    }

    /**
     * 获取 Pod 模板的元数据对象（不存在时创建）
     */
    private V1ObjectMeta templateMeta(V1Deployment deployment) {
        V1PodTemplateSpec template = deployment.getSpec() == null ? null : deployment.getSpec().getTemplate();
        if (template == null) {
            throw new IllegalStateException("Deployment 未定义 Pod 模板");
        }
        V1ObjectMeta meta = template.getMetadata();
        if (meta == null) {
            meta = new V1ObjectMeta();
            template.setMetadata(meta);
        }
        return meta;
    }

    /**
     * 滚动更新状态摘要（用于超时提示）
     */
    private String describeStatus(V1DeploymentStatus status) {
        if (status == null) {
            return "无状态信息";
        }
        return "replicas=" + status.getReplicas()
            + ", updated=" + status.getUpdatedReplicas()
            + ", available=" + status.getAvailableReplicas()
            + ", unavailable=" + status.getUnavailableReplicas();
    }

    /**
     * 轮询间隔休眠
     */
    private void sleepRolloutInterval() {
        try {
            Thread.sleep(ROLLOUT_POLL_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待滚动更新时线程被中断", e);
        }
    }

}
