package org.dromara.iot.cloud.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.cloud.domain.bo.IotFlinkJobBo;
import org.dromara.iot.cloud.domain.vo.IotFlinkJobVo;

import java.util.List;

/**
 * Flink 作业 服务层
 *
 * @author iot team
 */
public interface IotFlinkJobService {

    /**
     * 分页查询 Flink 作业（本地库）
     */
    PageResult<IotFlinkJobVo> queryPageList(IotFlinkJobBo bo, PageQuery pageQuery);

    /**
     * 查询作业详情
     */
    IotFlinkJobVo queryById(Long jobId);

    /**
     * 从 Flink JobManager 同步作业清单（增量 upsert）
     *
     * @param accountId 云账号ID（须为 flink 平台）
     */
    List<IotFlinkJobVo> syncJobs(Long accountId);

    /**
     * 优雅停止作业（依赖 savepoint 目录配置）
     */
    Boolean stopJob(Long jobId);

    /**
     * 取消作业
     */
    Boolean cancelJob(Long jobId);

}
