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
import org.dromara.iot.cloud.domain.bo.IotFlinkJobBo;
import org.dromara.iot.cloud.domain.vo.IotFlinkJobVo;
import org.dromara.iot.cloud.service.IotFlinkJobService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Flink 作业管理（JobManager 纳管）
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/cloud/flink")
public class IotFlinkJobController extends BaseController {

    private final IotFlinkJobService flinkJobService;

    /**
     * 分页查询 Flink 作业
     */
    @SaCheckPermission("iot:cloud:flink:list")
    @GetMapping("/list")
    public R<PageResult<IotFlinkJobVo>> list(IotFlinkJobBo bo, PageQuery pageQuery) {
        return R.ok(flinkJobService.queryPageList(bo, pageQuery));
    }

    /**
     * 查询作业详情
     */
    @SaCheckPermission("iot:cloud:flink:query")
    @GetMapping("/{jobId}")
    public R<IotFlinkJobVo> getInfo(@PathVariable Long jobId) {
        return R.ok(flinkJobService.queryById(jobId));
    }

    /**
     * 从 Flink JobManager 同步作业清单
     */
    @SaCheckPermission("iot:cloud:flink:sync")
    @Log(title = "Flink 作业同步", businessType = BusinessType.OTHER)
    @RepeatSubmit()
    @PostMapping("/sync/{accountId}")
    public R<List<IotFlinkJobVo>> sync(@PathVariable Long accountId) {
        return R.ok(flinkJobService.syncJobs(accountId));
    }

    /**
     * 优雅停止作业
     */
    @SaCheckPermission("iot:cloud:flink:operate")
    @Log(title = "Flink 作业停止", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/stop/{jobId}")
    public R<Void> stop(@PathVariable Long jobId) {
        return toAjax(flinkJobService.stopJob(jobId));
    }

    /**
     * 取消作业
     */
    @SaCheckPermission("iot:cloud:flink:operate")
    @Log(title = "Flink 作业取消", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/cancel/{jobId}")
    public R<Void> cancel(@PathVariable Long jobId) {
        return toAjax(flinkJobService.cancelJob(jobId));
    }

}
