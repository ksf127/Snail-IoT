-- ============================================================================
-- Snail-IoT - 自动化部署（阶段四）
--
-- 使用前提：
--   1. 已导入 iot 基础脚本
--   2. 已导入 iot_platform.sql（含"Snail-IoT"一级目录与"自动化部署"菜单 1900000000000000008）
--
-- 执行方式：
--   docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_deploy.sql
--
-- 说明：
--   - 菜单与按钮权限（iot:deploy:list / release / query）已随 iot_platform.sql 初始化，
--     本脚本仅负责部署任务表；
--   - 状态机：0待执行 -> 1执行中 -> 2成功；执行中 -> 3失败。
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 部署任务表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_deploy_task;
CREATE TABLE iot_deploy_task
(
    task_id     BIGINT       NOT NULL,
    app_code    VARCHAR(64)  NOT NULL,
    version     VARCHAR(64)  NOT NULL,
    target_env  VARCHAR(32)  NOT NULL,
    task_type    VARCHAR(20)           DEFAULT 'deploy',
    build_number INT,
    status      CHAR(1)               DEFAULT '0',
    deploy_log  TEXT,
    start_time  TIMESTAMP,
    finish_time TIMESTAMP,
    del_flag    CHAR(1)               DEFAULT '0',
    remark      VARCHAR(500)          DEFAULT '',
    create_dept BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP,
    update_by   BIGINT,
    update_time TIMESTAMP,
    PRIMARY KEY (task_id)
);
CREATE INDEX idx_iot_deploy_task_app_env ON iot_deploy_task (app_code, target_env);
COMMENT ON TABLE iot_deploy_task IS '部署任务表';
COMMENT ON COLUMN iot_deploy_task.task_id     IS '部署任务ID';
COMMENT ON COLUMN iot_deploy_task.app_code    IS '应用编码';
COMMENT ON COLUMN iot_deploy_task.version     IS '版本号';
COMMENT ON COLUMN iot_deploy_task.target_env  IS '目标环境（dev/test/prod）';
COMMENT ON COLUMN iot_deploy_task.task_type    IS '任务类型（deploy发布 build构建）';
COMMENT ON COLUMN iot_deploy_task.build_number IS 'Jenkins 构建号（构建任务成功后可据此下载 JAR）';
COMMENT ON COLUMN iot_deploy_task.status      IS '状态（0待执行 1执行中 2成功 3失败）';
COMMENT ON COLUMN iot_deploy_task.deploy_log  IS '部署日志（含各阶段时间戳）';
COMMENT ON COLUMN iot_deploy_task.start_time  IS '开始执行时间';
COMMENT ON COLUMN iot_deploy_task.finish_time IS '执行完成时间';
COMMENT ON COLUMN iot_deploy_task.del_flag    IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_deploy_task.remark      IS '备注';
COMMENT ON COLUMN iot_deploy_task.create_dept IS '创建部门';
COMMENT ON COLUMN iot_deploy_task.create_by   IS '创建者';
COMMENT ON COLUMN iot_deploy_task.create_time IS '创建时间';
COMMENT ON COLUMN iot_deploy_task.update_by   IS '更新者';
COMMENT ON COLUMN iot_deploy_task.update_time IS '更新时间';

