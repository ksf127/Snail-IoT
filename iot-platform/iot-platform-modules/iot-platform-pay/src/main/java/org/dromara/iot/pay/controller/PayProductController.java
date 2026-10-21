package org.dromara.iot.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.pay.domain.bo.IotPayProductBo;
import org.dromara.iot.pay.domain.vo.IotPayProductVo;
import org.dromara.iot.pay.service.IotPayProductService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支付商品（虚拟商品货架）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/pay/product")
public class PayProductController extends BaseController {

    private final IotPayProductService productService;

    /**
     * 分页查询商品
     */
    @SaCheckPermission("iot:pay:product")
    @GetMapping("/list")
    public R<PageResult<IotPayProductVo>> list(IotPayProductBo bo, PageQuery pageQuery) {
        return R.ok(productService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询上架商品（下单可选）
     */
    @SaCheckPermission("iot:pay:create")
    @GetMapping("/on-sale")
    public R<List<IotPayProductVo>> onSale() {
        return R.ok(productService.listOnSale());
    }

    /**
     * 查询商品详情
     */
    @SaCheckPermission("iot:pay:product")
    @GetMapping("/{productId}")
    public R<IotPayProductVo> getInfo(@PathVariable Long productId) {
        return R.ok(productService.queryById(productId));
    }

    /**
     * 新增商品
     */
    @SaCheckPermission("iot:pay:product")
    @Log(title = "支付商品", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotPayProductBo bo) {
        return toAjax(productService.createProduct(bo));
    }

    /**
     * 修改商品
     */
    @SaCheckPermission("iot:pay:product")
    @Log(title = "支付商品", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotPayProductBo bo) {
        return toAjax(productService.updateProduct(bo));
    }

    /**
     * 删除商品
     */
    @SaCheckPermission("iot:pay:product")
    @Log(title = "支付商品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public R<Void> remove(@PathVariable Long[] productIds) {
        return toAjax(productService.deleteProduct(List.of(productIds)));
    }

}
