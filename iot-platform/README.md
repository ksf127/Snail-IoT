# Snail-IoT · iot-platform

> 自研 IoT 业务产品：设备接入与采集、产品/设备管理、三线权限、统一认证、档案/报表/部署、聚合支付、工单、云平台纳管、任务调度。
> 基于 `org.dromara:iot-vue-plus:6.0.0` 框架底座构建。

---

## 一、技术栈

| 类别 | 选型 |
| --- | --- |
| 运行时 | JDK 21 · Spring Boot 4.1.0 |
| 鉴权 | Sa-Token 1.45.0（含 CAS / OAuth2 / OIDC） |
| 持久化 | MyBatis-Plus 3.5.17 · PostgreSQL 16（pgvector） |
| 缓存/锁 | Redis 7 · Redisson · Lock4j |
| 搜索 | Elasticsearch 8.15.3 · Easy-ES 3.0.2 |
| 消息 | Kafka 3.9.0（KRaft）· EMQX 5.8.4 · mica-mqtt 2.6.8 |
| 存储 | MinIO（S3，AWS SDK） |
| 编排 | warm-flow 1.8.9 · LiteFlow 2.16.1.2 |
| 调度/AI | SnailJob 2.0.2 · SnailAI 1.1.1 |
| 支付 | IJPay 2.9.13 |
| 前端 | Vue 3.5 · TypeScript · Element Plus · Vite 8（`plus-ui`） |

---

## 二、工程结构

```
iot-platform/
├── pom.xml                     # 聚合 POM（继承 iot-vue-plus 6.0.0）
├── iot-platform-common/        # 常量与枚举
├── iot-platform-modules/       # 业务模块聚合（12 个）
│   ├── iot-platform-device/       # 产品 / 设备
│   ├── iot-platform-permission/   # 分组 / 数据集 / 应用（三线权限）
│   ├── iot-platform-collect/      # MQTT / HTTP / Kafka / ES 采集链路
│   ├── iot-platform-monitor/      # 设备监控 / 服务器监控
│   ├── iot-platform-sso/          # OAuth2 Server / OIDC / CAS
│   ├── iot-platform-archive/      # 档案管理
│   ├── iot-platform-report/       # 报表
│   ├── iot-platform-deploy/       # 自动化部署 / GitHub 项目同步
│   ├── iot-platform-pay/          # 聚合支付
│   ├── iot-platform-ticket/       # 工单（LiteFlow / Warm-Flow）
│   ├── iot-platform-cloud/        # 云平台纳管
│   └── iot-platform-job/          # 任务调度
├── iot-platform-admin/         # 启动入口（IotApplication）与配置
├── plus-ui/                    # 前端工程（Vue3 + Vite）
└── script/
    ├── docker/docker-compose.yml  # 统一容器编排
    ├── k8s/                       # Kubernetes 清单与 db-init
    └── sql/postgres/              # 业务建表脚本
```

---

## 三、构建与运行

### 3.1 前置

框架底座必须先安装到本地 Maven 仓库（产品 POM 依赖 `org.dromara:iot-vue-plus:6.0.0`）：

```bash
cd ../thirdparty/iot-vue-plus && mvn -DskipTests clean install
```

### 3.2 构建后端

```bash
# 普通构建
mvn -DskipTests clean package

# 生产打包（产出 iot-platform-admin/target/iot-platform-admin.jar）
mvn -Pprod -DskipTests clean package

# 接入 Nacos（默认不激活，独立 profile）
mvn -Pnacos -DskipTests clean package
```

> Windows 沙箱下需追加：`"-Dmaven.repo.local=C:\Users\ThinkPad\.m2\repository" "-Duser.home=D:\DevProjects\snail\.mvn-home"`。

### 3.3 构建前端

```bash
cd plus-ui
pnpm install --registry=https://registry.npmmirror.com
pnpm dev          # 开发
pnpm build:prod   # 生产构建
```

### 3.4 容器化部署（推荐）

```bash
# 先启动根基础设施（PostgreSQL + Redis）
# 根编排未声明 networks，必须用 -p devprojects 生成业务编排引用的 devprojects_default
cd .. && docker compose -p devprojects -f docker-compose.yml up -d

# 构建并启动全部业务容器
cd iot-platform
docker compose -p devprojects -f script/docker/docker-compose.yml up -d --build
```

