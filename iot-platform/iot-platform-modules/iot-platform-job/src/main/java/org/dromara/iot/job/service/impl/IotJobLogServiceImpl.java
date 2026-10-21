package org.dromara.iot.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.job.domain.IotJobLog;
import org.dromara.iot.job.domain.bo.IotJobLogBo;
import org.dromara.iot.job.domain.vo.IotJobLogVo;
import org.dromara.iot.job.mapper.IotJobLogMapper;
import org.dromara.iot.job.service.IotJobLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * 任务执行日志 服务层实现
 *
 * @author iot team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotJobLogServiceImpl implements IotJobLogService {

    private final IotJobLogMapper jobLogMapper;

    @Override
    public PageResult<IotJobLogVo> queryPageList(IotJobLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotJobLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getJobId() != null, IotJobLog::getJobId, bo.getJobId());
        lqw.like(StringUtils.isNotBlank(bo.getJobName()), IotJobLog::getJobName, bo.getJobName());
        lqw.eq(StringUtils.isNotBlank(bo.getTriggerType()), IotJobLog::getTriggerType, bo.getTriggerType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotJobLog::getStatus, bo.getStatus());
        lqw.orderByDesc(IotJobLog::getStartTime);
        Page<IotJobLogVo> page = jobLogMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(Collection<Long> logIds) {
        if (logIds == null || logIds.isEmpty()) {
            return false;
        }
        return jobLogMapper.deleteByIds(logIds) > 0;
    }

}
