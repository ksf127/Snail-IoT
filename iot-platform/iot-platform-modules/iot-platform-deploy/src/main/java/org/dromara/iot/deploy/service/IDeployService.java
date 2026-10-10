package org.dromara.iot.deploy.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Map;

/**
 * 自动化部署 服务层
 * <p>
 * 负责应用制品发布编排、部署进度与日志查询。
 *
 * @author iot team
 */
public interface IDeployService {

    /**
     * 分页查询部署任务
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 部署任务分页结果
     */
    PageResult<Map<String, Object>> queryPageList(Map<String, Object> query, PageQuery pageQuery);

    /**
     * 触发一次部署发布
     *
     * @param appCode  应用编码
     * @param version  版本号
     * @param targetEnv 目标环境（dev / test / prod）
     * @return 部署任务ID
     */
    Long release(String appCode, String version, String targetEnv);

    /**
     * 查询部署日志
     *
     * @param taskId 部署任务ID
     * @return 部署日志文本
     */
    String queryLog(Long taskId);

}