-- ----------------------------------------------------------------------------
-- 2. 部署应用登记表
--    发布时从中选择应用编码，仓库地址与分支供 CI/CD 拉取代码使用。
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_deploy_app;
CREATE TABLE iot_deploy_app
(
    app_id      BIGINT       NOT NULL,
    app_code    VARCHAR(64)  NOT NULL,
    app_name    VARCHAR(128) NOT NULL,
    repo_url    VARCHAR(255),
    repo_branch VARCHAR(64)           DEFAULT 'main',
    build_type  VARCHAR(32)           DEFAULT 'maven',
    build_command   TEXT,
    build_workdir   VARCHAR(255)          DEFAULT '.',
    dockerfile_path VARCHAR(255)          DEFAULT 'Dockerfile',
    artifact    VARCHAR(128),
    jar_path    VARCHAR(255),
    k8s_workload VARCHAR(128),
    k8s_container VARCHAR(128),
    status      CHAR(1)               DEFAULT '0',
    sort_no     INT                   DEFAULT 0,
    del_flag    CHAR(1)               DEFAULT '0',
    remark      VARCHAR(500)          DEFAULT '',
    create_dept BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP,
    update_by   BIGINT,
    update_time TIMESTAMP,
    PRIMARY KEY (app_id)
);
-- 仅约束未删除记录，允许删除后重新登记同一编码
CREATE UNIQUE INDEX uk_iot_deploy_app_code ON iot_deploy_app (app_code) WHERE del_flag = '0';
COMMENT ON TABLE iot_deploy_app IS '部署应用登记表';
COMMENT ON COLUMN iot_deploy_app.app_id      IS '应用ID';
COMMENT ON COLUMN iot_deploy_app.app_code    IS '应用编码（唯一）';
COMMENT ON COLUMN iot_deploy_app.app_name    IS '应用名称';
COMMENT ON COLUMN iot_deploy_app.repo_url    IS '代码仓库地址';
COMMENT ON COLUMN iot_deploy_app.repo_branch IS '代码分支';
COMMENT ON COLUMN iot_deploy_app.build_type  IS '构建方式（maven/node）';
COMMENT ON COLUMN iot_deploy_app.build_command   IS '构建命令（在构建目录内执行，留空则跳过源码构建）';
COMMENT ON COLUMN iot_deploy_app.build_workdir   IS '构建目录（相对代码根目录，默认 .）';
COMMENT ON COLUMN iot_deploy_app.dockerfile_path IS 'Dockerfile 路径（相对代码根目录）';
COMMENT ON COLUMN iot_deploy_app.artifact    IS '制品/镜像名称（不含 tag，构建时传入版本作为 tag）';
COMMENT ON COLUMN iot_deploy_app.jar_path    IS '构建产物 JAR 路径（相对代码根目录，支持通配符，留空表示不归档 JAR）';
COMMENT ON COLUMN iot_deploy_app.k8s_workload  IS 'Kubernetes 工作负载名称（Deployment）';
COMMENT ON COLUMN iot_deploy_app.k8s_container IS 'Kubernetes 容器名称（留空取首个容器）';
COMMENT ON COLUMN iot_deploy_app.status      IS '状态（0启用 1停用）';
COMMENT ON COLUMN iot_deploy_app.sort_no     IS '排序号（越小越靠前）';
COMMENT ON COLUMN iot_deploy_app.del_flag    IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_deploy_app.remark      IS '备注';
COMMENT ON COLUMN iot_deploy_app.create_dept IS '创建部门';
COMMENT ON COLUMN iot_deploy_app.create_by   IS '创建者';
COMMENT ON COLUMN iot_deploy_app.create_time IS '创建时间';
COMMENT ON COLUMN iot_deploy_app.update_by   IS '更新者';
COMMENT ON COLUMN iot_deploy_app.update_time IS '更新时间';

