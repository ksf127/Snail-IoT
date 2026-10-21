#!/usr/bin/env bash
# ============================================================================
# Snail-IoT · Kubernetes 部署（Docker Desktop 内置 Kubernetes）
#
# 按依赖顺序部署：命名空间 → 密钥 → 中间件 → 数据库初始化 → 应用 → 就绪校验
#
# 两套规模：
#   dev  → 精简栈：postgres + redis + iot-server + iot-web（单机内存有限）
#   prod → 全量栈：上述之外再加 elasticsearch/kafka/emqx/minio/sba/snailjob/snailai
#
# 多环境隔离（每个环境一个独立命名空间，前端使用互不冲突的 NodePort）：
#   dev  → 命名空间 iot-platform-dev  ，前端 http://localhost:30080
#   prod → 命名空间 iot-platform-prod ，前端 http://localhost:30081
#
# 前置：
#   1. Docker Desktop 已启用 Kubernetes，kubectl 指向 docker-desktop 上下文
#   2. 业务镜像已在本地构建（imagePullPolicy: IfNotPresent 复用 Docker 引擎镜像）
#      - iot-platform/iot-platform-admin:1.0.0
#      - iot-platform/monitor-admin:6.0.0   （仅 prod 需要）
#      - iot-platform/snailjob-server:2.0.2 （仅 prod 需要）
#      - iot-platform/snailai-server:1.1.1  （仅 prod 需要）
#      - iot-platform/plus-ui:1.0.0
#
# 用法：
#   bash script/k8s/deploy.sh          # 部署开发环境（默认，精简栈）
#   bash script/k8s/deploy.sh dev      # 部署开发环境（精简栈）
#   bash script/k8s/deploy.sh prod     # 部署生产环境（全量栈）
# ============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# ---- 环境解析：命名空间与前端 NodePort 均按环境隔离 ----
ENV="${1:-${ENV:-dev}}"
case "$ENV" in
  dev)  WEB_NODE_PORT="${WEB_NODE_PORT:-30080}" ;;
  prod) WEB_NODE_PORT="${WEB_NODE_PORT:-30081}" ;;
  *) echo "!! 未知环境：$ENV（可选：dev | prod）"; exit 1 ;;
esac
NAMESPACE="${NAMESPACE:-iot-platform-$ENV}"

# ---- 渲染清单到临时目录 ----
# 现有 YAML 硬编码 `namespace: iot-platform` 与 LoadBalancer:80，直接 apply 会导致
# 两个环境互相覆盖且端口冲突。这里复制一份清单并做替换，仓库内文件保持可读。
RENDER_DIR="$(mktemp -d)"
trap 'rm -rf "$RENDER_DIR"' EXIT
cp -R "$SCRIPT_DIR"/. "$RENDER_DIR"/

# 1) 全量命名空间替换
find "$RENDER_DIR" -name '*.yaml' -print0 | xargs -0 sed -i "s/namespace: iot-platform/namespace: $NAMESPACE/g"
# 2) 命名空间资源自身的名称
sed -i "s/^  name: iot-platform$/  name: $NAMESPACE/" "$RENDER_DIR/namespace.yaml"
# 3) iot-web 由 LoadBalancer:80 改为各环境独立的 NodePort
sed -i "s/type: LoadBalancer/type: NodePort/" "$RENDER_DIR/apps/iot-web.yaml"
sed -i "s/^      targetPort: 80$/      targetPort: 80\n      nodePort: $WEB_NODE_PORT/" "$RENDER_DIR/apps/iot-web.yaml"

# 4) 部署范围：dev 精简栈 / prod 全量栈
#    dev 单机 Docker Desktop 内存有限，跑不动全量 JVM 栈（ES+Kafka+EMQX+3 个 Server
#    同时启动会互相饿死），因此只保留核心链路 postgres + redis + iot-server + iot-web。
if [ "$ENV" = "dev" ]; then
  INFRA_ITEMS=(postgres redis)
  APP_ITEMS=(iot-server iot-web)
  # 精简栈未部署 ES / EMQX，关闭对应客户端，避免 iot-server 启动期连不上而反复重试
  sed -i '/- name: EASY_ES_ENABLE/{n;s/value: "true"/value: "false"/}' "$RENDER_DIR/apps/iot-server.yaml"
  sed -i '/- name: MQTT_CLIENT_ENABLED/{n;s/value: "true"/value: "false"/}' "$RENDER_DIR/apps/iot-server.yaml"
  # iot-web 的 wait-backends 只等待 iot-server（sba/snailjob/snailai 在精简栈中不存在）
  sed -i 's|for hp in iot-server:8080 iot-sba:9090 iot-snailjob:8800 iot-snailai:8900; do|for hp in iot-server:8080; do|' "$RENDER_DIR/apps/iot-web.yaml"
