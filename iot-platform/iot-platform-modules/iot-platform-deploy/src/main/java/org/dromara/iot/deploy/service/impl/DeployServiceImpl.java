package org.dromara.iot.deploy.service.impl;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.deploy.service.IDeployService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 自动化部署 服务层实现
 * <p>
 * TODO 待实现：
 * - 新增 iot_deploy_task 表记录发布任务与状态机（待发布/发布中/成功/失败）；
 * - 制品存放走 iot-common-oss（阿里云 OSS）；
 * - 部署执行可通过 阿里云 ACK/ECS OpenAPI 或 SSH 编排，配合 Redis 分布式锁避免并发发布；
 * - 部署日志实时推送可复用 iot-common-push（SSE/WebSocket）。
 *
 * @author iot team
 */
@Service
public class DeployServiceImpl implements IDeployService {

    /**
     * 分页查询部署任务
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 部署任务分页结果
     */
    @Override
    public PageResult<Map<String, Object>> queryPageList(Map<String, Object> query, PageQuery pageQuery) {
        // TODO 接入 iot_deploy_task 表查询
        return PageResult.build(List.of(), 0L);
    }

    /**
     * 触发一次部署发布
     *
     * @param appCode   应用编码
     * @param version   版本号
     * @param targetEnv 目标环境（dev / test / prod）
     * @return 部署任务ID
     */
    @Override
    public Long release(String appCode, String version, String targetEnv) {
        // TODO 创建部署任务并异步执行
        return null;
    }

    /**
     * 查询部署日志
     *
     * @param taskId 部署任务ID
     * @return 部署日志文本
     */
    @Override
    public String queryLog(Long taskId) {
        // TODO 从部署任务记录/对象存储读取日志
        return "";
    }

}