-- ----------------------------------------------------------------------------
-- 3. 部署环境登记表
--    每个环境一个独立 Kubernetes 命名空间，前端入口使用互不冲突的 NodePort。
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_deploy_env;
CREATE TABLE iot_deploy_env
(
    env_id        BIGINT       NOT NULL,
    env_code      VARCHAR(32)  NOT NULL,
    env_name      VARCHAR(64)  NOT NULL,
    k8s_namespace VARCHAR(64)  NOT NULL,
    web_node_port INT,
    cluster_name  VARCHAR(64)           DEFAULT 'docker-desktop',
    status        CHAR(1)               DEFAULT '0',
    sort_no       INT                   DEFAULT 0,
    del_flag      CHAR(1)               DEFAULT '0',
    remark        VARCHAR(500)          DEFAULT '',
    create_dept   BIGINT,
    create_by     BIGINT,
    create_time   TIMESTAMP,
    update_by     BIGINT,
    update_time   TIMESTAMP,
    PRIMARY KEY (env_id)
);
-- 仅约束未删除记录，允许删除后重新登记同一编码
CREATE UNIQUE INDEX uk_iot_deploy_env_code ON iot_deploy_env (env_code) WHERE del_flag = '0';
COMMENT ON TABLE iot_deploy_env IS '部署环境登记表';
COMMENT ON COLUMN iot_deploy_env.env_id        IS '环境ID';
COMMENT ON COLUMN iot_deploy_env.env_code      IS '环境编码（唯一，dev/test/prod）';
COMMENT ON COLUMN iot_deploy_env.env_name      IS '环境名称';
COMMENT ON COLUMN iot_deploy_env.k8s_namespace IS 'Kubernetes 命名空间';
COMMENT ON COLUMN iot_deploy_env.web_node_port IS '前端服务 NodePort 端口';
COMMENT ON COLUMN iot_deploy_env.cluster_name  IS '集群名称';
COMMENT ON COLUMN iot_deploy_env.status        IS '状态（0启用 1停用）';
COMMENT ON COLUMN iot_deploy_env.sort_no       IS '排序号（越小越靠前）';
COMMENT ON COLUMN iot_deploy_env.del_flag      IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_deploy_env.remark        IS '备注';
COMMENT ON COLUMN iot_deploy_env.create_dept   IS '创建部门';
COMMENT ON COLUMN iot_deploy_env.create_by     IS '创建者';
COMMENT ON COLUMN iot_deploy_env.create_time   IS '创建时间';
COMMENT ON COLUMN iot_deploy_env.update_by     IS '更新者';
COMMENT ON COLUMN iot_deploy_env.update_time   IS '更新时间';

-- ----------------------------------------------------------------------------
-- 4. 种子数据：登记本项目与开发/生产环境
--    - 一个应用对应一个可构建、可部署的单元（后端 / 前端分开登记）；
--    - repo_url 支持 Git 地址，也支持 Jenkins 容器内的本地挂载路径（以 / 开头），
--      本地路径会直接构建工作区，未提交的改动同样生效；
--    - build_command 在 build_workdir 下执行，留空表示镜像内完成构建（如前端多阶段 Dockerfile）。
-- ----------------------------------------------------------------------------
DELETE FROM iot_deploy_app WHERE app_id BETWEEN 1900000000000008001 AND 1900000000000008099;

INSERT INTO iot_deploy_app (app_id, app_code, app_name, repo_url, repo_branch, build_type,
                            build_command, build_workdir, dockerfile_path,
                            artifact, jar_path, k8s_workload, k8s_container,
                            status, sort_no, del_flag, remark,
                            create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000008001, 'iot-server', 'IoT 后端服务',
        '/workspace/snail', 'main', 'maven',
        'mvn -B -DskipTests -f thirdparty/iot-vue-plus/pom.xml clean install && mvn -B -Pprod -DskipTests -f iot-platform/pom.xml clean package',
        '.', 'iot-platform/iot-platform-admin/Dockerfile',
        'iot-platform/iot-platform-admin',
        'iot-platform/iot-platform-admin/target/iot-platform-admin.jar',
        'iot-server', 'iot-server',
        '0', 10, '0', 'Spring Boot 后端：Maven 多模块编译 + 镜像 + 滚动发布',
        103, 1, now(), 1, now()),
       (1900000000000008002, 'iot-web', 'IoT 管理前端',
        '/workspace/snail', 'main', 'node',
        '',
        '.', 'iot-platform/plus-ui/Dockerfile',
        'iot-platform/plus-ui', '',
        'iot-web', 'iot-web',
        '0', 20, '0', 'Vue3 前端：由 Dockerfile 多阶段构建（pnpm install + build:prod）',
        103, 1, now(), 1, now());

DELETE FROM iot_deploy_env WHERE env_id BETWEEN 1900000000000008101 AND 1900000000000008199;

