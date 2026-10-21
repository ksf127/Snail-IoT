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
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.cloud.domain.IotCloudAccount;
import org.dromara.iot.cloud.domain.IotCloudResource;
import org.dromara.iot.cloud.domain.bo.IotCloudResourceBo;
import org.dromara.iot.cloud.domain.vo.IotCloudResourceVo;
import org.dromara.iot.cloud.mapper.IotCloudAccountMapper;
import org.dromara.iot.cloud.mapper.IotCloudResourceMapper;
import org.dromara.iot.cloud.provider.CloudProviderFactory;
import org.dromara.iot.cloud.provider.ICloudProvider;
import org.dromara.iot.cloud.provider.model.CloudResourceDTO;
import org.dromara.iot.cloud.service.IotCloudResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 云资源 服务层实现
 *
 * @author iot team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotCloudResourceServiceImpl implements IotCloudResourceService {

    private final IotCloudResourceMapper resourceMapper;
    private final IotCloudAccountMapper accountMapper;
    private final CloudProviderFactory providerFactory;

    @Override
    public PageResult<IotCloudResourceVo> queryPageList(IotCloudResourceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotCloudResource> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getAccountId() != null, IotCloudResource::getAccountId, bo.getAccountId());
        lqw.eq(StringUtils.isNotBlank(bo.getProvider()), IotCloudResource::getProvider, bo.getProvider());
        lqw.eq(StringUtils.isNotBlank(bo.getResourceType()), IotCloudResource::getResourceType, bo.getResourceType());
        lqw.eq(StringUtils.isNotBlank(bo.getRegionId()), IotCloudResource::getRegionId, bo.getRegionId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotCloudResource::getStatus, bo.getStatus());
        lqw.and(StringUtils.isNotBlank(bo.getKeyword()), w -> w
            .like(IotCloudResource::getInstanceId, bo.getKeyword())
            .or().like(IotCloudResource::getInstanceName, bo.getKeyword()));
        lqw.orderByDesc(IotCloudResource::getCreateTime);
        Page<IotCloudResourceVo> page = resourceMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public IotCloudResourceVo queryById(Long resourceId) {
        return resourceMapper.selectVoById(resourceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<IotCloudResourceVo> syncResources(Long accountId, IotCloudResourceBo query) {
        IotCloudAccount account = requireAccount(accountId);
        ICloudProvider provider = providerFactory.getProvider(account.getProvider());
        List<CloudResourceDTO> remoteList = provider.listResources(account, query);
        LocalDateTime now = LocalDateTime.now();
        int inserted = 0;
        int updated = 0;
        for (CloudResourceDTO dto : remoteList) {
            List<IotCloudResource> exists = resourceMapper.selectList(Wrappers.<IotCloudResource>lambdaQuery()
                .eq(IotCloudResource::getAccountId, accountId)
                .eq(IotCloudResource::getInstanceId, dto.getInstanceId()));
            IotCloudResource entity = CollUtil.isEmpty(exists) ? new IotCloudResource() : exists.get(0);
            entity.setAccountId(accountId);
            entity.setProvider(account.getProvider());
            entity.setResourceType(dto.getResourceType());
            entity.setInstanceId(dto.getInstanceId());
            entity.setInstanceName(dto.getInstanceName());
            entity.setRegionId(dto.getRegionId());
            entity.setStatus(dto.getStatus());
            entity.setSpec(dto.getSpec());
            entity.setPrivateIp(dto.getPrivateIp());
            entity.setPublicIp(dto.getPublicIp());
            entity.setBizMeta(JsonUtils.toJsonString(dto.getBizMeta()));
            entity.setLastSyncTime(now);
            if (CollUtil.isEmpty(exists)) {
                resourceMapper.insert(entity);
                inserted++;
            } else {
                resourceMapper.updateById(entity);
                updated++;
            }
        }
        log.info("同步云资源完成，accountId={}，provider={}，新增={}，更新={}",
            accountId, account.getProvider(), inserted, updated);
        return resourceMapper.selectVoList(Wrappers.<IotCloudResource>lambdaQuery()
            .eq(IotCloudResource::getAccountId, accountId)
            .orderByDesc(IotCloudResource::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createResource(IotCloudResourceBo bo) {
        if (bo.getAccountId() == null) {
            throw new ServiceException("云账号ID不能为空");
        }
        IotCloudAccount account = requireAccount(bo.getAccountId());
        ICloudProvider provider = providerFactory.getProvider(account.getProvider());
        CloudResourceDTO dto = provider.createResource(account, bo);
        IotCloudResource entity = new IotCloudResource();
        entity.setAccountId(account.getAccountId());
        entity.setProvider(account.getProvider());
        entity.setResourceType(dto.getResourceType());
        entity.setInstanceId(dto.getInstanceId());
        entity.setInstanceName(dto.getInstanceName());
        entity.setRegionId(dto.getRegionId());
        entity.setStatus(dto.getStatus());
        entity.setSpec(dto.getSpec());
        entity.setPrivateIp(dto.getPrivateIp());
        entity.setPublicIp(dto.getPublicIp());
        entity.setBizMeta(JsonUtils.toJsonString(dto.getBizMeta()));
        entity.setLastSyncTime(LocalDateTime.now());
        entity.setRemark(bo.getRemark());
        return resourceMapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean startResource(Long resourceId) {
        IotCloudResource resource = requireResource(resourceId);
        IotCloudAccount account = requireAccount(resource.getAccountId());
        providerFactory.getProvider(account.getProvider()).startResource(account, resource);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stopResource(Long resourceId) {
        IotCloudResource resource = requireResource(resourceId);
        IotCloudAccount account = requireAccount(resource.getAccountId());
        providerFactory.getProvider(account.getProvider()).stopResource(account, resource);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteResourceById(Long resourceId) {
        IotCloudResource resource = requireResource(resourceId);
        IotCloudAccount account = requireAccount(resource.getAccountId());
        providerFactory.getProvider(account.getProvider()).deleteResource(account, resource);
        return resourceMapper.deleteById(resourceId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(Collection<Long> resourceIds) {
        if (CollUtil.isEmpty(resourceIds)) {
            return false;
        }
        return resourceMapper.deleteByIds(resourceIds) > 0;
    }

    private IotCloudResource requireResource(Long resourceId) {
        IotCloudResource resource = resourceId == null ? null : resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new ServiceException("云资源不存在：" + resourceId);
        }
        return resource;
    }

    private IotCloudAccount requireAccount(Long accountId) {
        IotCloudAccount account = accountId == null ? null : accountMapper.selectById(accountId);
        if (account == null) {
            throw new ServiceException("云账号不存在：" + accountId);
        }
        return account;
    }

}
