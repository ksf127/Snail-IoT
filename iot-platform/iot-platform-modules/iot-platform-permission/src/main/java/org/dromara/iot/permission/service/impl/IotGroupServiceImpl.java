package org.dromara.iot.permission.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.iot.permission.domain.IotDataset;
import org.dromara.iot.permission.domain.IotGroup;
import org.dromara.iot.permission.domain.bo.IotGroupBo;
import org.dromara.iot.permission.domain.vo.IotGroupVo;
import org.dromara.iot.permission.mapper.IotDatasetMapper;
import org.dromara.iot.permission.mapper.IotGroupMapper;
import org.dromara.iot.permission.service.IIotGroupService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据分组 服务层实现
 *
 * @author iot team
 */
@RequiredArgsConstructor
@Service
public class IotGroupServiceImpl implements IIotGroupService {

    private final IotGroupMapper baseMapper;

    private final IotDatasetMapper datasetMapper;

    /**
     * 查询分组详情
     *
     * @param groupId 分组ID
     * @return 分组视图对象
     */
    @Override
    public IotGroupVo queryById(Long groupId) {
        return baseMapper.selectVoById(groupId);
    }

    /**
     * 查询分组列表（含父分组名称）
     *
     * @param bo 查询条件
     * @return 分组列表
     */
    @Override
    public List<IotGroupVo> queryList(IotGroupBo bo) {
        List<IotGroupVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        fillParentName(list);
        return list;
    }

    /**
     * 新增分组
     *
     * @param bo 分组业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(IotGroupBo bo) {
        checkCodeUnique(bo.getGroupId(), bo.getGroupCode());
        IotGroup add = MapstructUtils.convert(bo, IotGroup.class);
        if (add != null && add.getOrderNum() == null) {
            add.setOrderNum(0);
        }
        return baseMapper.insert(add) > 0;
    }

    /**
     * 修改分组
     *
     * @param bo 分组业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(IotGroupBo bo) {
        checkCodeUnique(bo.getGroupId(), bo.getGroupCode());
        if (bo.getGroupId() != null && bo.getGroupId().equals(bo.getParentId())) {
            throw new ServiceException("父分组不能是自己");
        }
        IotGroup update = MapstructUtils.convert(bo, IotGroup.class);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 批量删除分组（存在子分组或数据集时不允许删除）
     *
     * @param ids     分组ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (Boolean.TRUE.equals(isValid) && CollUtil.isNotEmpty(ids)) {
            Long childCount = baseMapper.selectCount(Wrappers.<IotGroup>lambdaQuery()
                .in(IotGroup::getParentId, ids));
            if (childCount != null && childCount > 0) {
                throw new ServiceException("存在子分组，不允许删除");
            }
            Long datasetCount = datasetMapper.selectCount(Wrappers.<IotDataset>lambdaQuery()
                .in(IotDataset::getGroupId, ids));
            if (datasetCount != null && datasetCount > 0) {
                throw new ServiceException("分组下存在数据集，不允许删除");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 校验分组编码唯一
     *
     * @param groupId   分组ID（修改时排除自身）
     * @param groupCode 分组编码
     */
    private void checkCodeUnique(Long groupId, String groupCode) {
        boolean exists = baseMapper.exists(Wrappers.<IotGroup>lambdaQuery()
            .eq(IotGroup::getGroupCode, groupCode)
            .ne(groupId != null, IotGroup::getGroupId, groupId));
        if (exists) {
            throw new ServiceException("分组编码已存在：" + groupCode);
        }
    }

    /**
     * 填充父分组名称
     *
     * @param list 分组列表
     */
    private void fillParentName(List<IotGroupVo> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Long> parentIds = list.stream()
            .map(IotGroupVo::getParentId)
            .filter(id -> id != null && id != 0L)
            .collect(Collectors.toSet());
        if (CollUtil.isEmpty(parentIds)) {
            return;
        }
        Map<Long, String> nameMap = baseMapper.selectVoList(Wrappers.<IotGroup>lambdaQuery()
                .in(IotGroup::getGroupId, parentIds))
            .stream()
            .collect(Collectors.toMap(IotGroupVo::getGroupId, IotGroupVo::getGroupName, (a, b) -> a));
        list.forEach(item -> item.setParentName(nameMap.get(item.getParentId())));
    }

    /**
     * 构建分组查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotGroup> buildQueryWrapper(IotGroupBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IotGroup> lqw = Wrappers.lambdaQuery();
        lqw.like(StrUtil.isNotBlank(bo.getGroupCode()), IotGroup::getGroupCode, bo.getGroupCode());
        lqw.like(StrUtil.isNotBlank(bo.getGroupName()), IotGroup::getGroupName, bo.getGroupName());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotGroup::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            IotGroup::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(IotGroup::getParentId).orderByAsc(IotGroup::getOrderNum);
        return lqw;
    }

}