INSERT INTO iot_deploy_env (env_id, env_code, env_name, k8s_namespace, web_node_port, cluster_name,
                            status, sort_no, del_flag, remark,
                            create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000008101, 'dev', '开发环境', 'iot-platform-dev', 30080, 'docker-desktop',
        '0', 10, '0', '推送 main 分支后自动部署，入口 http://localhost:30080',
        103, 1, now(), 1, now()),
       (1900000000000008102, 'prod', '生产环境', 'iot-platform-prod', 30081, 'docker-desktop',
        '0', 20, '0', '手动触发发布，入口 http://localhost:30081',
        103, 1, now(), 1, now());

-- ----------------------------------------------------------------------------
-- 5. GitHub 项目表与账号授权表
--    从 GitHub 公开 API 同步仓库列表；按账号授权，普通账号仅能看到已授权的项目。
--    使用 IF NOT EXISTS，可单独重复执行本节而不影响部署数据。
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS iot_github_repo
(
    repo_id        BIGINT       NOT NULL,
    owner_login    VARCHAR(128) NOT NULL,
    repo_name      VARCHAR(255) NOT NULL,
    full_name      VARCHAR(255) NOT NULL,
    html_url       VARCHAR(500),
    clone_url      VARCHAR(500),
    description    TEXT,
    language       VARCHAR(64),
    default_branch VARCHAR(128),
    private_flag   CHAR(1)               DEFAULT '0',
    archived       CHAR(1)               DEFAULT '0',
    pushed_at      TIMESTAMP,
    sync_time      TIMESTAMP,
    del_flag       CHAR(1)               DEFAULT '0',
    create_dept    BIGINT,
    create_by      BIGINT,
    create_time    TIMESTAMP,
    update_by      BIGINT,
    update_time    TIMESTAMP,
    PRIMARY KEY (repo_id)
);
CREATE INDEX IF NOT EXISTS idx_iot_github_repo_owner ON iot_github_repo (owner_login);
CREATE INDEX IF NOT EXISTS idx_iot_github_repo_name ON iot_github_repo (full_name);
COMMENT ON TABLE iot_github_repo IS 'GitHub 项目表（从 GitHub 同步）';
COMMENT ON COLUMN iot_github_repo.repo_id        IS 'GitHub 仓库ID';
COMMENT ON COLUMN iot_github_repo.owner_login    IS '仓库所有者登录名';
COMMENT ON COLUMN iot_github_repo.repo_name      IS '仓库名称';
COMMENT ON COLUMN iot_github_repo.full_name      IS '仓库全名（owner/name）';
COMMENT ON COLUMN iot_github_repo.html_url       IS '仓库页面地址';
COMMENT ON COLUMN iot_github_repo.clone_url      IS '仓库克隆地址';
COMMENT ON COLUMN iot_github_repo.description    IS '仓库描述';
COMMENT ON COLUMN iot_github_repo.language       IS '主要开发语言';
COMMENT ON COLUMN iot_github_repo.default_branch IS '默认分支';
COMMENT ON COLUMN iot_github_repo.private_flag   IS '是否私有（0公开 1私有）';
COMMENT ON COLUMN iot_github_repo.archived       IS '是否已归档（0否 1是）';
COMMENT ON COLUMN iot_github_repo.pushed_at      IS '最近推送时间';
COMMENT ON COLUMN iot_github_repo.sync_time      IS '最近同步时间';
COMMENT ON COLUMN iot_github_repo.del_flag       IS '删除标志（0代表存在 1代表删除）';

CREATE TABLE IF NOT EXISTS iot_github_repo_user
(
    repo_id     BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    create_time TIMESTAMP,
    PRIMARY KEY (repo_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_iot_github_repo_user_user ON iot_github_repo_user (user_id);
COMMENT ON TABLE iot_github_repo_user IS 'GitHub 项目授权表（项目 -> 系统账号）';
COMMENT ON COLUMN iot_github_repo_user.repo_id     IS 'GitHub 仓库ID';
COMMENT ON COLUMN iot_github_repo_user.user_id     IS '系统用户ID';
COMMENT ON COLUMN iot_github_repo_user.create_time IS '授权时间';
