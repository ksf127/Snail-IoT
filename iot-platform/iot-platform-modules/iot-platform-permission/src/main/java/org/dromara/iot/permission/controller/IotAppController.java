package org.dromara.iot.permission.controller;

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
import org.dromara.iot.permission.domain.bo.IotAppBo;
import org.dromara.iot.permission.domain.vo.IotAppVo;
import org.dromara.iot.permission.service.IIotAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用管理（服务线：功能窗口，三线交汇点）
 * <p>
 * 应用绑定数据集（引用非存储）并授权角色。
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/app")
public class IotAppController extends BaseController {

    private final IIotAppService appService;

    /**
     * 分页查询应用列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 应用分页结果
     */
    @SaCheckPermission("iot:app:list")
    @GetMapping("/list")
    public R<PageResult<IotAppVo>> list(IotAppBo bo, PageQuery pageQuery) {
        return R.ok(appService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出应用列表
     *
     * @param bo       查询条件
     * @param response HTTP 响应
     */
    @Log(title = "应用管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("iot:app:export")
    @PostMapping("/export")
    public void export(IotAppBo bo, HttpServletResponse response) {
        List<IotAppVo> list = appService.queryList(bo);
        ExcelBuilder.of(list, IotAppVo.class).sheetName("应用").toResponse(response);
    }

    /**
     * 获取应用详细信息
     *
     * @param appId 应用ID
     * @return 应用详情
     */
    @SaCheckPermission("iot:app:query")
    @GetMapping("/{appId}")
    public R<IotAppVo> getInfo(@PathVariable Long appId) {
        return R.ok(appService.queryById(appId));
    }

    /**
     * 新增应用
     *
     * @param bo 应用业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:app:add")
    @Log(title = "应用管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotAppBo bo) {
        return toAjax(appService.insertByBo(bo));
    }

    /**
     * 修改应用
     *
     * @param bo 应用业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:app:edit")
    @Log(title = "应用管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotAppBo bo) {
        return toAjax(appService.updateByBo(bo));
    }

    /**
     * 删除应用
     *
     * @param appIds 应用ID数组
     * @return 操作结果
     */
    @SaCheckPermission("iot:app:remove")
    @Log(title = "应用管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{appIds}")
    public R<Void> remove(@PathVariable Long[] appIds) {
        return toAjax(appService.deleteWithValidByIds(List.of(appIds), true));
    }

    /**
     * 查询应用引用的数据集ID列表
     *
     * @param appId 应用ID
     * @return 数据集ID列表
     */
    @SaCheckPermission("iot:app:auth")
    @GetMapping("/{appId}/datasets")
    public R<List<Long>> datasets(@PathVariable Long appId) {
        return R.ok(appService.queryDatasetIds(appId));
    }

    /**
     * 保存应用引用的数据集
     *
     * @param appId      应用ID
     * @param datasetIds 数据集ID列表
     * @return 操作结果
     */
    @SaCheckPermission("iot:app:auth")
    @Log(title = "应用管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{appId}/datasets")
    public R<Void> saveDatasets(@PathVariable Long appId, @RequestBody List<Long> datasetIds) {
        return toAjax(appService.saveDatasets(appId, datasetIds));
    }

    /**
     * 查询应用已授权的角色ID列表
     *
     * @param appId 应用ID
     * @return 角色ID列表
     */
    @SaCheckPermission("iot:app:auth")
    @GetMapping("/{appId}/roles")
    public R<List<Long>> roles(@PathVariable Long appId) {
        return R.ok(appService.queryRoleIds(appId));
    }

    /**
     * 保存应用授权的角色
     *
     * @param appId   应用ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    @SaCheckPermission("iot:app:auth")
    @Log(title = "应用管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{appId}/roles")
    public R<Void> saveRoles(@PathVariable Long appId, @RequestBody List<Long> roleIds) {
        return toAjax(appService.saveRoles(appId, roleIds));
    }

}