启动后访问 http://localhost ，使用 `admin / admin123` 登录。`iot-web`（Nginx，80）是唯一对外入口，`/prod-api`、`/admin`、`/snail-job`、`/snail-ai` 均由其反向代理。

---

## 四、服务端口

| 服务 | 容器 | 端口 |
| --- | --- | --- |
| 前端统一入口 | `iot-web` | 80 |
| 后端 | `iot-server` | 8080 |
| Spring Boot Admin | `iot-sba` | 9090 |
| SnailJob | `iot-snailjob` | 8800 / 17888 |
| SnailAI | `iot-snailai` | 8900 / 18888 |
| Nacos | `iot-nacos` | 8848 / 9848 |
| Elasticsearch | `iot-elasticsearch` | 9200 |
| Kafka | `iot-kafka` | 9094 |
| EMQX | `iot-emqx` | 1883 / 8083 / 18083 |
| MinIO | `iot-minio` | 9000 / 9001 |

---

## 五、关键配置

主配置：[iot-platform-admin/src/main/resources/application.yml](file:///d:/DevProjects/snail/iot-platform/iot-platform-admin/src/main/resources/application.yml)

- `spring.application.name: iot-platform`，端口 `8080`
- Sa-Token `token-name: Authorization`；后续请求需带 `clientid` 头且与 Token extra 一致；免鉴权路径含 `/iot/pay/notify/**`
- `iot.sso`：`server-url`、`default-client-id`、`ticket-timeout`、`oidc.id-token-timeout`
- `iot.github`：`owner=ksf127`、`token=${GITHUB_TOKEN:}`（私有仓库同步用）
- SpringDoc 13 个分组：`通用/系统/设备/采集/监控/单点登录/档案/报表/部署/支付/工单/云平台/任务调度`
- 消息推送：`message.transport: sse`、`message.path: /resource/message`（WebSocket 未启用）
- 组件开关：`warm-flow`、`liteflow`、`mqtt.client`、`easy-es`、`spring.ai.mcp`（`/mcp`）、Kafka、Nacos（默认 `NACOS_ENABLED:false`）
- `api-decrypt.enabled: true`（接口加解密，RSA 密钥与前端的 `VITE_APP_RSA_*` 成对）

生产配置：[application-prod.yml](file:///d:/DevProjects/snail/iot-platform/iot-platform-admin/src/main/resources/application-prod.yml)（连接串全部由环境变量注入）。

---

## 六、文档索引

- 工作区总览：[../README.md](file:///d:/DevProjects/snail/README.md)
- [01 系统架构设计](file:///d:/DevProjects/snail/docs/01-系统架构设计.md)
- [02 部署指南](file:///d:/DevProjects/snail/docs/02-部署指南.md)
- [03 构建与开发指南](file:///d:/DevProjects/snail/docs/03-构建与开发指南.md)
- [04 账号与初始化说明](file:///d:/DevProjects/snail/docs/04-账号与初始化说明.md)
- [05 模块功能说明](file:///d:/DevProjects/snail/docs/05-模块功能说明.md)
- HTML 图表：[docs/diagrams](file:///d:/DevProjects/snail/docs/diagrams/index.html)

---

## 七、默认账号

| 系统 | 地址 | 账号 / 密码 |
| --- | --- | --- |
| 平台前端 | http://localhost | `admin` / `admin123` |
| Nacos | http://localhost:8848/nacos | `nacos` / `123456` |
| EMQX | http://localhost:18083 | `admin` / `public` |
| MinIO | http://localhost:9001 | `ruoyi` / `ruoyi123` |
| Spring Boot Admin | http://localhost/admin | `admin` / `admin123` |
| SnailJob | http://localhost/snail-job | `admin` / `admin123` |
| SnailAI | http://localhost/snail-ai | `admin` / `admin123` |

> 上述控制台均由 `iot-web`（80）反向代理，也可直连端口（SBA 9090、SnailJob 8800、SnailAI 8900）。完整说明见 [04 账号与初始化说明](file:///d:/DevProjects/snail/docs/04-账号与初始化说明.md)。
