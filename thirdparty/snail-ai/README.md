<p align="center">
  <a href="https://github.com/aizuda/snail-ai">
   <img alt="Snail-AI-Logo" src="docs/images/favicon.svg" width="200px">
  </a>
</p>

<p align="center">
     🔥🔥🔥 灵活，易用和可扩展的企业级 AI 智能体平台<br><br/>
</p>

<p align="center">

> ✅️ 基于 Spring Boot 4 + Spring AI，开箱即用的多模型、多 Agent 管理平台 <br/>
> ✅️ 支持 RAG、记忆、技能编排、向量检索等核心 AI 能力
>
</p>

# 使用前务必先了解版权

Aizuda/SnailAI 采用 [APACHE LICENSE 2.0](LICENSE) 开源协议，您在使用过程中，需要注意以下几点:

⭕本项目 双协议授权 默认采用 Apache-2.0 协议开源 完全开放 遵守附加协议 的前提下允许任何目的商用场景（不包含扩散到第三方例如：提供给第三方源码、提供SaaS服务给第三方）。 如果违反 附加协议 自动升级为 AGPL-3.0 协议。

🔴附加协议：使用方在项目介绍中显著位置必须标注版权信息（链接仓库地址），不允许删除源码注释申明，不允许山寨换皮商用与官方企业版竞争（包含二次开源），否则视为侵权（索赔200万）。

使用必须遵守国家法律法规，⛔不允许非法项目使用，后果自负❗

# 简介

> Snail AI 是一个灵活、可扩展的企业级 AI 智能体平台。基于 Spring Boot 4 和 Spring AI 构建，提供多模型管理、智能体编排、RAG 知识库、长期记忆、技能管理等核心能力。拥有完善的后台管理界面和 OpenAPI 接口，支持快速接入和二次开发。

# 开源地址
- [snail-job-admin](https://gitee.com/opensnail/snail-job-admin.git)(基于[soybean-admin](https://github.com/soybeanjs/soybean-admin)开源项目搭建)
- [snail-job](https://gitee.com/aizuda/snail-job.git)
- [snail-ai-admin](https://gitee.com/opensnail/snail-ai-admin.git)
- [snail-ai-chat](https://gitee.com/opensnail/snail-ai-chat.git)

# 核心特性

- **多模型管理** — 统一接入和管理多种大语言模型（LLM），灵活切换
- **智能体（Agent）** — 可编排、可扩展的 Agent 框架，支持多 Agent 协作
- **RAG 知识库** — 基于向量检索的检索增强生成，支持 Milvus / Elasticsearch
- **长期记忆** — Agent 对话记忆持久化，提升上下文连贯性
- **技能管理** — 可配置的技能（Skill）体系，扩展 Agent 能力边界
- **资源管理** — 文档解析（PDF、Office）、对象存储（MinIO）
- **OpenAPI** — 标准化 API 接口，方便第三方系统集成
- **后台管理** — 基于 SoybeanAdmin 的现代化管理界面

# 快速开始
- [QuickStart](https://snailai.opensnail.com/guide/quick-start.html)

# 核心文档（运维 / 二次开发必读）

> 📚 统一文档导航：[docs/delivery.md](docs/delivery.md) — 按角色（架构师 / 后端 / 前端 / Prompt 工程师 / DBA）推荐阅读路径
>
> 📁 所有交付物文档已生成在 `/docs` 目录下，按编号顺序阅读：

| # | 文档 | 内容要点 |
|---|------|---------|
| 01 | [📐 技术架构设计](docs/01-技术架构设计.md) | 技术栈选型、分层架构、整体组件图 + SSE 时序图、AI 调度策略 |
| 02 | [🚀 启动验证与功能说明](docs/02-启动验证与功能说明.md) | 环境依赖、启动 SOP、模块功能介绍、3 个冒烟测试 Prompt |
| 03 | [🛠 搭建与部署指南](docs/03-搭建与部署指南.md) | 搭建 SOP、docker-compose、5 个致命问题排查 |
| 04 | [🧠 AI 核心链路与 Prompt 设计](docs/04-AI核心链路与Prompt设计.md) | 模型选型、Prompt 模板库、RAG 全链路、幻觉控制 |
| 05 | [📡 API 接口与数据字典](docs/05-API接口与数据字典.md) | 核心 API 列表（含 SSE）、数据流、数据库表结构、向量库设计 |

> 在线版文档站点：[https://snailai.opensnail.com](https://snailai.opensnail.com)

# 期望

欢迎提出更好的意见，帮助完善 Snail AI。

## 系统截图

|                                                                                              |                                                                                              |
|----------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| ![系统截图](https://foruda.gitee.com/images/1778548151052200487/8110b826_5705488.png "屏幕截图") | ![系统截图](https://foruda.gitee.com/images/1778548259929534435/4a637132_5705488.png "屏幕截图") |
| ![系统截图](https://foruda.gitee.com/images/1778548393302983265/7f62b626_5705488.png "屏幕截图") | ![系统截图](https://foruda.gitee.com/images/1778548415201930078/b3def0ad_5705488.png "屏幕截图") |
| ![系统截图](https://foruda.gitee.com/images/1778548458453654504/5a955292_5705488.png "屏幕截图") | ![系统截图](https://foruda.gitee.com/images/1778548483651093760/8827bbaa_5705488.png "屏幕截图") |
| ![系统截图](https://foruda.gitee.com/images/1778548510660280272/c582410f_5705488.png "屏幕截图") | ![系统截图](https://foruda.gitee.com/images/1778548539192977119/0d476c6b_5705488.png "屏幕截图") |
| ![系统截图](https://foruda.gitee.com/images/1778548554716070174/5d13562e_5705488.png "屏幕截图") | ![系统截图](https://foruda.gitee.com/images/1778548585615181750/26aa8967_5705488.png "屏幕截图") |
| ![系统截图](https://foruda.gitee.com/images/1778548596937179249/ff95b81b_5705488.png "屏幕截图") | ![系统截图](https://foruda.gitee.com/images/1778548616880644283/e1585de4_5705488.png "屏幕截图") |
| ![系统截图](https://foruda.gitee.com/images/1778548634259005753/d30e9255_5705488.png "屏幕截图") | ![系统截图](https://foruda.gitee.com/images/1778548846501718976/b325242f_5705488.png "屏幕截图")     |

