#!/usr/bin/env bash
# ============================================================================
# Snail-IoT · 通用应用构建脚本（多项目）
#
# 由 Jenkins 参数化 Job「snail-app-build」调用，参数全部通过环境变量传入，
# 一个应用只要在平台「应用管理」里登记仓库 / 构建命令 / Dockerfile / 工作负载，
# 即可直接构建部署，无需在 Jenkins 中新建 Job。
#
# 环境变量：
#   APP_CODE          应用编码（必填）
#   REPO_URL          Git 地址；或以 / 开头的 Jenkins 容器内本地路径（如 /workspace/snail）
#   REPO_BRANCH       分支（默认 main）
#   BUILD_WORKDIR     构建目录，相对代码根目录（默认 .）
#   BUILD_COMMAND     构建命令，在构建目录内执行；留空表示由 Dockerfile 内完成构建
#   IMAGE             镜像名称（不含 tag）
#   IMAGE_TAG         镜像 tag（通常传发布版本号）
#   JAR_PATH          构建产物 JAR 路径（相对代码根目录，支持通配符；留空不归档）
#   DOCKERFILE_PATH   Dockerfile 路径，相对代码根目录（默认 Dockerfile）
#   K8S_NAMESPACE     目标命名空间，留空则跳过部署
#   K8S_WORKLOAD      目标 Deployment，留空则跳过部署
#   K8S_CONTAINER     目标容器名，留空取 Deployment 首个容器
#
# 用法：
#   bash build-app.sh prepare   # 准备源码（本地路径直接复用，远程仓库浅克隆）
#   bash build-app.sh build     # 执行构建命令
#   bash build-app.sh artifact  # 归档 JAR 到 Jenkins 工作区 artifacts/
#   bash build-app.sh image     # 构建镜像
#   bash build-app.sh deploy    # 更新镜像并等待滚动完成
# ============================================================================
set -euo pipefail

APP_CODE="${APP_CODE:?缺少参数 APP_CODE}"
REPO_URL="${REPO_URL:-}"
REPO_BRANCH="${REPO_BRANCH:-main}"
BUILD_WORKDIR="${BUILD_WORKDIR:-.}"
BUILD_COMMAND="${BUILD_COMMAND:-}"
IMAGE="${IMAGE:-}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
JAR_PATH="${JAR_PATH:-}"
DOCKERFILE_PATH="${DOCKERFILE_PATH:-Dockerfile}"
K8S_NAMESPACE="${K8S_NAMESPACE:-}"
K8S_WORKLOAD="${K8S_WORKLOAD:-}"
K8S_CONTAINER="${K8S_CONTAINER:-}"
ARTIFACT_DIR="${WORKSPACE:-/tmp}/artifacts"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAVEN_SETTINGS="${MAVEN_SETTINGS:-$SCRIPT_DIR/maven/settings.xml}"

# 本地仓库复用挂载的 ~/.m2，central 走 settings.xml 中的阿里云镜像
export MAVEN_OPTS="${MAVEN_OPTS:--Dmaven.repo.local=/var/jenkins_home/.m2/repository}"
if [ -f "$MAVEN_SETTINGS" ] && [ -z "${MAVEN_ARGS:-}" ]; then
  export MAVEN_ARGS="-s $MAVEN_SETTINGS"
fi
export DOCKER_BUILDKIT="${DOCKER_BUILDKIT:-0}"

# 各阶段在独立的 shell 中执行，源码路径通过临时文件传递
SRC_FILE="/tmp/iot-build-${APP_CODE}.src"
KUBE_SRC="${KUBE_SRC:-/root/.kube/config}"
export KUBECONFIG=/tmp/kube/config

log() {
  echo "==> $*"
}

fail() {
  echo "!! $*" >&2
  exit 1
}

prepare_kubeconfig() {
  mkdir -p /tmp/kube
  sed -e 's#https://127.0.0.1:6443#https://kubernetes.docker.internal:6443#g' \
      -e 's#https://localhost:6443#https://kubernetes.docker.internal:6443#g' \
      "$KUBE_SRC" > "$KUBECONFIG"
}

source_dir() {
  [ -f "$SRC_FILE" ] || fail "未找到源码路径记录，请先执行 prepare 阶段"
  cat "$SRC_FILE"
}

