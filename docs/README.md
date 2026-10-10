# 平台文档索引

本目录是 Snail IoT 平台的长期维护文档，涵盖架构、部署、构建、账号与模块说明，并附可离线打开的独立 HTML 图表。

> 工作区总览请见上级 [README.md](file:///d:/DevProjects/snail/README.md)。

---

## 一、核心文档

| 文档 | 内容 | 适用场景 |
| --- | --- | --- |
| [01-系统架构设计](file:///d:/DevProjects/snail/docs/01-系统架构设计.md) | 总体定位、技术栈、分层架构、数据链路、权限模型、模块清单 | 了解整体设计 |
| [02-部署指南](file:///d:/DevProjects/snail/docs/02-部署指南.md) | 两层 Compose、端口规划、启动顺序、环境变量、故障排查 | 部署上线 / 运维 |
| [03-构建与开发指南](file:///d:/DevProjects/snail/docs/03-构建与开发指南.md) | 工程依赖、后端/前端构建、沙箱参数、代码约定、FAQ | 本地开发 |
| [04-账号与初始化说明](file:///d:/DevProjects/snail/docs/04-账号与初始化说明.md) | 全部默认账号、数据库初始化、SQL 导入、安全建议 | 首次启动 / 初始化 |
| [05-模块功能说明](file:///d:/DevProjects/snail/docs/05-模块功能说明.md) | 各业务模块职责、HTTP 前缀、核心类、前端页面地图 | 二次开发 |

---

## 二、可视化图表（独立 HTML）

图表为自包含 HTML（内嵌 SVG），**无需联网、双击即可打开**。

| 图表 | 说明 |
| --- | --- |
| [图表索引](file:///d:/DevProjects/snail/docs/diagrams/index.html) | 三张图表的导航入口 |
| [01-系统架构图](file:///d:/DevProjects/snail/docs/diagrams/01-系统架构图.html) | 六层架构与模块分布 |
| [02-部署拓扑图](file:///d:/DevProjects/snail/docs/diagrams/02-部署拓扑图.html) | 浏览器 → 服务 → 中间件 → 数据层 |
| [03-数据采集流程](file:///d:/DevProjects/snail/docs/diagrams/03-数据采集流程.html) | 设备上行到 Kafka/ES/PG 的完整链路 |

---

## 三、子工程文档

| 文档 | 内容 |
| --- | --- |
| [iot-platform/README.md](file:///d:/DevProjects/snail/iot-platform/README.md) | 自研 IoT 产品：技术栈、工程结构、构建运行、端口 |
| [iot-platform/plus-ui/README.md](file:///d:/DevProjects/snail/iot-platform/plus-ui/README.md) | 前端工程：技术栈、目录、环境变量、构建部署 |
| [thirdparty/README.md](file:///d:/DevProjects/snail/thirdparty/README.md) | 框架底座与 AI 平台说明及维护约定 |
| [thirdparty/snail-ai/docs](file:///d:/DevProjects/snail/thirdparty/snail-ai/docs) | SnailAI 上游文档站（架构 / 部署 / 使用指南） |

---

## 四、推荐阅读顺序

1. **新手入门**：[工作区总览](../README.md) → [系统架构设计](01-系统架构设计.md) → [账号与初始化说明](04-账号与初始化说明.md)
2. **动手部署**：[部署指南](02-部署指南.md) → [部署拓扑图](diagrams/02-部署拓扑图.html)
3. **参与开发**：[构建与开发指南](03-构建与开发指南.md) → [模块功能说明](05-模块功能说明.md) → [数据采集流程](diagrams/03-数据采集流程.html)
