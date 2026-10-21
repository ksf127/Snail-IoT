# Snail-IoT

> 一站式物联网（IoT）平台工作区：设备接入与采集、产品/设备管理、统一认证、报表、自动化部署、聚合支付、工单管理、云平台纳管、任务调度，以及配套的 AI 智能体与管理后台框架。

本仓库是一个**多工程并列工作区**，根目录只负责编排（基础设施 Docker Compose、向量初始化脚本、文档），业务代码分布在两个子工程目录中。

---

## 一、仓库结构

```
snail/
├── docker-compose.yml          # 根基础设施编排（PostgreSQL/pgvector + Redis）
├── init-vector.sql             # PostgreSQL 初始化：pgvector 扩展与向量表
├── README.md                   # 本文件（工作区总览）
├── docs/                       # 平台文档（长期维护，含独立 HTML 图表）
│   ├── 01-系统架构设计.md
│   ├── 02-部署指南.md
│   ├── 03-构建与开发指南.md
│   ├── 04-账号与初始化说明.md
│   ├── 05-模块功能说明.md
│   └── diagrams/               # 独立 HTML 图表（可离线打开）
│
├── iot-platform/               # ① 自研 IoT 业务产品（Spring Boot 4 + Vue3）
│   ├── pom.xml                 # 产品聚合 POM（继承 iot-vue-plus 框架父 POM）
│   ├── iot-platform-common/    # 通用常量与枚举
│   ├── iot-platform-modules/   # 业务模块聚合（device/permission/collect/...）
│   ├── iot-platform-admin/     # 启动入口（Spring Boot 主类 + 配置 + 安全）
│   ├── plus-ui/                # 前端工程（Vue3 + TS + Element Plus + Vite）
│   └── script/                 # 部署编排与 SQL
│
└── thirdparty/                 # ② 第三方/上游源码归档（框架底座）
    ├── iot-vue-plus/           # IoVue-Plus 6.0.0 框架（提供父 POM 与通用能力）
    └── snail-ai/               # SnailAI 企业级 AI 智能体平台
```

> 命名约定：自研产品统一使用 `iot-platform-*` / `plus-ui`；框架底座统一使用 `iot-*`。二者均为 `org.dromara.*` Java 包名。

---

## 二、子工程一览

