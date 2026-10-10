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
import org.dromara.iot.permission.domain.IotAppDataset;
import org.dromara.iot.permission.domain.IotDataset;
import org.dromara.iot.permission.domain.IotDatasetRole;
import org.dromara.iot.permission.domain.IotGroup;
import org.dromara.iot.permission.domain.bo.IotDatasetBo;
import org.dromara.iot.permission.domain.bo.IotDatasetRoleBo;
import org.dromara.iot.permission.domain.vo.IotDatasetRoleVo;
import org.dromara.iot.permission.domain.vo.IotDatasetVo;
import org.dromara.iot.permission.domain.vo.IotGroupVo;
import org.dromara.iot.permission.mapper.IotAppDatasetMapper;
import org.dromara.iot.permission.mapper.IotDatasetMapper;
import org.dromara.iot.permission.mapper.IotDatasetRoleMapper;
import org.dromara.iot.permission.mapper.IotGroupMapper;
import org.dromara.iot.permission.service.IIotDatasetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据集 服务层实现
 *
 * @author iot team
 */
@RequiredArgsConstructor
@Service
public class IotDatasetServiceImpl implements IIotDatasetService {

    private final IotDatasetMapper baseMapper;

    private final IotGroupMapper groupMapper;

    private final IotDatasetRoleMapper datasetRoleMapper;

    private final IotAppDatasetMapper appDatasetMapper;

    /**
     * 查询数据集详情
     *
     * @param datasetId 数据集ID
     * @return 数据集视图对象
     */
    @Override
    public IotDatasetVo queryById(Long datasetId) {
        IotDatasetVo vo = baseMapper.selectVoById(datasetId);
        if (vo != null && vo.getGroupId() != null) {
            IotGroupVo group = groupMapper.selectVoById(vo.getGroupId());
            if (group != null) {
                vo.setGroupName(group.getGroupName());
            }
        }
        return vo;
    }

    /**
     * 分页查询数据集列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<IotDatasetVo> queryPageList(IotDatasetBo bo, PageQuery pageQuery) {
        Page<IotDatasetVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        fillGroupName(page.getRecords());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询数据集列表（不分页）
     *
     * @param bo 查询条件
     * @return 数据集列表
     */
    @Override
    public List<IotDatasetVo> queryList(IotDatasetBo bo) {
        List<IotDatasetVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        fillGroupName(list);
        return list;
    }

    /**
     * 新增数据集
     *
     * @param bo 数据集业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(IotDatasetBo bo) {
        checkCodeUnique(bo.getDatasetId(), bo.getDatasetCode());
        IotDataset add = MapstructUtils.convert(bo, IotDataset.class);
        if (add != null && add.getRowCount() == null) {
            add.setRowCount(0L);
        }
        return baseMapper.insert(add) > 0;
    }

    /**
     * 修改数据集
     *
     * @param bo 数据集业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(IotDatasetBo bo) {
        checkCodeUnique(bo.getDatasetId(), bo.getDatasetCode());
        IotDataset update = MapstructUtils.convert(bo, IotDataset.class);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 批量删除数据集（被应用引用时不允许删除）
     *
     * @param ids     数据集ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (Boolean.TRUE.equals(isValid) && CollUtil.isNotEmpty(ids)) {
            Long refCount = appDatasetMapper.selectCount(Wrappers.<IotAppDataset>lambdaQuery()
                .in(IotAppDataset::getDatasetId, ids));
            if (refCount != null && refCount > 0) {
                throw new ServiceException("数据集已被应用引用，不允许删除");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 查询数据集已配置的角色数据权限
     *
     * @param datasetId 数据集ID
     * @return 角色数据权限列表
     */
    @Override
    public List<IotDatasetRoleVo> queryRoleScopes(Long datasetId) {
        return datasetRoleMapper.selectRoleScopesByDatasetId(datasetId);
    }

    /**
     * 保存数据集的角色数据权限（全量覆盖）
     *
     * @param datasetId 数据集ID
     * @param scopes    角色数据权限列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoleScopes(Long datasetId, List<IotDatasetRoleBo> scopes) {
        datasetRoleMapper.delete(Wrappers.<IotDatasetRole>lambdaQuery()
            .eq(IotDatasetRole::getDatasetId, datasetId));
        if (CollUtil.isEmpty(scopes)) {
            return Boolean.TRUE;
        }
        for (IotDatasetRoleBo scope : scopes) {
            if (scope.getRoleId() == null) {
                continue;
            }
            IotDatasetRole entity = new IotDatasetRole();
            entity.setDatasetId(datasetId);
            entity.setRoleId(scope.getRoleId());
            entity.setDataScope(StrUtil.blankToDefault(scope.getDataScope(), "1"));
            datasetRoleMapper.insert(entity);
        }
        return Boolean.TRUE;
    }

    /**
     * 校验数据集编码唯一
     *
     * @param datasetId   数据集ID（修改时排除自身）
     * @param datasetCode 数据集编码
     */
    private void checkCodeUnique(Long datasetId, String datasetCode) {
        boolean exists = baseMapper.exists(Wrappers.<IotDataset>lambdaQuery()
            .eq(IotDataset::getDatasetCode, datasetCode)
            .ne(datasetId != null, IotDataset::getDatasetId, datasetId));
        if (exists) {
            throw new ServiceException("数据集编码已存在：" + datasetCode);
        }
    }

    /**
     * 填充所属分组名称
     *
     * @param list 数据集列表
     */
    private void fillGroupName(List<IotDatasetVo> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Long> groupIds = list.stream()
            .map(IotDatasetVo::getGroupId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());
        if (CollUtil.isEmpty(groupIds)) {
            return;
        }
        Map<Long, String> nameMap = groupMapper.selectVoList(Wrappers.<IotGroup>lambdaQuery()
                .in(IotGroup::getGroupId, groupIds))
            .stream()
            .collect(Collectors.toMap(IotGroupVo::getGroupId, IotGroupVo::getGroupName, (a, b) -> a));
        list.forEach(item -> item.setGroupName(nameMap.get(item.getGroupId())));
    }

    /**
     * 构建数据集查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotDataset> buildQueryWrapper(IotDatasetBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IotDataset> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getGroupId() != null, IotDataset::getGroupId, bo.getGroupId());
        lqw.like(StrUtil.isNotBlank(bo.getDatasetCode()), IotDataset::getDatasetCode, bo.getDatasetCode());
        lqw.like(StrUtil.isNotBlank(bo.getDatasetName()), IotDataset::getDatasetName, bo.getDatasetName());
        lqw.eq(StrUtil.isNotBlank(bo.getDatasetType()), IotDataset::getDatasetType, bo.getDatasetType());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotDataset::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            IotDataset::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(IotDataset::getCreateTime);
        return lqw;
    }

}
