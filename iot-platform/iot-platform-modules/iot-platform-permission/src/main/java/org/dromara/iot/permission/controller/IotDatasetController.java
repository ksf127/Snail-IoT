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
import org.dromara.iot.permission.domain.bo.IotDatasetBo;
import org.dromara.iot.permission.domain.bo.IotDatasetRoleBo;
import org.dromara.iot.permission.domain.vo.IotDatasetRoleVo;
import org.dromara.iot.permission.domain.vo.IotDatasetVo;
import org.dromara.iot.permission.service.IIotDatasetService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据集管理（数据线：数据资产）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/dataset")
public class IotDatasetController extends BaseController {

    private final IIotDatasetService datasetService;

    /**
     * 分页查询数据集列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 数据集分页结果
     */
    @SaCheckPermission("iot:dataset:list")
    @GetMapping("/list")
    public R<PageResult<IotDatasetVo>> list(IotDatasetBo bo, PageQuery pageQuery) {
        return R.ok(datasetService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询数据集下拉选项（供应用绑定选择使用）
     *
     * @param bo 查询条件
     * @return 数据集列表
     */
    @SaCheckPermission("iot:dataset:list")
    @GetMapping("/options")
    public R<List<IotDatasetVo>> options(IotDatasetBo bo) {
        return R.ok(datasetService.queryList(bo));
    }

    /**
     * 导出数据集列表
     *
     * @param bo       查询条件
     * @param response HTTP 响应
     */
    @Log(title = "数据集管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("iot:dataset:export")
    @PostMapping("/export")
    public void export(IotDatasetBo bo, HttpServletResponse response) {
        List<IotDatasetVo> list = datasetService.queryList(bo);
        ExcelBuilder.of(list, IotDatasetVo.class).sheetName("数据集").toResponse(response);
    }

    /**
     * 获取数据集详细信息
     *
     * @param datasetId 数据集ID
     * @return 数据集详情
     */
    @SaCheckPermission("iot:dataset:query")
    @GetMapping("/{datasetId}")
    public R<IotDatasetVo> getInfo(@PathVariable Long datasetId) {
        return R.ok(datasetService.queryById(datasetId));
    }

    /**
     * 新增数据集
     *
     * @param bo 数据集业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:dataset:add")
    @Log(title = "数据集管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotDatasetBo bo) {
        return toAjax(datasetService.insertByBo(bo));
    }

    /**
     * 修改数据集
     *
     * @param bo 数据集业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:dataset:edit")
    @Log(title = "数据集管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotDatasetBo bo) {
        return toAjax(datasetService.updateByBo(bo));
    }

    /**
     * 删除数据集
     *
     * @param datasetIds 数据集ID数组
     * @return 操作结果
     */
    @SaCheckPermission("iot:dataset:remove")
    @Log(title = "数据集管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{datasetIds}")
    public R<Void> remove(@PathVariable Long[] datasetIds) {
        return toAjax(datasetService.deleteWithValidByIds(List.of(datasetIds), true));
    }

    /**
     * 查询数据集的角色数据权限
     *
     * @param datasetId 数据集ID
     * @return 角色数据权限列表
     */
    @SaCheckPermission("iot:dataset:auth")
    @GetMapping("/{datasetId}/roles")
    public R<List<IotDatasetRoleVo>> roleScopes(@PathVariable Long datasetId) {
        return R.ok(datasetService.queryRoleScopes(datasetId));
    }

    /**
     * 保存数据集的角色数据权限
     *
     * @param datasetId 数据集ID
     * @param scopes    角色数据权限列表
     * @return 操作结果
     */
    @SaCheckPermission("iot:dataset:auth")
    @Log(title = "数据集管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{datasetId}/roles")
    public R<Void> saveRoleScopes(@PathVariable Long datasetId, @RequestBody List<IotDatasetRoleBo> scopes) {
        return toAjax(datasetService.saveRoleScopes(datasetId, scopes));
    }

}
