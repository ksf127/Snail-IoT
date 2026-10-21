# ============================================================================
# Snail-IoT · Jenkins CI 镜像
# 自带：JDK21（基镜像）+ Maven + Node22/pnpm + Docker CLI + kubectl
# 用途：在容器内完成 后端/前端构建 → 镜像构建 → kubectl 部署（Docker Desktop k8s）
# ============================================================================
FROM jenkins/jenkins:lts-jdk21

USER root

ARG MAVEN_VERSION=3.9.9
ARG PNPM_VERSION=10.34.5
ARG NODE_MAJOR=22

# ---------------- 基础工具 ----------------
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates gnupg lsb-release \
 && rm -rf /var/lib/apt/lists/*

# ---------------- Maven ----------------
# 使用 archive 源以固定版本（dlcdn 会下线旧版本）
RUN curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/maven.tgz \
 && tar -xzf /tmp/maven.tgz -C /opt \
 && ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven \
 && rm -f /tmp/maven.tgz
ENV MAVEN_HOME=/opt/maven
ENV PATH=/opt/maven/bin:$PATH
# m2 落在 jenkins_home，随卷持久化（容器以 root 运行时 user.home=/root）
ENV MAVEN_OPTS="-Dmaven.repo.local=/var/jenkins_home/.m2/repository"

# ---------------- Node.js + pnpm ----------------
RUN curl -fsSL "https://deb.nodesource.com/setup_${NODE_MAJOR}.x" | bash - \
 && apt-get install -y --no-install-recommends nodejs \
 && rm -rf /var/lib/apt/lists/* \
 && npm i -g "pnpm@${PNPM_VERSION}" --registry=https://registry.npmmirror.com \
 && node -v && pnpm -v

# ---------------- Docker CLI（复用宿主 docker.sock 构建镜像） ----------------
# 直接使用 Debian 官方仓库（download.docker.com 在国内常被中断），只取客户端
RUN apt-get update \
 && (apt-get install -y --no-install-recommends docker-cli \
     || apt-get install -y --no-install-recommends docker.io) \
 && docker --version \
 && rm -rf /var/lib/apt/lists/*

# ---------------- kubectl（稳定版，按构建机架构选择） ----------------
RUN KUBECTL_VERSION="$(curl -fsSL https://dl.k8s.io/release/stable.txt)" \
 && ARCH="$(dpkg --print-architecture)" \
 && curl -fsSL -o /usr/local/bin/kubectl "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/${ARCH}/kubectl" \
 && chmod +x /usr/local/bin/kubectl \
 && kubectl version --client

# 允许 Jenkins 对 bind-mount 进容器的仓库执行 git 操作（规避 dubious ownership）
RUN git config --system --add safe.directory '*'

# ---------------- Jenkins 插件 ----------------
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

USER jenkins
