package org.dromara.iot.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.permission.domain.bo.IotGroupBo;
import org.dromara.iot.permission.domain.vo.IotGroupVo;
import org.dromara.iot.permission.service.IIotGroupService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据分组管理（数据线：数据的文件夹）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/group")
public class IotGroupController extends BaseController {

    private final IIotGroupService groupService;

    /**
     * 查询分组列表
     *
     * @param bo 查询条件
     * @return 分组列表
     */
    @SaCheckPermission("iot:group:list")
    @GetMapping("/list")
    public R<List<IotGroupVo>> list(IotGroupBo bo) {
        return R.ok(groupService.queryList(bo));
    }

    /**
     * 导出分组列表
     *
     * @param bo       查询条件
     * @param response HTTP 响应
     */
    @Log(title = "分组管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("iot:group:export")
    @PostMapping("/export")
    public void export(IotGroupBo bo, HttpServletResponse response) {
        List<IotGroupVo> list = groupService.queryList(bo);
        ExcelBuilder.of(list, IotGroupVo.class).sheetName("分组数据").toResponse(response);
    }

    /**
     * 获取分组详细信息
     *
     * @param groupId 分组ID
     * @return 分组详情
     */
    @SaCheckPermission("iot:group:query")
    @GetMapping("/{groupId}")
    public R<IotGroupVo> getInfo(@PathVariable Long groupId) {
        return R.ok(groupService.queryById(groupId));
    }

    /**
     * 新增分组
     *
     * @param bo 分组业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:group:add")
    @Log(title = "分组管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotGroupBo bo) {
        return toAjax(groupService.insertByBo(bo));
    }

    /**
     * 修改分组
     *
     * @param bo 分组业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:group:edit")
    @Log(title = "分组管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotGroupBo bo) {
        return toAjax(groupService.updateByBo(bo));
    }

    /**
     * 删除分组
     *
     * @param groupIds 分组ID数组
     * @return 操作结果
     */
    @SaCheckPermission("iot:group:remove")
    @Log(title = "分组管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{groupIds}")
    public R<Void> remove(@PathVariable Long[] groupIds) {
        return toAjax(groupService.deleteWithValidByIds(List.of(groupIds), true));
    }

}
