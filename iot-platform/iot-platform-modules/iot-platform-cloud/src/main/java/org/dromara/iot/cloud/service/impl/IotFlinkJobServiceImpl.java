package org.dromara.iot.cloud.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.cloud.domain.IotCloudAccount;
import org.dromara.iot.cloud.domain.IotCloudResource;
import org.dromara.iot.cloud.domain.IotFlinkJob;
import org.dromara.iot.cloud.domain.bo.IotFlinkJobBo;
import org.dromara.iot.cloud.domain.bo.IotCloudResourceBo;
import org.dromara.iot.cloud.domain.vo.IotFlinkJobVo;
import org.dromara.iot.cloud.enums.CloudProviderEnum;
import org.dromara.iot.cloud.enums.CloudResourceTypeEnum;
import org.dromara.iot.cloud.mapper.IotCloudAccountMapper;
import org.dromara.iot.cloud.mapper.IotFlinkJobMapper;
import org.dromara.iot.cloud.provider.CloudProviderFactory;
import org.dromara.iot.cloud.provider.ICloudProvider;
import org.dromara.iot.cloud.provider.model.CloudResourceDTO;
import org.dromara.iot.cloud.service.IotFlinkJobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Flink 作业 服务层实现
 *
 * @author iot team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotFlinkJobServiceImpl implements IotFlinkJobService {

    private final IotFlinkJobMapper flinkJobMapper;
    private final IotCloudAccountMapper accountMapper;
    private final CloudProviderFactory providerFactory;

    @Override
    public PageResult<IotFlinkJobVo> queryPageList(IotFlinkJobBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotFlinkJob> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getAccountId() != null, IotFlinkJob::getAccountId, bo.getAccountId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotFlinkJob::getStatus, bo.getStatus());
        lqw.like(StringUtils.isNotBlank(bo.getJobName()), IotFlinkJob::getJobName, bo.getJobName());
        lqw.and(StringUtils.isNotBlank(bo.getKeyword()), w -> w
            .like(IotFlinkJob::getFlinkJobId, bo.getKeyword())
            .or().like(IotFlinkJob::getJobName, bo.getKeyword()));
        lqw.orderByDesc(IotFlinkJob::getCreateTime);
        Page<IotFlinkJobVo> page = flinkJobMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public IotFlinkJobVo queryById(Long jobId) {
        return flinkJobMapper.selectVoById(jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<IotFlinkJobVo> syncJobs(Long accountId) {
        IotCloudAccount account = requireAccount(accountId);
        if (!CloudProviderEnum.FLINK.getCode().equals(account.getProvider())) {
            throw new ServiceException("该云账号不是 Flink 平台，无法同步 Flink 作业：" + account.getProvider());
        }
        ICloudProvider provider = providerFactory.getProvider(account.getProvider());
        IotCloudResourceBo query = new IotCloudResourceBo();
        query.setAccountId(accountId);
        query.setResourceType(CloudResourceTypeEnum.FLINK_JOB.getCode());
        List<CloudResourceDTO> remoteList = provider.listResources(account, query);
        LocalDateTime now = LocalDateTime.now();
        int inserted = 0;
        int updated = 0;
        for (CloudResourceDTO dto : remoteList) {
            if (!CloudResourceTypeEnum.FLINK_JOB.getCode().equals(dto.getResourceType())) {
                continue;
            }
            List<IotFlinkJob> exists = flinkJobMapper.selectList(Wrappers.<IotFlinkJob>lambdaQuery()
                .eq(IotFlinkJob::getAccountId, accountId)
                .eq(IotFlinkJob::getFlinkJobId, dto.getInstanceId()));
            IotFlinkJob job = CollUtil.isEmpty(exists) ? new IotFlinkJob() : exists.get(0);
            Map<String, Object> meta = dto.getBizMeta();
            job.setAccountId(accountId);
            job.setFlinkJobId(dto.getInstanceId());
            job.setJobName(dto.getInstanceName());
            job.setStatus(dto.getStatus());
            job.setParallelism(parseInt(dto.getSpec()));
            job.setStartTime(metaOf(meta, "startTime"));
            job.setDuration(metaOf(meta, "duration"));
            job.setLastSyncTime(now);
            if (CollUtil.isEmpty(exists)) {
                flinkJobMapper.insert(job);
                inserted++;
            } else {
                flinkJobMapper.updateById(job);
                updated++;
            }
        }
        log.info("同步 Flink 作业完成，accountId={}，新增={}，更新={}", accountId, inserted, updated);
        return flinkJobMapper.selectVoList(Wrappers.<IotFlinkJob>lambdaQuery()
            .eq(IotFlinkJob::getAccountId, accountId)
            .orderByDesc(IotFlinkJob::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stopJob(Long jobId) {
        IotFlinkJob job = requireJob(jobId);
        IotCloudAccount account = requireAccount(job.getAccountId());
        providerFactory.getProvider(account.getProvider()).stopResource(account, toResource(job));
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelJob(Long jobId) {
        IotFlinkJob job = requireJob(jobId);
        IotCloudAccount account = requireAccount(job.getAccountId());
        providerFactory.getProvider(account.getProvider()).deleteResource(account, toResource(job));
        return Boolean.TRUE;
    }

    /**
     * 作业转换为一过性资源对象，供 Provider 调用
     */
    private IotCloudResource toResource(IotFlinkJob job) {
        IotCloudResource resource = new IotCloudResource();
        resource.setResourceId(job.getJobId());
        resource.setAccountId(job.getAccountId());
        resource.setResourceType(CloudResourceTypeEnum.FLINK_JOB.getCode());
        resource.setInstanceId(job.getFlinkJobId());
        resource.setInstanceName(job.getJobName());
        return resource;
    }

    private Integer parseInt(String value) {
        try {
            return StringUtils.isNotBlank(value) ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long metaOf(Map<String, Object> meta, String key) {
        if (meta == null || meta.get(key) == null) {
            return null;
        }
        Object value = meta.get(key);
        return value instanceof Number number ? number.longValue() : null;
    }

    private IotFlinkJob requireJob(Long jobId) {
        IotFlinkJob job = jobId == null ? null : flinkJobMapper.selectById(jobId);
        if (job == null) {
            throw new ServiceException("Flink 作业不存在：" + jobId);
        }
        return job;
    }

    private IotCloudAccount requireAccount(Long accountId) {
        IotCloudAccount account = accountId == null ? null : accountMapper.selectById(accountId);
        if (account == null) {
            throw new ServiceException("云账号不存在：" + accountId);
        }
        return account;
    }

}