| 目录 | 角色 | 技术栈 | 版本 |
| --- | --- | --- | --- |
| [iot-platform](file:///d:/DevProjects/snail/iot-platform) | 自研 IoT 业务产品 | Spring Boot 4.1.0 / JDK 21 / MyBatis-Plus / Sa-Token / Easy-ES / Kafka / MQTT | 1.0.0 |
| [thirdparty/iot-vue-plus](file:///d:/DevProjects/snail/thirdparty/iot-vue-plus) | 框架底座（提供父 POM、通用模块、监控/调度/代码生成） | Spring Boot 4.1.0 / JDK 21 / Jetty / Redisson | 6.0.0 |
| [thirdparty/snail-ai](file:///d:/DevProjects/snail/thirdparty/snail-ai) | AI 智能体平台（RAG / Agent / MCP） | Spring Boot 4 + Spring AI + gRPC | 1.1.1 |

**依赖关系**：`iot-platform` 的产品父 POM 通过 `<parent>` 继承 `org.dromara:iot-vue-plus:6.0.0`，因此**必须先安装框架**（`mvn install`）后，产品工程才能构建。

---

## 三、快速开始

### 3.1 前置条件

- JDK 21、Maven 3.9+
- Node.js ≥ 20.19（推荐 v22）+ pnpm ≥ 10
- Docker Desktop（含 Compose v2）

### 3.2 三步启动（Docker 统一编排）

```bash
# 1) 启动基础设施（PostgreSQL + Redis），创建 devprojects_default 网络
docker compose -f docker-compose.yml up -d

# 2) 构建框架底座并安装到本地仓库（产品依赖它）
cd thirdparty/iot-vue-plus && mvn -DskipTests clean install

# 3) 构建产品后端 + 启动全部业务容器
cd ../../iot-platform && mvn -Pprod -DskipTests clean package
docker compose -f script/docker/docker-compose.yml up -d --build
```

启动完成后访问 **http://localhost**，使用 `admin / admin123` 登录。

> Windows 沙箱环境构建时，Maven 需追加 `"-Dmaven.repo.local=C:\Users\ThinkPad\.m2\repository" "-Duser.home=D:\DevProjects\snail\.mvn-home"`。详见 [构建与开发指南](file:///d:/DevProjects/snail/docs/03-构建与开发指南.md)。

### 3.3 默认账号

| 系统 | 地址 | 账号 / 密码 |
| --- | --- | --- |
| IoT 平台前端 | http://localhost | `admin` / `admin123` |
| Nacos 控制台 | http://localhost:8848/nacos | `nacos` / `123456` |
| EMQX Dashboard | http://localhost:18083 | `admin` / `public` |
| MinIO 控制台 | http://localhost:9001 | `ruoyi` / `ruoyi123` |
| PostgreSQL | localhost:5432（库 `ruoyi`） | `postgres` / `postgres` |
| Redis | localhost:6379 | 密码 `123456` |
| Spring Boot Admin | http://localhost/admin（监控） | `admin` / `admin123` |
| SnailJob | http://localhost/snail-job（调度） | `admin` / `admin123` |
| SnailAI | http://localhost/snail-ai（AI 控制台） | `admin` / `admin123` |

完整说明见 [账号与初始化说明](file:///d:/DevProjects/snail/docs/04-账号与初始化说明.md)。

---

## 四、文档索引

| 文档 | 内容 |
| --- | --- |
| [01-系统架构设计](file:///d:/DevProjects/snail/docs/01-系统架构设计.md) | 总体架构、技术选型、数据链路、模块划分 |
| [02-部署指南](file:///d:/DevProjects/snail/docs/02-部署指南.md) | 基础设施、容器编排、启动顺序、故障排查 |
| [03-构建与开发指南](file:///d:/DevProjects/snail/docs/03-构建与开发指南.md) | 后端/前端本地开发、构建命令、常见问题 |
| [04-账号与初始化说明](file:///d:/DevProjects/snail/docs/04-账号与初始化说明.md) | 全部默认账号、数据库初始化、SQL 脚本 |
| [05-模块功能说明](file:///d:/DevProjects/snail/docs/05-模块功能说明.md) | 各业务模块职责、接口分组、前端页面 |
| [HTML 图表](file:///d:/DevProjects/snail/docs/diagrams) | 架构图 / 部署拓扑 / 数据采集流程（可离线打开） |

---

## 五、平台能力总览

- **设备接入与采集**：MQTT（EMQX）设备上行 → Kafka 缓冲 → 落库 PostgreSQL / 写入 Elasticsearch 供时序检索
- **产品与设备管理**：产品、设备、物模型、分组；设备在线状态与监控
- **三线权限模型**：分组（数据线）、数据集（数据线）、应用（服务线）交汇授权
- **统一认证（SSO）**：CAS / OAuth2 / OIDC 一体化，支持第三方客户端接入
- **档案 / 报表 / 自动化部署**：业务档案、报表导出、部署流水
- **聚合支付**：统一下单、回调、退款，渠道工厂可插拔（Mock / 微信 / 支付宝），内置虚拟商品货架
- **工单管理**：普通工单 + LiteFlow 规则引擎 + Warm-Flow 流程引擎
- **云平台纳管**：Provider 可插拔，内置阿里云 ECS 与 Flink 作业纳管
- **任务调度**：本地 Cron/固定频率调度器 + 执行器 SPI，并可对接 SnailJob 分布式调度
- **AI 智能体**：集成 SnailAI，提供对话、RAG 知识库、MCP 工具调用
- **监控**：Spring Boot Admin 应用监控中心

---

## 六、许可证

各子工程遵循各自的许可证：
- `iot-platform`：见 `iot-platform/` 下说明
- `thirdparty/iot-vue-plus`：MIT（见其 `LICENSE`）
- `thirdparty/snail-ai`：Apache 2.0 + 附加协议（见其 `LICENSE`）
