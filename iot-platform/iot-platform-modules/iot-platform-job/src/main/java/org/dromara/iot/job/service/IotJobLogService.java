package org.dromara.iot.job.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.job.domain.bo.IotJobLogBo;
import org.dromara.iot.job.domain.vo.IotJobLogVo;

import java.util.Collection;

/**
 * 任务执行日志 服务层
 *
 * @author iot team
 */
public interface IotJobLogService {

    /**
     * 分页查询执行日志
     */
    PageResult<IotJobLogVo> queryPageList(IotJobLogBo bo, PageQuery pageQuery);

    /**
     * 删除执行日志
     */
    Boolean deleteByIds(Collection<Long> logIds);

}
