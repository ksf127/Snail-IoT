package org.dromara.iot.permission.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.permission.domain.IotApp;
import org.dromara.iot.permission.domain.IotAppDataset;
import org.dromara.iot.permission.domain.IotAppRole;
import org.dromara.iot.permission.domain.bo.IotAppBo;
import org.dromara.iot.permission.domain.vo.IotAppVo;
import org.dromara.iot.permission.mapper.IotAppDatasetMapper;
import org.dromara.iot.permission.mapper.IotAppMapper;
import org.dromara.iot.permission.mapper.IotAppRoleMapper;
import org.dromara.iot.permission.service.IIotAppService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 应用 服务层实现
 *
 * @author iot team
 */
@RequiredArgsConstructor
@Service
public class IotAppServiceImpl implements IIotAppService {

    private final IotAppMapper baseMapper;

    private final IotAppDatasetMapper appDatasetMapper;

    private final IotAppRoleMapper appRoleMapper;

    /**
     * 查询应用详情
     *
     * @param appId 应用ID
     * @return 应用视图对象
     */
    @Override
    public IotAppVo queryById(Long appId) {
        IotAppVo vo = baseMapper.selectVoById(appId);
        fillCount(vo);
        return vo;
    }

    /**
     * 分页查询应用列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<IotAppVo> queryPageList(IotAppBo bo, PageQuery pageQuery) {
        Page<IotAppVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        page.getRecords().forEach(this::fillCount);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询应用列表（不分页）
     *
     * @param bo 查询条件
     * @return 应用列表
     */
    @Override
    public List<IotAppVo> queryList(IotAppBo bo) {
        List<IotAppVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        list.forEach(this::fillCount);
        return list;
    }

    /**
     * 新增应用
     *
     * @param bo 应用业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(IotAppBo bo) {
        checkCodeUnique(bo.getAppId(), bo.getAppCode());
        IotApp add = MapstructUtils.convert(bo, IotApp.class);
        return baseMapper.insert(add) > 0;
    }

    /**
     * 修改应用
     *
     * @param bo 应用业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(IotAppBo bo) {
        checkCodeUnique(bo.getAppId(), bo.getAppCode());
        IotApp update = MapstructUtils.convert(bo, IotApp.class);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 批量删除应用（同时清理数据集引用与角色授权关系）
     *
     * @param ids     应用ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (CollUtil.isEmpty(ids)) {
            return Boolean.FALSE;
        }
        appDatasetMapper.delete(Wrappers.<IotAppDataset>lambdaQuery().in(IotAppDataset::getAppId, ids));
        appRoleMapper.delete(Wrappers.<IotAppRole>lambdaQuery().in(IotAppRole::getAppId, ids));
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 查询应用引用的数据集ID列表
     *
     * @param appId 应用ID
     * @return 数据集ID列表
     */
    @Override
    public List<Long> queryDatasetIds(Long appId) {
        return appDatasetMapper.selectList(Wrappers.<IotAppDataset>lambdaQuery()
                .eq(IotAppDataset::getAppId, appId))
            .stream()
            .map(IotAppDataset::getDatasetId)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 保存应用引用的数据集（全量覆盖）
     *
     * @param appId      应用ID
     * @param datasetIds 数据集ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveDatasets(Long appId, List<Long> datasetIds) {
        appDatasetMapper.delete(Wrappers.<IotAppDataset>lambdaQuery().eq(IotAppDataset::getAppId, appId));
        if (CollUtil.isEmpty(datasetIds)) {
            return Boolean.TRUE;
        }
        datasetIds.stream().filter(Objects::nonNull).distinct().forEach(datasetId -> {
            IotAppDataset entity = new IotAppDataset();
            entity.setAppId(appId);
            entity.setDatasetId(datasetId);
            appDatasetMapper.insert(entity);
        });
        return Boolean.TRUE;
    }

    /**
     * 查询应用已授权的角色ID列表
     *
     * @param appId 应用ID
     * @return 角色ID列表
     */
    @Override
    public List<Long> queryRoleIds(Long appId) {
        return appRoleMapper.selectList(Wrappers.<IotAppRole>lambdaQuery()
                .eq(IotAppRole::getAppId, appId))
            .stream()
            .map(IotAppRole::getRoleId)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 保存应用授权的角色（全量覆盖）
     *
     * @param appId   应用ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoles(Long appId, List<Long> roleIds) {
        appRoleMapper.delete(Wrappers.<IotAppRole>lambdaQuery().eq(IotAppRole::getAppId, appId));
        if (CollUtil.isEmpty(roleIds)) {
            return Boolean.TRUE;
        }
        roleIds.stream().filter(Objects::nonNull).distinct().forEach(roleId -> {
            IotAppRole entity = new IotAppRole();
            entity.setAppId(appId);
            entity.setRoleId(roleId);
            appRoleMapper.insert(entity);
        });
        return Boolean.TRUE;
    }

    /**
     * 校验应用编码唯一
     *
     * @param appId   应用ID（修改时排除自身）
     * @param appCode 应用编码
     */
    private void checkCodeUnique(Long appId, String appCode) {
        boolean exists = baseMapper.exists(Wrappers.<IotApp>lambdaQuery()
            .eq(IotApp::getAppCode, appCode)
            .ne(appId != null, IotApp::getAppId, appId));
        if (exists) {
            throw new ServiceException("应用编码已存在：" + appCode);
        }
    }

    /**
     * 填充引用数据集数量与授权角色数量
     *
     * @param vo 应用视图对象
     */
    private void fillCount(IotAppVo vo) {
        if (vo == null || vo.getAppId() == null) {
            return;
        }
        vo.setDatasetCount(appDatasetMapper.selectCount(Wrappers.<IotAppDataset>lambdaQuery()
            .eq(IotAppDataset::getAppId, vo.getAppId())));
        vo.setRoleCount(appRoleMapper.selectCount(Wrappers.<IotAppRole>lambdaQuery()
            .eq(IotAppRole::getAppId, vo.getAppId())));
    }

    /**
     * 构建应用查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotApp> buildQueryWrapper(IotAppBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IotApp> lqw = Wrappers.lambdaQuery();
        lqw.like(StrUtil.isNotBlank(bo.getAppCode()), IotApp::getAppCode, bo.getAppCode());
        lqw.like(StrUtil.isNotBlank(bo.getAppName()), IotApp::getAppName, bo.getAppName());
        lqw.eq(StrUtil.isNotBlank(bo.getAppType()), IotApp::getAppType, bo.getAppType());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotApp::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            IotApp::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(IotApp::getCreateTime);
        return lqw;
    }

}
