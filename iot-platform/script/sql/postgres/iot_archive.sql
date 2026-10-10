-- ============================================================================
-- IoT 物联网平台 - 档案表（阶段三）与对象存储（MinIO）配置
--
-- 使用前提：
--   1. 已导入 iot 基础脚本（含 sys_oss / sys_oss_config 表）
--   2. 已导入 iot_platform.sql（含 iot:archive:list / iot:archive:add 菜单）
--
-- 执行方式：
--   docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_archive.sql
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 档案表：设备档案 / 运维记录 / 归档附件（附件通过 oss_id 关联 sys_oss）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_archive;
CREATE TABLE iot_archive
(
    archive_id   BIGINT       NOT NULL,
    archive_type VARCHAR(64)  NOT NULL,
    biz_id       BIGINT,
    oss_id       BIGINT,
    archive_time TIMESTAMP,
    status       CHAR(1)               DEFAULT '0',
    del_flag     CHAR(1)               DEFAULT '0',
    remark       VARCHAR(500)          DEFAULT '',
    create_dept  BIGINT,
    create_by    BIGINT,
    create_time  TIMESTAMP,
    update_by    BIGINT,
    update_time  TIMESTAMP,
    PRIMARY KEY (archive_id)
);
COMMENT ON TABLE iot_archive IS '档案表：设备档案/运维记录/归档附件';
COMMENT ON COLUMN iot_archive.archive_id   IS '档案ID';
COMMENT ON COLUMN iot_archive.archive_type IS '档案类型';
COMMENT ON COLUMN iot_archive.biz_id       IS '业务主键（如设备ID）';
COMMENT ON COLUMN iot_archive.oss_id       IS '对象存储文件ID（sys_oss.oss_id）';
COMMENT ON COLUMN iot_archive.archive_time IS '归档时间';
COMMENT ON COLUMN iot_archive.status       IS '状态（0正常 1停用）';
COMMENT ON COLUMN iot_archive.del_flag     IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_archive.remark       IS '备注';
COMMENT ON COLUMN iot_archive.create_dept  IS '创建部门';
COMMENT ON COLUMN iot_archive.create_by    IS '创建者';
COMMENT ON COLUMN iot_archive.create_time  IS '创建时间';
COMMENT ON COLUMN iot_archive.update_by    IS '更新者';
COMMENT ON COLUMN iot_archive.update_time  IS '更新时间';

-- ----------------------------------------------------------------------------
-- 2. 将默认对象存储配置指向 MinIO 容器
--    endpoint   : 后端容器内访问 S3 API（走容器网络 iot-minio:9000）
--    domain_url : 生成给浏览器访问的地址（宿主机端口 9000 已发布）
-- ----------------------------------------------------------------------------
UPDATE sys_oss_config
SET endpoint      = 'iot-minio:9000',
    domain_url    = 'http://localhost:9000',
    bucket_name   = 'ruoyi',
    access_key    = 'ruoyi',
    secret_key    = 'ruoyi123',
    is_https      = 'N',
    access_policy = '1',
    status        = 'Y'
WHERE config_key = 'minio';
