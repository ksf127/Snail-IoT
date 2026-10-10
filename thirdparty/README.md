# thirdparty —— 第三方 / 上游源码归档

本目录存放**框架底座与 AI 平台上游源码**，为 `../iot-platform` 自研产品提供基础能力与父 POM。二者均保持上游工程形态，**原则上不直接修改**，以保证后续可平滑升级。

---

## 一、目录一览

```
thirdparty/
├── iot-vue-plus/     # 框架底座（IoVue-Plus 6.0.0）——提供父 POM 与通用能力
└── snail-ai/         # AI 智能体平台（SnailAI 1.1.1）——提供对话 / RAG / Agent / MCP
```

| 目录 | 角色 | 技术栈 | 版本 | 许可证 |
| --- | --- | --- | --- | --- |
| [iot-vue-plus](file:///d:/DevProjects/snail/thirdparty/iot-vue-plus) | 框架底座，产品父 POM | Spring Boot 4.1.0 / JDK 21 / Jetty / MyBatis-Plus / Sa-Token / Redisson | 6.0.0 | MIT |
| [snail-ai](file:///d:/DevProjects/snail/thirdparty/snail-ai) | 企业级 AI 智能体平台 | Spring Boot 4 + Spring AI + gRPC + pgvector | 1.1.1 | Apache 2.0 + 附加协议 |

---

## 二、iot-vue-plus（框架底座）

### 2.1 与产品的关系

自研产品 [iot-platform](file:///d:/DevProjects/snail/iot-platform) 的产品父 POM 通过 Maven `<parent>` 继承本工程：

```xml
<parent>
    <groupId>org.dromara</groupId>
    <artifactId>iot-vue-plus</artifactId>
    <version>6.0.0</version>
    <relativePath/>
</parent>
```

因此**构建产品前必须先安装框架到本地仓库**：

```bash
cd thirdparty/iot-vue-plus
mvn -DskipTests clean install
```

### 2.2 模块结构

```
iot-vue-plus/
├── iot-admin/       # 启动入口（DromaraApplication + application*.yml + i18n + banner）
├── iot-api/         # 对外公共 API 定义
├── iot-common/      # 通用能力组件（BOM + 25 个子模块）
│   ├── iot-common-bom             # 依赖版本统一管理
│   ├── iot-common-core            # 核心工具与常量
│   ├── iot-common-web             # Web 层封装
│   ├── iot-common-satoken         # Sa-Token 认证
│   ├── iot-common-security        # 安全与权限
│   ├── iot-common-mybatis         # MyBatis-Plus 增强
│   ├── iot-common-redis           # Redisson 缓存
│   ├── iot-common-elasticsearch   # Easy-ES 集成
│   ├── iot-common-mqtt            # mica-mqtt 集成
│   ├── iot-common-job             # SnailJob 客户端
│   ├── iot-common-liteflow        # 规则编排
│   ├── iot-common-oss             # 对象存储（MinIO 等）
│   ├── iot-common-excel/json/log/mail/sms/push/...
│   └── ...
├── iot-extend/      # 扩展运行服务
│   ├── iot-monitor-admin          # Spring Boot Admin 监控
│   ├── iot-snailjob-server        # SnailJob 调度中心
│   └── iot-snailai-server         # SnailAI 服务端
├── iot-modules/     # 通用业务模块
│   ├── iot-system                 # 用户/角色/菜单/字典/租户
│   ├── iot-gen                    # 代码生成器
│   ├── iot-job                    # 定时任务
│   ├── iot-workflow               # warm-flow 工作流
│   ├── iot-ai                     # AI 能力接入
│   └── iot-demo                   # 示例
└── script/          # 部署脚本与 SQL（multidb：mysql/postgres/oracle/sqlserver）
```

### 2.3 关键版本

| 组件 | 版本 |
| --- | --- |
| Spring Boot | 4.1.0 |
| JDK | 21 |
| MyBatis-Plus | 3.5.17 |
| Sa-Token | 1.45.0 |
| Easy-ES | 3.0.2 |
| SnailJob | 2.0.2 |
| SnailAI | 1.1.1 |
| warm-flow | 1.8.9 |
| LiteFlow | 2.16.1.2 |
| mica-mqtt | 2.6.8 |

---

## 三、snail-ai（AI 智能体平台）

### 3.1 能力概览

- **对话 / Agent**：大模型对话、Agent 编排、工具调用（Function Calling）
- **RAG**：知识库、文档解析（Docling / PaddleOCR）、分块、向量检索与问答
- **MCP**：Model Context Protocol 工具接入（SSE / stdio 传输）
- **模型管理**：Chat / Embedding / Rerank 多模型供应商接入
- **OpenAPI**：对外提供鉴权与对话接口，便于第三方系统接入

### 3.2 模块结构

```
snail-ai/
├── snail-ai-commons/     # 公共基础
│   ├── snail-ai-commons-core
│   └── snail-ai-commons-grpc
├── snail-ai-models/      # 模型能力
│   ├── snail-ai-model-chat
│   ├── snail-ai-model-common
│   ├── snail-ai-model-embedding
│   └── snail-ai-model-rerank
├── snail-ai-server/      # 服务端
│   ├── snail-ai-server-admin
│   ├── snail-ai-server-features
│   ├── snail-ai-server-openapi
│   └── snail-ai-server-persistence
├── snail-ai-agent/       # 智能体运行时
│   ├── snail-ai-agent-chat
│   ├── snail-ai-agent-common
│   ├── snail-ai-agent-executor
│   └── snail-ai-agent-openapi
├── snail-ai-starter/     # 启动入口（内嵌管理后台静态页）
├── snail-ai-agent-example/  # 接入示例
├── docs/                 # VitePress 文档站（架构 / 部署 / 使用指南）
├── script/               # 部署编排与多数据库 SQL
└── tools/                # Docling / PaddleOCR 解析服务
```

### 3.3 内部文档

上游工程自带完整文档，可直接参考（无需重复）：

- [snail-ai/README.md](file:///d:/DevProjects/snail/thirdparty/snail-ai/README.md)
- `snail-ai/docs/` —— VitePress 站点源码（`architecture/`、`deploy/`、`guide/`、`api/`）

---

## 四、维护约定

1. **不直接修改上游源码**：产品定制逻辑一律放入 `iot-platform/`，保持 thirdparty 干净，便于跟随上游升级。
2. **升级流程**：
   - 更新 `thirdparty/iot-vue-plus` 源码 → 改根 `pom.xml` 中的 `<revision>` → `mvn -DskipTests clean install`
   - 回到 `iot-platform/` 重新构建，处理编译报错（通常是 API 变更）
3. **父 POM 版本对齐**：`iot-platform/pom.xml` 的 `<parent>` 版本必须与框架 `revision` 一致。
4. **构建沙箱参数**（Windows 受限环境）：

```bash
mvn -DskipTests clean install "-Dmaven.repo.local=C:\Users\ThinkPad\.m2\repository" "-Duser.home=D:\DevProjects\snail\.mvn-home"
```

---

## 五、相关文档

| 文档 | 内容 |
| --- | --- |
| [工作区总览](file:///d:/DevProjects/snail/README.md) | 整体结构与快速开始 |
| [系统架构设计](file:///d:/DevProjects/snail/docs/01-系统架构设计.md) | 分层架构与技术选型 |
| [构建与开发指南](file:///d:/DevProjects/snail/docs/03-构建与开发指南.md) | 框架前置安装、产品构建 |
| [iot-platform/README.md](file:///d:/DevProjects/snail/iot-platform/README.md) | 自研产品说明 |
