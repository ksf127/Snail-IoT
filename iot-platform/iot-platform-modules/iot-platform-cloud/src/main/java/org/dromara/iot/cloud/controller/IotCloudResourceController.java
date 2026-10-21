package org.dromara.iot.cloud.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.cloud.domain.bo.IotCloudResourceBo;
import org.dromara.iot.cloud.domain.vo.IotCloudResourceVo;
import org.dromara.iot.cloud.service.IotCloudResourceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 云资源管理（纳管资源同步与生命周期操作）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/cloud/resource")
public class IotCloudResourceController extends BaseController {

    private final IotCloudResourceService cloudResourceService;

    /**
     * 分页查询纳管资源
     */
    @SaCheckPermission("iot:cloud:resource:list")
    @GetMapping("/list")
    public R<PageResult<IotCloudResourceVo>> list(IotCloudResourceBo bo, PageQuery pageQuery) {
        return R.ok(cloudResourceService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询资源详情
     */
    @SaCheckPermission("iot:cloud:resource:query")
    @GetMapping("/{resourceId}")
    public R<IotCloudResourceVo> getInfo(@PathVariable Long resourceId) {
        return R.ok(cloudResourceService.queryById(resourceId));
    }

    /**
     * 从云平台同步资源清单
     */
    @SaCheckPermission("iot:cloud:resource:sync")
    @Log(title = "云资源同步", businessType = BusinessType.OTHER)
    @RepeatSubmit()
    @PostMapping("/sync/{accountId}")
    public R<List<IotCloudResourceVo>> sync(@PathVariable Long accountId, IotCloudResourceBo bo) {
        return R.ok(cloudResourceService.syncResources(accountId, bo));
    }

    /**
     * 在云平台创建资源并纳管
     */
    @SaCheckPermission("iot:cloud:resource:add")
    @Log(title = "云资源创建", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotCloudResourceBo bo) {
        return toAjax(cloudResourceService.createResource(bo));
    }

    /**
     * 启动资源
     */
    @SaCheckPermission("iot:cloud:resource:operate")
    @Log(title = "云资源启动", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/start/{resourceId}")
    public R<Void> start(@PathVariable Long resourceId) {
        return toAjax(cloudResourceService.startResource(resourceId));
    }

    /**
     * 停止资源
     */
    @SaCheckPermission("iot:cloud:resource:operate")
    @Log(title = "云资源停止", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/stop/{resourceId}")
    public R<Void> stop(@PathVariable Long resourceId) {
        return toAjax(cloudResourceService.stopResource(resourceId));
    }

    /**
     * 释放资源（云端删除 + 本地移除）
     */
    @SaCheckPermission("iot:cloud:resource:operate")
    @Log(title = "云资源释放", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/release/{resourceId}")
    public R<Void> release(@PathVariable Long resourceId) {
        return toAjax(cloudResourceService.deleteResourceById(resourceId));
    }

    /**
     * 移除本地纳管记录
     */
    @SaCheckPermission("iot:cloud:resource:remove")
    @Log(title = "云资源移除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{resourceIds}")
    public R<Void> remove(@PathVariable Long[] resourceIds) {
        return toAjax(cloudResourceService.deleteByIds(List.of(resourceIds)));
    }

}
