package org.dromara.iot.job.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.job.domain.bo.IotJobInfoBo;
import org.dromara.iot.job.domain.vo.IotJobInfoVo;
import org.dromara.iot.job.domain.vo.IotJobLogVo;

import java.util.Collection;
import java.util.Map;

/**
 * 任务信息 服务层
 *
 * @author iot team
 */
public interface IotJobInfoService {

    /**
     * 分页查询任务
     */
    PageResult<IotJobInfoVo> queryPageList(IotJobInfoBo bo, PageQuery pageQuery);

    /**
     * 查询任务详情
     */
    IotJobInfoVo queryById(Long jobId);

    /**
     * 新增任务
     */
    Boolean insertByBo(IotJobInfoBo bo);

    /**
     * 修改任务
     */
    Boolean updateByBo(IotJobInfoBo bo);

    /**
     * 删除任务
     */
    Boolean deleteByIds(Collection<Long> jobIds);

    /**
     * 启停任务
     *
     * @param jobId  任务ID
     * @param status 状态（0启用 1停用）
     */
    Boolean changeStatus(Long jobId, String status);

    /**
     * 手动触发一次任务
     */
    IotJobLogVo triggerOnce(Long jobId);

    /**
     * 查询可用执行器（名称 -> 描述），供前端下拉选择
     */
    Map<String, String> handlerNames();

}
