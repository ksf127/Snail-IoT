#!/usr/bin/env bash
# ============================================================================
# Snail-IoT · 构建与部署脚本
#
# 页面「自动化部署 → 重新构建」触发的 Jenkins 流水线，实际执行的就是本脚本。
# 直接构建挂载进容器的仓库工作区（/workspace/snail），因此本地未提交的改动
# 也会被构建，无需先 commit。
#
# 用法：
#   bash ci.sh            # 全流程：后端 → 前端 → 镜像 → 部署 → 冒烟
#   bash ci.sh backend    # 仅构建后端
#   bash ci.sh frontend   # 仅构建前端
#   bash ci.sh images     # 仅构建镜像
#   bash ci.sh deploy     # 仅部署
#   bash ci.sh smoke      # 仅冒烟验证
# ============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
cd "$REPO_ROOT"

NAMESPACE="${NAMESPACE:-iot-platform-dev}"
IMAGE_PREFIX="${IMAGE_PREFIX:-iot-platform}"
TAG="${TAG:-1.0.0}"
K8S_DIR="iot-platform/script/k8s"
export NAMESPACE IMAGE_PREFIX TAG
export MAVEN_OPTS="${MAVEN_OPTS:--Dmaven.repo.local=/var/jenkins_home/.m2/repository}"

KUBE_SRC="${KUBE_SRC:-/root/.kube/config}"
export KUBECONFIG=/tmp/kube/config

prepare_kubeconfig() {
  mkdir -p /tmp/kube
  sed -e 's#https://127.0.0.1:6443#https://kubernetes.docker.internal:6443#g' \
      -e 's#https://localhost:6443#https://kubernetes.docker.internal:6443#g' \
      "$KUBE_SRC" > "$KUBECONFIG"
}

stage_backend() {
  echo "==> [1/5] 构建后端"
  mvn -B -DskipTests -f thirdparty/iot-vue-plus/pom.xml clean install
  mvn -B -Pprod -DskipTests -f iot-platform/pom.xml clean package
  ls -lh iot-platform/iot-platform-admin/target/iot-platform-admin.jar
}

stage_frontend() {
  echo "==> [2/5] 构建前端"
  cd iot-platform/plus-ui
  pnpm install --frozen-lockfile --registry=https://registry.npmmirror.com \
    --config.manage-package-manager-versions=false
  pnpm run build:prod
  ls -ld dist
  cd "$REPO_ROOT"
}

stage_images() {
  echo "==> [3/5] 构建镜像"
  docker build -t "$IMAGE_PREFIX/iot-platform-admin:$TAG" iot-platform/iot-platform-admin
  docker build -t "$IMAGE_PREFIX/plus-ui:$TAG" iot-platform/plus-ui
  docker images --format "{{.Repository}}:{{.Tag}}" | grep "^$IMAGE_PREFIX/" || true
}

stage_deploy() {
  echo "==> [4/5] 部署到 Kubernetes"
  prepare_kubeconfig
  kubectl version --client
  kubectl get nodes
  bash "$K8S_DIR/deploy.sh" dev
  # 镜像 tag 固定为 $TAG，apply 不会触发滚动更新，这里显式重启以载入新镜像
  kubectl -n "$NAMESPACE" rollout restart deployment/iot-server deployment/iot-web
  kubectl -n "$NAMESPACE" rollout status deployment/iot-server --timeout=600s
  kubectl -n "$NAMESPACE" rollout status deployment/iot-web --timeout=300s
}

stage_smoke() {
  echo "==> [5/5] 冒烟验证"
  kubectl -n "$NAMESPACE" get pods
  kubectl -n "$NAMESPACE" exec deploy/iot-web -- wget -q -O /dev/null http://127.0.0.1/ \
    && echo "前端页面 OK"
  kubectl -n "$NAMESPACE" exec deploy/iot-web -- sh -c 'nc -z iot-server 8080' \
    && echo "iot-server 连通"
}

case "${1:-all}" in
  backend)  stage_backend ;;
  frontend) stage_frontend ;;
  images)   stage_images ;;
  deploy)   stage_deploy ;;
  smoke)    stage_smoke ;;
  all)      stage_backend; stage_frontend; stage_images; stage_deploy; stage_smoke ;;
  *) echo "!! 未知参数：$1（可选：backend | frontend | images | deploy | smoke | all）"; exit 1 ;;
esac

echo "==> 完成：$1"
