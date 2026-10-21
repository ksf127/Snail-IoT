#!/usr/bin/env bash
# ============================================================================
# Snail-IoT · 数据库初始化
#
# 1. 从仓库 SQL 目录动态生成 4 个 ConfigMap（规避单 ConfigMap 1MiB 限制）
# 2. 删除并重建 iot-db-init Job（Job spec 不可变，重建最稳妥）
# 3. 等待 Job 完成
#
# 前置：目标命名空间与 PostgreSQL 已就绪。
# 用法：
#   bash script/k8s/db-init.sh            # 默认命名空间 iot-platform-dev
#   NAMESPACE=iot-platform-prod bash script/k8s/db-init.sh
# ============================================================================
set -euo pipefail

NAMESPACE="${NAMESPACE:-iot-platform-dev}"
ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"

VUE_SQL="$ROOT/thirdparty/iot-vue-plus/script/sql/postgres"
AI_SQL="$ROOT/thirdparty/snail-ai/script/sql"
IOT_SQL="$ROOT/iot-platform/script/sql/postgres"

echo "==> [db-init] 仓库根目录：$ROOT"
echo "==> [db-init] 生成 SQL ConfigMap"

kubectl -n "$NAMESPACE" create configmap iot-sql-vector \
  --from-file=init-vector.sql="$ROOT/init-vector.sql" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "$NAMESPACE" create configmap iot-sql-base \
  --from-file=postgres_iot_vue.sql="$VUE_SQL/postgres_iot_vue.sql" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "$NAMESPACE" create configmap iot-sql-framework \
  --from-file=postgres_iot_job.sql="$VUE_SQL/postgres_iot_job.sql" \
  --from-file=postgres_iot_workflow.sql="$VUE_SQL/postgres_iot_workflow.sql" \
  --from-file=postgres_iot_ai.sql="$VUE_SQL/postgres_iot_ai.sql" \
  --from-file=snail_ai_schema_pgsql.sql="$AI_SQL/snail_ai_schema_pgsql.sql" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "$NAMESPACE" create configmap iot-sql-iot \
  --from-file="$IOT_SQL" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "==> [db-init] 重建 Job iot-db-init"
kubectl -n "$NAMESPACE" delete job iot-db-init --ignore-not-found=true
# Job 清单内硬编码命名空间，渲染为目标环境后再 apply（kubectl -n 会被清单内的 namespace 覆盖）
RENDER_JOB="$(mktemp)"
sed "s/namespace: iot-platform/namespace: $NAMESPACE/g" "$ROOT/iot-platform/script/k8s/db-init-job.yaml" > "$RENDER_JOB"
kubectl apply -f "$RENDER_JOB"
rm -f "$RENDER_JOB"

echo "==> [db-init] 等待初始化完成（最长 10 分钟）"
kubectl -n "$NAMESPACE" wait --for=condition=complete job/iot-db-init --timeout=600s

echo "==> [db-init] 数据库初始化完成。"
