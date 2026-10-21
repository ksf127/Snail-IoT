package org.dromara.iot.cloud.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import org.dromara.iot.cloud.domain.IotCloudAccount;
import org.dromara.iot.cloud.domain.IotCloudResource;
import org.dromara.iot.cloud.domain.IotFlinkJob;
import org.dromara.iot.cloud.domain.bo.IotCloudAccountBo;
import org.dromara.iot.cloud.domain.vo.IotCloudAccountVo;
import org.dromara.iot.cloud.enums.CloudProviderEnum;
import org.dromara.iot.cloud.mapper.IotCloudAccountMapper;
import org.dromara.iot.cloud.mapper.IotCloudResourceMapper;
import org.dromara.iot.cloud.mapper.IotFlinkJobMapper;
import org.dromara.iot.cloud.provider.CloudProviderFactory;
import org.dromara.iot.cloud.provider.model.CloudConnectionResult;
import org.dromara.iot.cloud.provider.model.CloudRegionDTO;
import org.dromara.iot.cloud.service.IotCloudAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 云账号 服务层实现
 *
 * @author iot team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotCloudAccountServiceImpl implements IotCloudAccountService {

    private final IotCloudAccountMapper accountMapper;
    private final IotCloudResourceMapper resourceMapper;
    private final IotFlinkJobMapper flinkJobMapper;
    private final CloudProviderFactory providerFactory;

    /**
     * 云账号默认状态：启用
     */
    private static final String STATUS_ENABLED = "0";

    @Override
    public PageResult<IotCloudAccountVo> queryPageList(IotCloudAccountBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotCloudAccount> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getAccountName()), IotCloudAccount::getAccountName, bo.getAccountName());
        lqw.eq(StringUtils.isNotBlank(bo.getProvider()), IotCloudAccount::getProvider, bo.getProvider());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotCloudAccount::getStatus, bo.getStatus());
        lqw.orderByDesc(IotCloudAccount::getCreateTime);
        Page<IotCloudAccountVo> page = accountMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public IotCloudAccountVo queryById(Long accountId) {
        return accountMapper.selectVoById(accountId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(IotCloudAccountBo bo) {
        checkProvider(bo.getProvider());
        checkAccountNameUnique(bo.getAccountName(), null);
        IotCloudAccount add = MapstructUtils.convert(bo, IotCloudAccount.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(STATUS_ENABLED);
        }
        return accountMapper.insert(add) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(IotCloudAccountBo bo) {
        requireAccount(bo.getAccountId());
        checkProvider(bo.getProvider());
        checkAccountNameUnique(bo.getAccountName(), bo.getAccountId());
        IotCloudAccount update = MapstructUtils.convert(bo, IotCloudAccount.class);
        // 前端不回显密钥，未填写时保持原值
        if (StringUtils.isBlank(bo.getAccessSecret())) {
            update.setAccessSecret(null);
        }
        return accountMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(Collection<Long> accountIds) {
        if (CollUtil.isEmpty(accountIds)) {
            return false;
        }
        resourceMapper.delete(Wrappers.<IotCloudResource>lambdaQuery()
            .in(IotCloudResource::getAccountId, accountIds));
        flinkJobMapper.delete(Wrappers.<IotFlinkJob>lambdaQuery()
            .in(IotFlinkJob::getAccountId, accountIds));
        return accountMapper.deleteByIds(accountIds) > 0;
    }

    @Override
    public CloudConnectionResult testConnection(Long accountId) {
        IotCloudAccount account = requireAccount(accountId);
        return providerFactory.getProvider(account.getProvider()).testConnection(account);
    }

    @Override
    public List<CloudRegionDTO> listRegions(Long accountId) {
        IotCloudAccount account = requireAccount(accountId);
        return providerFactory.getProvider(account.getProvider()).listRegions(account);
    }

    /**
     * 校验云平台标识
     */
    private void checkProvider(String provider) {
        if (CloudProviderEnum.fromCode(provider) == null) {
            throw new ServiceException("不支持的云平台：" + provider);
        }
    }

    /**
     * 校验账号名称唯一
     */
    private void checkAccountNameUnique(String accountName, Long accountId) {
        if (StringUtils.isBlank(accountName)) {
            return;
        }
        boolean exists = accountMapper.exists(Wrappers.<IotCloudAccount>lambdaQuery()
            .eq(IotCloudAccount::getAccountName, accountName)
            .ne(accountId != null, IotCloudAccount::getAccountId, accountId));
        if (exists) {
            throw new ServiceException("云账号名称已存在：" + accountName);
        }
    }

    /**
     * 校验云账号存在并返回实体（含密钥，供 Provider 调用）
     */
    private IotCloudAccount requireAccount(Long accountId) {
        IotCloudAccount account = accountId == null ? null : accountMapper.selectById(accountId);
        if (account == null) {
            throw new ServiceException("云账号不存在：" + accountId);
        }
        return account;
    }

}
