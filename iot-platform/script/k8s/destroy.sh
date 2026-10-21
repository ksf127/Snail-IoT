#!/usr/bin/env bash
# ============================================================================
# Snail-IoT · Kubernetes 卸载
#
# 删除目标环境命名空间（含 Deployment / Service / PVC / ConfigMap / Secret / Job）。
# 注意：默认 StorageClass 回收策略为 Delete，PVC 数据将被一并删除！
#
# 用法：
#   bash script/k8s/destroy.sh          # 卸载开发环境（默认）
#   bash script/k8s/destroy.sh dev      # 卸载开发环境
#   bash script/k8s/destroy.sh prod     # 卸载生产环境
# ============================================================================
set -euo pipefail

ENV="${1:-${ENV:-dev}}"
case "$ENV" in
  dev|prod) ;;
  *) echo "!! 未知环境：$ENV（可选：dev | prod）"; exit 1 ;;
esac
NAMESPACE="${NAMESPACE:-iot-platform-$ENV}"

echo "==> 即将删除命名空间 $NAMESPACE（将导致全部数据丢失）"
kubectl delete namespace "$NAMESPACE" --ignore-not-found=true --wait=true

echo "==> 已卸载 $NAMESPACE"
