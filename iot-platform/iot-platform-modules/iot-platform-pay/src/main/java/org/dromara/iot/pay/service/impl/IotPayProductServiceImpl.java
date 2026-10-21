package org.dromara.iot.pay.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.pay.domain.IotPayProduct;
import org.dromara.iot.pay.domain.bo.IotPayProductBo;
import org.dromara.iot.pay.domain.vo.IotPayProductVo;
import org.dromara.iot.pay.enums.PayProductTypeEnum;
import org.dromara.iot.pay.mapper.IotPayProductMapper;
import org.dromara.iot.pay.service.IotPayProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

/**
 * 支付商品 服务层实现
 *
 * @author iot team
 */
@Service
@RequiredArgsConstructor
public class IotPayProductServiceImpl implements IotPayProductService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final IotPayProductMapper productMapper;

    @Override
    public PageResult<IotPayProductVo> queryPageList(IotPayProductBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotPayProduct> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getProductNo()), IotPayProduct::getProductNo, bo.getProductNo());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), IotPayProduct::getProductName, bo.getProductName());
        lqw.eq(StringUtils.isNotBlank(bo.getProductType()), IotPayProduct::getProductType, bo.getProductType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotPayProduct::getStatus, bo.getStatus());
        lqw.orderByDesc(IotPayProduct::getCreateTime);
        Page<IotPayProductVo> page = productMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<IotPayProductVo> listOnSale() {
        return productMapper.selectVoList(Wrappers.<IotPayProduct>lambdaQuery()
            .eq(IotPayProduct::getStatus, "0")
            .orderByDesc(IotPayProduct::getCreateTime));
    }

    @Override
    public IotPayProductVo queryById(Long productId) {
        return productMapper.selectVoById(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createProduct(IotPayProductBo bo) {
        requireProductType(bo.getProductType());
        bo.setProductNo(StringUtils.isNotBlank(bo.getProductNo()) ? bo.getProductNo() : generateNo());
        checkProductNoUnique(bo.getProductNo(), null);
        IotPayProduct add = MapstructUtils.convert(bo, IotPayProduct.class);
        if (StringUtils.isBlank(add.getCurrency())) {
            add.setCurrency("CNY");
        }
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus("0");
        }
        return productMapper.insert(add) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProduct(IotPayProductBo bo) {
        IotPayProduct exist = productMapper.selectById(bo.getProductId());
        if (exist == null) {
            throw new ServiceException("商品不存在：" + bo.getProductId());
        }
        requireProductType(bo.getProductType());
        if (StringUtils.isNotBlank(bo.getProductNo())) {
            checkProductNoUnique(bo.getProductNo(), bo.getProductId());
        }
        IotPayProduct update = MapstructUtils.convert(bo, IotPayProduct.class);
        return productMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProduct(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return false;
        }
        return productMapper.deleteByIds(productIds) > 0;
    }

    /**
     * 校验商品类型合法性
     */
    private void requireProductType(String productType) {
        if (PayProductTypeEnum.fromCode(productType) == null) {
            throw new ServiceException("不支持的商品类型：" + productType);
        }
    }

    /**
     * 校验商品编号唯一
     */
    private void checkProductNoUnique(String productNo, Long productId) {
        boolean exists = productMapper.exists(Wrappers.<IotPayProduct>lambdaQuery()
            .eq(IotPayProduct::getProductNo, productNo)
            .ne(productId != null, IotPayProduct::getProductId, productId));
        if (exists) {
            throw new ServiceException("商品编号已存在：" + productNo);
        }
    }

    /**
     * 生成商品编号
     */
    private String generateNo() {
        return IotConstants.PAY_PRODUCT_NO_PREFIX + NO_FORMATTER.format(LocalDateTime.now())
            + RandomUtil.randomNumbers(4);
    }

}
