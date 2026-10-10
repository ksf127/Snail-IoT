package org.dromara.iot.device.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.device.domain.bo.IotProductBo;
import org.dromara.iot.device.domain.vo.IotProductVo;
import org.dromara.iot.device.service.IIotProductService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品管理
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/product")
public class IotProductController extends BaseController {

    private final IIotProductService productService;

    /**
     * 分页查询产品列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 产品分页结果
     */
    @SaCheckPermission("iot:product:list")
    @GetMapping("/list")
    public R<PageResult<IotProductVo>> list(IotProductBo bo, PageQuery pageQuery) {
        return R.ok(productService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出产品列表
     *
     * @param bo       查询条件
     * @param response HTTP 响应
     */
    @Log(title = "产品管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("iot:product:export")
    @PostMapping("/export")
    public void export(IotProductBo bo, HttpServletResponse response) {
        List<IotProductVo> list = productService.queryList(bo);
        ExcelBuilder.of(list, IotProductVo.class).sheetName("产品数据").toResponse(response);
    }

    /**
     * 获取产品详细信息
     *
     * @param productId 产品ID
     * @return 产品详情
     */
    @SaCheckPermission("iot:product:query")
    @GetMapping("/{productId}")
    public R<IotProductVo> getInfo(@PathVariable Long productId) {
        return R.ok(productService.queryById(productId));
    }

    /**
     * 新增产品
     *
     * @param bo 产品业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:product:add")
    @Log(title = "产品管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotProductBo bo) {
        return toAjax(productService.insertByBo(bo));
    }

    /**
     * 修改产品
     *
     * @param bo 产品业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:product:edit")
    @Log(title = "产品管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotProductBo bo) {
        return toAjax(productService.updateByBo(bo));
    }

    /**
     * 删除产品
     *
     * @param productIds 产品ID数组
     * @return 操作结果
     */
    @SaCheckPermission("iot:product:remove")
    @Log(title = "产品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public R<Void> remove(@PathVariable Long[] productIds) {
        return toAjax(productService.deleteWithValidByIds(List.of(productIds), true));
    }

}
