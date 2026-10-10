package org.dromara.iot.device.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.device.domain.IotProduct;
import org.dromara.iot.device.domain.bo.IotProductBo;
import org.dromara.iot.device.domain.vo.IotProductVo;
import org.dromara.iot.device.mapper.IotProductMapper;
import org.dromara.iot.device.service.IIotProductService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 产品 服务层实现
 *
 * @author iot team
 */
@RequiredArgsConstructor
@Service
public class IotProductServiceImpl implements IIotProductService {

    private final IotProductMapper baseMapper;

    /**
     * 查询产品详情
     *
     * @param productId 产品ID
     * @return 产品视图对象
     */
    @Override
    public IotProductVo queryById(Long productId) {
        return baseMapper.selectVoById(productId);
    }

    /**
     * 分页查询产品列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<IotProductVo> queryPageList(IotProductBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotProduct> lqw = buildQueryWrapper(bo);
        Page<IotProductVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询产品列表（不分页）
     *
     * @param bo 查询条件
     * @return 产品列表
     */
    @Override
    public List<IotProductVo> queryList(IotProductBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    /**
     * 新增产品
     *
     * @param bo 产品业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(IotProductBo bo) {
        IotProduct add = MapstructUtils.convert(bo, IotProduct.class);
        return baseMapper.insert(add) > 0;
    }

    /**
     * 修改产品
     *
     * @param bo 产品业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(IotProductBo bo) {
        IotProduct update = MapstructUtils.convert(bo, IotProduct.class);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 批量删除产品
     *
     * @param ids     产品ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // TODO 业务校验：存在关联设备的产品不允许删除
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 构建产品查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotProduct> buildQueryWrapper(IotProductBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IotProduct> lqw = Wrappers.lambdaQuery();
        lqw.like(StrUtil.isNotBlank(bo.getProductCode()), IotProduct::getProductCode, bo.getProductCode());
        lqw.like(StrUtil.isNotBlank(bo.getProductName()), IotProduct::getProductName, bo.getProductName());
        lqw.eq(StrUtil.isNotBlank(bo.getProtocol()), IotProduct::getProtocol, bo.getProtocol());
        lqw.eq(StrUtil.isNotBlank(bo.getDeviceType()), IotProduct::getDeviceType, bo.getDeviceType());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotProduct::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            IotProduct::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(IotProduct::getCreateTime);
        return lqw;
    }

}