else
  INFRA_ITEMS=(postgres redis elasticsearch kafka emqx minio)
  APP_ITEMS=(iot-server iot-sba iot-snailjob iot-snailai iot-web)
fi

echo "==> 环境：$ENV（命名空间 $NAMESPACE，前端 NodePort $WEB_NODE_PORT）"
echo "    规模：中间件 ${INFRA_ITEMS[*]} | 应用 ${APP_ITEMS[*]}"

echo "==> [1/6] 创建命名空间与密钥"
kubectl apply -f "$RENDER_DIR/namespace.yaml"
kubectl apply -f "$RENDER_DIR/secret.yaml"
# 自动化部署所需的 ServiceAccount 授权（iot-server 调用 K8s API 下发镜像/滚动重启）
kubectl apply -f "$RENDER_DIR/deploy-rbac.yaml"

# dev 精简栈：清理历史全量部署残留的非核心工作负载，释放内存
if [ "$ENV" = "dev" ]; then
  echo "==> [dev] 清理非核心中间件/应用残留（若存在）"
  kubectl -n "$NAMESPACE" delete deployment iot-elasticsearch iot-kafka iot-emqx iot-minio \
    iot-sba iot-snailjob iot-snailai --ignore-not-found=true
  kubectl -n "$NAMESPACE" delete service iot-elasticsearch iot-kafka iot-emqx iot-minio \
    --ignore-not-found=true
  kubectl -n "$NAMESPACE" delete job iot-minio-init --ignore-not-found=true
  # 注意：iot-web 镜像内的 nginx.conf 硬编码了 sba/snailjob/snailai 三个 upstream，
  # 精简栈虽不部署这三个控制台，但必须保留同名 Service，否则 nginx 启动时解析不到
  # DNS 会直接报 "host not found in upstream" 退出（页面照样打不开）。
  for hp in iot-sba:9090 iot-snailjob:8800 iot-snailai:8900; do
    name="${hp%%:*}"; port="${hp##*:}"
    kubectl -n "$NAMESPACE" create service clusterip "$name" --tcp="$port:$port" \
      --dry-run=client -o yaml | kubectl apply -f -
  done
fi

echo "==> [2/6] 部署中间件（${INFRA_ITEMS[*]}）"
for item in "${INFRA_ITEMS[@]}"; do
  kubectl apply -f "$RENDER_DIR/infrastructure/$item.yaml"
done

echo "==> [3/6] 等待 PostgreSQL / Redis 就绪"
kubectl -n "$NAMESPACE" rollout status deployment/iot-postgres --timeout=300s
kubectl -n "$NAMESPACE" rollout status deployment/iot-redis --timeout=180s

echo "==> [4/6] 初始化数据库"
NAMESPACE="$NAMESPACE" bash "$SCRIPT_DIR/db-init.sh"

echo "==> [5/6] 部署应用（${APP_ITEMS[*]}）"
for item in "${APP_ITEMS[@]}"; do
  kubectl apply -f "$RENDER_DIR/apps/$item.yaml"
done

echo "==> [6/6] 等待应用就绪"
for d in "${APP_ITEMS[@]}"; do
  echo "    - 等待 deployment/$d"
  kubectl -n "$NAMESPACE" rollout status "deployment/$d" --timeout=600s
done

echo
echo "==> 部署完成。入口信息："
kubectl -n "$NAMESPACE" get svc iot-web
echo
echo "    前端入口： http://localhost:$WEB_NODE_PORT/                （iot-web NodePort）"
echo "    后端接口： http://localhost:$WEB_NODE_PORT/prod-api/       （经 Nginx 反代 iot-server）"
if [ "$ENV" != "dev" ]; then
  echo "    监控中心： http://localhost:$WEB_NODE_PORT/admin/          （iot-sba）"
  echo "    任务调度： http://localhost:$WEB_NODE_PORT/snail-job/      （iot-snailjob）"
  echo "    AI 控制台：http://localhost:$WEB_NODE_PORT/snail-ai/       （iot-snailai）"
fi
