package org.dromara.iot.job.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.job.domain.bo.IotJobInfoBo;
import org.dromara.iot.job.domain.vo.IotJobInfoVo;
import org.dromara.iot.job.domain.vo.IotJobLogVo;
import org.dromara.iot.job.service.IotJobInfoService;
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
import java.util.Map;

/**
 * 任务管理（自建调度 + SnailJob 分布式调度）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/job/info")
public class IotJobInfoController extends BaseController {

    private final IotJobInfoService jobInfoService;

    /**
     * 分页查询任务
     */
    @SaCheckPermission("iot:job:info:list")
    @GetMapping("/list")
    public R<PageResult<IotJobInfoVo>> list(IotJobInfoBo bo, PageQuery pageQuery) {
        return R.ok(jobInfoService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询任务详情
     */
    @SaCheckPermission("iot:job:info:query")
    @GetMapping("/{jobId}")
    public R<IotJobInfoVo> getInfo(@PathVariable Long jobId) {
        return R.ok(jobInfoService.queryById(jobId));
    }

    /**
     * 新增任务
     */
    @SaCheckPermission("iot:job:info:add")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotJobInfoBo bo) {
        return toAjax(jobInfoService.insertByBo(bo));
    }

    /**
     * 修改任务
     */
    @SaCheckPermission("iot:job:info:edit")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotJobInfoBo bo) {
        return toAjax(jobInfoService.updateByBo(bo));
    }

    /**
     * 删除任务
     */
    @SaCheckPermission("iot:job:info:remove")
    @Log(title = "任务管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobIds}")
    public R<Void> remove(@PathVariable Long[] jobIds) {
        return toAjax(jobInfoService.deleteByIds(List.of(jobIds)));
    }

    /**
     * 启停任务
     */
    @SaCheckPermission("iot:job:info:changeStatus")
    @Log(title = "任务启停", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/changeStatus/{jobId}/{status}")
    public R<Void> changeStatus(@PathVariable Long jobId, @PathVariable String status) {
        return toAjax(jobInfoService.changeStatus(jobId, status));
    }

    /**
     * 手动触发一次任务
     */
    @SaCheckPermission("iot:job:info:trigger")
    @Log(title = "任务手动触发", businessType = BusinessType.OTHER)
    @RepeatSubmit()
    @PostMapping("/trigger/{jobId}")
    public R<IotJobLogVo> trigger(@PathVariable Long jobId) {
        return R.ok(jobInfoService.triggerOnce(jobId));
    }

    /**
     * 查询可用执行器列表
     */
    @SaCheckPermission("iot:job:info:list")
    @GetMapping("/handlers")
    public R<Map<String, String>> handlers() {
        return R.ok(jobInfoService.handlerNames());
    }

}