stage_prepare() {
  [ -n "$REPO_URL" ] || fail "应用 $APP_CODE 未配置代码仓库"
  local src
  if [[ "$REPO_URL" == /* || "$REPO_URL" == file://* ]]; then
    src="${REPO_URL#file://}"
    [ -d "$src" ] || fail "本地代码目录不存在：$src（需挂载到 Jenkins 容器）"
    log "[prepare] 使用本地工作区：$src（未提交改动同样生效）"
  else
    src="/workspace/repos/$APP_CODE"
    rm -rf "$src"
    log "[prepare] 克隆 $REPO_URL（分支 $REPO_BRANCH）到 $src"
    git clone --depth 1 --branch "$REPO_BRANCH" "$REPO_URL" "$src"
  fi
  echo "$src" > "$SRC_FILE"
}

stage_build() {
  local src workdir
  src="$(source_dir)"
  workdir="$src/$BUILD_WORKDIR"
  [ -d "$workdir" ] || fail "构建目录不存在：$workdir"
  if [ -z "$(printf '%s' "$BUILD_COMMAND" | tr -d '[:space:]')" ]; then
    log "[build] 未配置构建命令，跳过源码构建（交由 Dockerfile 完成）"
    return 0
  fi
  log "[build] 在 $workdir 执行构建命令"
  log "[build] Maven 参数：${MAVEN_ARGS:-未注入}"
  cd "$workdir"
  bash -c "$BUILD_COMMAND"
}

stage_artifact() {
  if [ -z "$(printf '%s' "$JAR_PATH" | tr -d '[:space:]')" ]; then
    log "[artifact] 未配置 JAR 路径，跳过归档"
    return 0
  fi
  local src matched jar
  src="$(source_dir)"
  matched="$(cd "$src" && compgen -G "$JAR_PATH" | head -n 1)"
  [ -n "$matched" ] || fail "未找到构建产物：$src/$JAR_PATH"
  mkdir -p "$ARTIFACT_DIR"
  jar="$ARTIFACT_DIR/${APP_CODE}-${IMAGE_TAG}.jar"
  cp "$src/$matched" "$jar"
  log "[artifact] 已归档 $jar"
}

stage_image() {
  local src dockerfile context
  src="$(source_dir)"
  [ -n "$IMAGE" ] || fail "应用 $APP_CODE 未配置制品/镜像名称"
  dockerfile="$src/$DOCKERFILE_PATH"
  [ -f "$dockerfile" ] || fail "Dockerfile 不存在：$dockerfile"
  context="$(dirname "$dockerfile")"
  log "[image] 构建 $IMAGE:$IMAGE_TAG（上下文 $context）"
  docker build -t "$IMAGE:$IMAGE_TAG" -f "$dockerfile" "$context"
  docker images --format "{{.Repository}}:{{.Tag}}" | grep -F "$IMAGE:$IMAGE_TAG" || true
}

stage_deploy() {
  if [ -z "$K8S_NAMESPACE" ] || [ -z "$K8S_WORKLOAD" ]; then
    log "[deploy] 未配置命名空间或工作负载，跳过部署"
    return 0
  fi
  prepare_kubeconfig
  local container="$K8S_CONTAINER"
  if [ -z "$container" ]; then
    container="$(kubectl -n "$K8S_NAMESPACE" get deployment "$K8S_WORKLOAD" \
      -o jsonpath='{.spec.template.spec.containers[0].name}')"
  fi
  [ -n "$container" ] || fail "未找到工作负载 $K8S_WORKLOAD 的容器"
  log "[deploy] $K8S_NAMESPACE/deployment/$K8S_WORKLOAD 容器 $container -> $IMAGE:$IMAGE_TAG"
  kubectl -n "$K8S_NAMESPACE" set image "deployment/$K8S_WORKLOAD" "$container=$IMAGE:$IMAGE_TAG"
  # tag 复用（如固定 1.0.0）时 set image 不会触发滚动，显式重启以载入新镜像
  kubectl -n "$K8S_NAMESPACE" rollout restart "deployment/$K8S_WORKLOAD"
  kubectl -n "$K8S_NAMESPACE" rollout status "deployment/$K8S_WORKLOAD" --timeout=600s
}

case "${1:-all}" in
  prepare)  stage_prepare ;;
  build)    stage_build ;;
  artifact) stage_artifact ;;
  image)    stage_image ;;
  deploy)   stage_deploy ;;
  all)      stage_prepare; stage_build; stage_artifact; stage_image; stage_deploy ;;
  *) echo "!! 未知参数：$1（可选：prepare | build | artifact | image | deploy | all）"; exit 1 ;;
esac

echo "==> 完成：$1"
