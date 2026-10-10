package org.dromara.iot.deploy.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.iot.deploy.service.IDeployService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 自动化部署
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/deploy")
public class DeployController {

    private final IDeployService deployService;

    /**
     * 分页查询部署任务
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 部署任务分页结果
     */
    @SaCheckPermission("iot:deploy:list")
    @GetMapping("/list")
    public R<PageResult<Map<String, Object>>> list(@RequestParam Map<String, Object> query, PageQuery pageQuery) {
        return R.ok(deployService.queryPageList(query, pageQuery));
    }

    /**
     * 触发部署发布
     *
     * @param appCode   应用编码
     * @param version   版本号
     * @param targetEnv 目标环境
     * @return 部署任务ID
     */
    @SaCheckPermission("iot:deploy:release")
    @Log(title = "自动化部署", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/release")
    public R<Long> release(String appCode, String version, String targetEnv) {
        return R.ok(deployService.release(appCode, version, targetEnv));
    }

    /**
     * 查询部署日志
     *
     * @param taskId 部署任务ID
     * @return 部署日志
     */
    @SaCheckPermission("iot:deploy:query")
    @GetMapping("/{taskId}/log")
    public R<String> log(@PathVariable Long taskId) {
        return R.ok(deployService.queryLog(taskId));
    }

}
