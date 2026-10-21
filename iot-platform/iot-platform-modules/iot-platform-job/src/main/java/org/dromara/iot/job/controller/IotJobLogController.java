package org.dromara.iot.job.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.job.domain.bo.IotJobLogBo;
import org.dromara.iot.job.domain.vo.IotJobLogVo;
import org.dromara.iot.job.service.IotJobLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 任务执行日志
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/job/log")
public class IotJobLogController extends BaseController {

    private final IotJobLogService jobLogService;

    /**
     * 分页查询执行日志
     */
    @SaCheckPermission("iot:job:log:list")
    @GetMapping("/list")
    public R<PageResult<IotJobLogVo>> list(IotJobLogBo bo, PageQuery pageQuery) {
        return R.ok(jobLogService.queryPageList(bo, pageQuery));
    }

    /**
     * 删除执行日志
     */
    @SaCheckPermission("iot:job:log:remove")
    @Log(title = "任务执行日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{logIds}")
    public R<Void> remove(@PathVariable Long[] logIds) {
        return toAjax(jobLogService.deleteByIds(List.of(logIds)));
    }

}
