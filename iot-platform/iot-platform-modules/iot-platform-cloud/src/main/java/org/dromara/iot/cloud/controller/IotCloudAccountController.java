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
import org.dromara.iot.cloud.domain.bo.IotCloudAccountBo;
import org.dromara.iot.cloud.domain.vo.IotCloudAccountVo;
import org.dromara.iot.cloud.provider.model.CloudConnectionResult;
import org.dromara.iot.cloud.provider.model.CloudRegionDTO;
import org.dromara.iot.cloud.service.IotCloudAccountService;
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
 * 云账号管理（云平台纳管：阿里云、Flink）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/cloud/account")
public class IotCloudAccountController extends BaseController {

    private final IotCloudAccountService cloudAccountService;

    /**
     * 分页查询云账号
     */
    @SaCheckPermission("iot:cloud:account:list")
    @GetMapping("/list")
    public R<PageResult<IotCloudAccountVo>> list(IotCloudAccountBo bo, PageQuery pageQuery) {
        return R.ok(cloudAccountService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询云账号详情
     */
    @SaCheckPermission("iot:cloud:account:query")
    @GetMapping("/{accountId}")
    public R<IotCloudAccountVo> getInfo(@PathVariable Long accountId) {
        return R.ok(cloudAccountService.queryById(accountId));
    }

    /**
     * 新增云账号
     */
    @SaCheckPermission("iot:cloud:account:add")
    @Log(title = "云账号", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotCloudAccountBo bo) {
        return toAjax(cloudAccountService.insertByBo(bo));
    }

    /**
     * 修改云账号
     */
    @SaCheckPermission("iot:cloud:account:edit")
    @Log(title = "云账号", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotCloudAccountBo bo) {
        return toAjax(cloudAccountService.updateByBo(bo));
    }

    /**
     * 删除云账号
     */
    @SaCheckPermission("iot:cloud:account:remove")
    @Log(title = "云账号", businessType = BusinessType.DELETE)
    @DeleteMapping("/{accountIds}")
    public R<Void> remove(@PathVariable Long[] accountIds) {
        return toAjax(cloudAccountService.deleteByIds(List.of(accountIds)));
    }

    /**
     * 连通性检测
     */
    @SaCheckPermission("iot:cloud:account:test")
    @Log(title = "云账号连通性检测", businessType = BusinessType.OTHER)
    @PostMapping("/test/{accountId}")
    public R<CloudConnectionResult> test(@PathVariable Long accountId) {
        return R.ok(cloudAccountService.testConnection(accountId));
    }

    /**
     * 查询云平台地域列表
     */
    @SaCheckPermission("iot:cloud:account:query")
    @GetMapping("/regions/{accountId}")
    public R<List<CloudRegionDTO>> regions(@PathVariable Long accountId) {
        return R.ok(cloudAccountService.listRegions(accountId));
    }

}
