# iot-vue-plus 官方前端（plus-ui）多阶段构建
# 使用 glibc 基础镜像（node:slim）以兼容 rolldown-vite 的原生二进制
FROM node:22-slim AS builder
WORKDIR /app

# 安装 pnpm（版本与 package.json 的 packageManager 保持一致）
RUN npm i -g pnpm@10.34.5 --registry=https://registry.npmmirror.com

# 先拷贝依赖清单以利用构建缓存
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile \
    --registry=https://registry.npmmirror.com \
    --config.manage-package-manager-versions=false

# 拷贝源码并构建生产产物
COPY . .
RUN pnpm run build:prod

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
