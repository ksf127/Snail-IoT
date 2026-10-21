package org.dromara.iot.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.job.domain.IotJobInfo;
import org.dromara.iot.job.domain.IotJobLog;
import org.dromara.iot.job.domain.bo.IotJobInfoBo;
import org.dromara.iot.job.domain.vo.IotJobInfoVo;
import org.dromara.iot.job.domain.vo.IotJobLogVo;
import org.dromara.iot.job.enums.JobTriggerTypeEnum;
import org.dromara.iot.job.enums.JobTypeEnum;
import org.dromara.iot.job.executor.IotJobInvoker;
import org.dromara.iot.job.executor.JobHandlerRegistry;
import org.dromara.iot.job.mapper.IotJobInfoMapper;
import org.dromara.iot.job.mapper.IotJobLogMapper;
import org.dromara.iot.job.scheduler.IotJobScheduler;
import org.dromara.iot.job.service.IotJobInfoService;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

/**
 * 任务信息 服务层实现
 *
 * @author iot team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotJobInfoServiceImpl implements IotJobInfoService {

    private final IotJobInfoMapper jobInfoMapper;
    private final IotJobLogMapper jobLogMapper;
    private final JobHandlerRegistry handlerRegistry;
    private final IotJobInvoker jobInvoker;
    private final IotJobScheduler jobScheduler;

    /**
     * 状态：启用
     */
    private static final String STATUS_ENABLED = "0";

    @Override
    public PageResult<IotJobInfoVo> queryPageList(IotJobInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotJobInfo> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getJobName()), IotJobInfo::getJobName, bo.getJobName());
        lqw.eq(StringUtils.isNotBlank(bo.getJobGroup()), IotJobInfo::getJobGroup, bo.getJobGroup());
        lqw.eq(StringUtils.isNotBlank(bo.getJobType()), IotJobInfo::getJobType, bo.getJobType());
        lqw.eq(StringUtils.isNotBlank(bo.getTriggerType()), IotJobInfo::getTriggerType, bo.getTriggerType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotJobInfo::getStatus, bo.getStatus());
        lqw.orderByDesc(IotJobInfo::getCreateTime);
        Page<IotJobInfoVo> page = jobInfoMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public IotJobInfoVo queryById(Long jobId) {
        return jobInfoMapper.selectVoById(jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(IotJobInfoBo bo) {
        validate(bo);
        checkJobNameUnique(bo.getJobName(), null);
        IotJobInfo add = MapstructUtils.convert(bo, IotJobInfo.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(STATUS_ENABLED);
        }
        boolean flag = jobInfoMapper.insert(add) > 0;
        if (flag) {
            refreshSchedule(add);
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(IotJobInfoBo bo) {
        requireJob(bo.getJobId());
        validate(bo);
        checkJobNameUnique(bo.getJobName(), bo.getJobId());
        IotJobInfo update = MapstructUtils.convert(bo, IotJobInfo.class);
        boolean flag = jobInfoMapper.updateById(update) > 0;
        if (flag) {
            refreshSchedule(update);
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(Collection<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            return false;
        }
        jobIds.forEach(jobScheduler::cancel);
        jobLogMapper.delete(Wrappers.<IotJobLog>lambdaQuery().in(IotJobLog::getJobId, jobIds));
        return jobInfoMapper.deleteByIds(jobIds) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeStatus(Long jobId, String status) {
        requireJob(jobId);
        IotJobInfo update = new IotJobInfo();
        update.setJobId(jobId);
        update.setStatus(status);
        boolean flag = jobInfoMapper.updateById(update) > 0;
        if (flag) {
            refreshSchedule(update);
        }
        return flag;
    }

    @Override
    public IotJobLogVo triggerOnce(Long jobId) {
        IotJobInfo info = requireJob(jobId);
        return MapstructUtils.convert(jobInvoker.invoke(info, JobTriggerTypeEnum.MANUAL.getCode()), IotJobLogVo.class);
    }

    @Override
    public Map<String, String> handlerNames() {
        return handlerRegistry.describeAll();
    }

    /**
     * 校验任务类型、触发方式与执行器
     */
    private void validate(IotJobInfoBo bo) {
        JobTypeEnum jobType = JobTypeEnum.fromCode(bo.getJobType());
        if (jobType == null) {
            throw new ServiceException("不支持的任务类型：" + bo.getJobType());
        }
        JobTriggerTypeEnum triggerType = JobTriggerTypeEnum.fromCode(bo.getTriggerType());
        if (triggerType == null) {
            throw new ServiceException("不支持的触发方式：" + bo.getTriggerType());
        }
        if (JobTypeEnum.LOCAL == jobType) {
            if (handlerRegistry.get(bo.getHandlerName()) == null) {
                throw new ServiceException("执行器不存在：" + bo.getHandlerName());
            }
            if (JobTriggerTypeEnum.CRON == triggerType) {
                if (StringUtils.isBlank(bo.getCronExpr())) {
                    throw new ServiceException("Cron 表达式不能为空");
                }
                if (!CronExpression.isValidExpression(bo.getCronExpr())) {
                    throw new ServiceException("Cron 表达式不合法：" + bo.getCronExpr());
                }
            }
            if (JobTriggerTypeEnum.FIXED_RATE == triggerType
                && (bo.getIntervalSeconds() == null || bo.getIntervalSeconds() <= 0)) {
                throw new ServiceException("固定频率间隔必须大于 0 秒");
            }
        }
    }

    /**
     * 刷新调度：满足条件则注册，否则取消
     */
    private void refreshSchedule(IotJobInfo info) {
        if (info.getJobId() == null) {
            return;
        }
        // 局部更新场景需补全类型与触发方式，回查一次保证调度参数完整
        IotJobInfo full = jobInfoMapper.selectById(info.getJobId());
        if (full == null) {
            return;
        }
        boolean schedulable = STATUS_ENABLED.equals(full.getStatus())
            && JobTypeEnum.LOCAL.getCode().equals(full.getJobType())
            && (JobTriggerTypeEnum.CRON.getCode().equals(full.getTriggerType())
            || JobTriggerTypeEnum.FIXED_RATE.getCode().equals(full.getTriggerType()));
        if (schedulable) {
            jobScheduler.schedule(full);
        } else {
            jobScheduler.cancel(full.getJobId());
        }
    }

    /**
     * 校验任务名称唯一
     */
    private void checkJobNameUnique(String jobName, Long jobId) {
        if (StringUtils.isBlank(jobName)) {
            return;
        }
        boolean exists = jobInfoMapper.exists(Wrappers.<IotJobInfo>lambdaQuery()
            .eq(IotJobInfo::getJobName, jobName)
            .ne(jobId != null, IotJobInfo::getJobId, jobId));
        if (exists) {
            throw new ServiceException("任务名称已存在：" + jobName);
        }
    }

    /**
     * 校验任务存在并返回实体
     */
    private IotJobInfo requireJob(Long jobId) {
        IotJobInfo info = jobId == null ? null : jobInfoMapper.selectById(jobId);
        if (info == null) {
            throw new ServiceException("任务不存在：" + jobId);
        }
        return info;
    }

}
