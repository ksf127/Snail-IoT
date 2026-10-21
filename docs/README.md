# 平台文档索引

本目录是 Snail-IoT 平台的长期维护文档，涵盖架构、部署、构建、账号与模块说明，并附可离线打开的独立 HTML 图表。

> 工作区总览请见上级 [README.md](file:///d:/DevProjects/snail/README.md)。
> 文档基于当前仓库代码（`iot-platform` 1.0.0 / `iot-vue-plus` 6.0.0 / `snail-ai` 1.1.1）重新核对生成。

---

## 一、核心文档

| 文档 | 内容 | 适用场景 |
| --- | --- | --- |
| [01-系统架构设计](file:///d:/DevProjects/snail/docs/01-系统架构设计.md) | 总体定位、技术栈、分层架构、运行拓扑、登录/采集/SSE/SSO/部署/AI 链路、权限模型、模块清单 | 了解整体设计 |
| [02-部署指南](file:///d:/DevProjects/snail/docs/02-部署指南.md) | Compose 两层编排（含网络名约定）、端口规划、启动顺序、环境变量、K8s dev/prod、db-init、Jenkins、GitHub Actions | 部署上线 / 运维 |
| [03-构建与开发指南](file:///d:/DevProjects/snail/docs/03-构建与开发指南.md) | 工程依赖、后端/前端构建、Maven Profile、配置说明、SQL 脚本、FAQ | 本地开发 |
| [04-账号与初始化说明](file:///d:/DevProjects/snail/docs/04-账号与初始化说明.md) | 全部默认账号、服务令牌、数据库初始化（Compose 手动 / K8s 自动）、安全建议 | 首次启动 / 初始化 |
| [05-模块功能说明](file:///d:/DevProjects/snail/docs/05-模块功能说明.md) | 12 个业务模块职责、HTTP 端点、核心类、前端页面地图 | 二次开发 |

---

## 二、可视化图表（独立 HTML）

图表为自包含 HTML（内嵌 SVG），**无需联网、双击即可打开**。

| 图表 | 说明 |
| --- | --- |
| [图表索引](file:///d:/DevProjects/snail/docs/diagrams/index.html) | 五个页面的导航入口，含端口速览、12 模块目录、账号速查 |
| [01-系统架构图](file:///d:/DevProjects/snail/docs/diagrams/01-系统架构图.html) | 六层架构、接入/应用层说明、12 个业务模块详解与依赖构建 |
| [02-部署拓扑图](file:///d:/DevProjects/snail/docs/diagrams/02-部署拓扑图.html) | 浏览器 → Nginx → 服务 → 中间件 → 数据层，含完整端口、启动顺序与 K8s 双环境 |
| [03-数据采集流程](file:///d:/DevProjects/snail/docs/diagrams/03-数据采集流程.html) | 设备上行 → EMQX → Kafka → ES / Redis 心跳 / 在线状态回写，附逐步验证命令 |
| [04-账号密码清单](file:///d:/DevProjects/snail/docs/diagrams/04-账号密码清单.html) | 平台、基础设施、控制台、服务令牌、K8s Secret、演示设备密钥与改密加固建议 |
| [05-从零搭建与验证](file:///d:/DevProjects/snail/docs/diagrams/05-从零搭建与验证.html) | 环境准备 → 中间件 → 数据库 → 构建 → 启动 → 逐项验证 → Docker/K8s → 排障 → 验收清单 |

> 每个页面左侧均带固定目录：上半部分用于跨页面跳转，下半部分为本页章节锚点；窄屏自动折叠为顶部横向导航。

---

## 三、子工程文档

| 文档 | 内容 |
| --- | --- |
| [iot-platform/README.md](file:///d:/DevProjects/snail/iot-platform/README.md) | 自研 IoT 产品：技术栈、工程结构、构建运行、端口 |
| [iot-platform/plus-ui/README.md](file:///d:/DevProjects/snail/iot-platform/plus-ui/README.md) | 前端工程：技术栈、目录、环境变量、构建部署 |
| [thirdparty/README.md](file:///d:/DevProjects/snail/thirdparty/README.md) | 框架底座与 AI 平台说明及维护约定 |
| [thirdparty/snail-ai/docs](file:///d:/DevProjects/snail/thirdparty/snail-ai/docs) | SnailAI 上游文档站（架构 / 部署 / 使用指南） |

---

## 四、本次文档刷新要点

- **Nginx**：主项目唯一使用 [nginx.conf](file:///d:/DevProjects/snail/iot-platform/plus-ui/nginx.conf)，随 `iot-platform/plus-ui:1.0.0` 镜像发布；`iot-web` 的 80 端口是唯一对外入口，统一反代后端与三大控制台，并内置 `/jenkins-api/` 代理。
- **Compose 网络**：根编排 [docker-compose.yml](file:///d:/DevProjects/snail/docker-compose.yml) 只声明 `postgres` / `redis`，**未声明 networks**；业务编排以 `external: true` 复用 `devprojects_default`。因此启动根编排需显式 `-p devprojects`（或设置 `COMPOSE_PROJECT_NAME=devprojects`），否则实际创建的是 `snail_default`，业务编排会报网络不存在。
- **采集落点**：Kafka 消费后写入 Elasticsearch 时序索引 `iot_device_data`（`easy-es.enable=false` 时跳过），同时刷新 Redis 心跳 `iot:device:online:{deviceCode}`（TTL 5 分钟）；**仅在首次心跳时**把设备在线状态回写 PostgreSQL，并非设备数据双写。
- **消息推送**：统一消息使用 SSE（`/resource/message`），WebSocket 未启用。
- **统一认证**：自研 Sa-Token OAuth2 Server + OIDC 元数据 + CAS Server/Client，票据落 Redis。
- **GitHub 项目同步**：`/iot/deploy/github` 支持公开仓库（无 Token）与私有仓库（`GITHUB_TOKEN`）两种模式，并可按账号授权可见项目。
- **K8s 双环境**：dev 为精简栈（postgres + redis + iot-server + iot-web），prod 为全量栈；前端入口 NodePort 分别为 30080 / 30081。
- **Ollama**：不在主编排内，仅存在于 `thirdparty/snail-ai/script/docker/docker-compose.full.yml`。

---

## 五、推荐阅读顺序

1. **新手入门**：[工作区总览](../README.md) → [系统架构设计](01-系统架构设计.md) → [从零搭建与验证](diagrams/05-从零搭建与验证.html) → [账号密码清单](diagrams/04-账号密码清单.html)
2. **动手部署**：[部署指南](02-部署指南.md) → [部署拓扑图](diagrams/02-部署拓扑图.html)
3. **参与开发**：[构建与开发指南](03-构建与开发指南.md) → [模块功能说明](05-模块功能说明.md) → [数据采集流程](diagrams/03-数据采集流程.html)
