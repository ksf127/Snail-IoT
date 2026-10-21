package org.dromara.iot.pay.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.pay.domain.bo.IotPayProductBo;
import org.dromara.iot.pay.domain.vo.IotPayProductVo;

import java.util.Collection;
import java.util.List;

/**
 * 支付商品 服务层
 *
 * @author iot team
 */
public interface IotPayProductService {

    /**
     * 分页查询商品
     */
    PageResult<IotPayProductVo> queryPageList(IotPayProductBo bo, PageQuery pageQuery);

    /**
     * 查询上架商品列表（下单可选）
     */
    List<IotPayProductVo> listOnSale();

    /**
     * 查询商品详情
     */
    IotPayProductVo queryById(Long productId);

    /**
     * 新增商品
     */
    Boolean createProduct(IotPayProductBo bo);

    /**
     * 修改商品
     */
    Boolean updateProduct(IotPayProductBo bo);

    /**
     * 批量删除商品
     */
    Boolean deleteProduct(Collection<Long> productIds);

}
