# plus-ui —— IoT 平台前端

> Snail-IoT Web 前端工程，基于 Vue 3 + TypeScript + Element Plus + Vite。

本工程是 [iot-platform](file:///d:/DevProjects/snail/iot-platform) 的独立前端，配套后端产品服务使用；由框架底座 [iot-vue-plus](file:///d:/DevProjects/snail/thirdparty/iot-vue-plus) 派生，工程 `package.json` 的 `name` 为 `iot-vue-plus`。

---

## 一、技术栈

| 类别 | 选型 | 版本 |
| --- | --- | --- |
| 框架 | Vue | 3.5.42 |
| 语言 | TypeScript | ^6.0.3 |
| 构建 | Vite | ^8.2.2 |
| UI 库 | Element Plus | 2.14.5 |
| 状态管理 | Pinia | 4.0.3 |
| 路由 | Vue Router | 5.3.1 |
| 表格 | vxe-table | 4.21.9 |
| 图表 | ECharts | 6.1.0 |
| 富文本 | wangEditor-next | 6.4.1 |
| 国际化 | vue-i18n | 11.4.10 |
| 请求 | Axios | 1.20.0 |
| 包管理 | pnpm | ≥ 10 |
| 代码检查 | oxlint + oxfmt | — |

---

## 二、目录结构

```
plus-ui/
├── src/
│   ├── api/            # 接口定义（按模块拆分 index.ts + types.ts）
│   │   ├── system/     #   用户/角色/菜单/字典/部门/岗位/参数/通知/OSS/客户端
│   │   ├── monitor/    #   缓存/在线用户/登录日志/操作日志
│   │   ├── workflow/   #   流程定义/实例/任务/分类/表达式
│   │   ├── tool/       #   代码生成
│   │   ├── ai/         #   AI 智能体
│   │   └── *.js        #   平台业务：product/device/app/dataset/group/
│   │                   #             archive/report/deploy/pay/monitor
│   ├── views/          # 页面（目录与 api 对应）
│   │   ├── product/ device/ app/ dataset/ group/     # 核心业务
│   │   ├── archive/ report/ deploy/ pay/             # 档案/报表/部署/支付
│   │   ├── monitor/    # 服务/设备监控 + admin/snailjob/snailai 嵌入
│   │   ├── system/     # 系统管理
│   │   ├── workflow/   # 工作流
│   │   ├── tool/gen/   # 代码生成
│   │   ├── ai/chat/    # AI 对话
│   │   ├── login.vue / register.vue / index.vue
│   ├── components/     # 通用组件（分页/上传/编辑器/图标选择/权限/流程组件等）
│   ├── layout/         # 布局（侧边栏/顶栏/标签页/设置）
│   ├── router/         # 路由（动态菜单由后端下发）
│   ├── store/          # Pinia 状态（app/dict/permission/settings/tagsView/user）
│   ├── hooks/          # 组合式函数（表格/表单/弹窗/树）
│   ├── utils/          # 工具（request/crypto/permission/iot 等）
│   ├── lang/           # 国际化（zh_CN / en_US）
│   ├── assets/styles/  # 样式（tokens / element-plus 覆盖 / 业务样式）
│   └── plugins/        # 插件注册（auth/cache/modal/download/tab）
├── .env.development    # 开发环境变量（VITE_APP_BASE_API=/dev-api）
├── .env.production     # 生产环境变量（VITE_APP_BASE_API=/prod-api）
├── vite.config.ts      # 构建配置（含代理）
└── package.json
```

---

## 三、环境变量

| 文件 | 变量 | 值 | 说明 |
| --- | --- | --- | --- |
| `.env.development` | `VITE_APP_BASE_API` | `/dev-api` | 开发代理前缀 |
| `.env.production` | `VITE_APP_BASE_API` | `/prod-api` | 生产由 Nginx 反代 |
| 两者 | `VITE_APP_ENCRYPT` | `true` | 请求报文加密开关 |
| 两者 | `VITE_APP_CLIENT_ID` | `e5cd7e4891bf95d1d19206ce24a7b32e` | OAuth2 客户端标识 |

---

## 四、开发与构建

```bash
# 安装依赖（推荐国内镜像）
pnpm install --registry=https://registry.npmmirror.com

# 启动开发服务器（默认 http://localhost:80）
pnpm dev

# 生产构建（输出 dist/）
pnpm build:prod

# 本地预览构建产物
pnpm preview

# 代码检查与格式化
pnpm lint
pnpm fmt
```

> 开发前请先启动后端服务；代理目标在 `vite.config.ts` 中配置，默认转发到后端网关地址。

---

## 五、部署

生产环境构建产物 `dist/` 由 [script/docker](../script/docker) 编排中的 `iot-web`（Nginx）容器托管，对外暴露 80 端口。Nginx 负责：

- 托管前端静态资源（SPA history 路由回退至 `index.html`）
- 反向代理 `/prod-api` 至后端服务

---

## 六、相关文档

| 文档 | 内容 |
| --- | --- |
| [iot-platform/README.md](file:///d:/DevProjects/snail/iot-platform/README.md) | 产品整体说明 |
| [构建与开发指南](file:///d:/DevProjects/snail/docs/03-构建与开发指南.md) | 前后端构建、环境变量、常见问题 |
| [模块功能说明](file:///d:/DevProjects/snail/docs/05-模块功能说明.md) | 各业务模块与前端页面地图 |
| [账号与初始化说明](file:///d:/DevProjects/snail/docs/04-账号与初始化说明.md) | 默认账号 `admin / admin123` |
