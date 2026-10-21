package org.dromara.iot.deploy.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.deploy.domain.bo.IotDeployAppBo;
import org.dromara.iot.deploy.domain.bo.IotDeployEnvBo;
import org.dromara.iot.deploy.domain.bo.IotDeployTaskBo;
import org.dromara.iot.deploy.domain.vo.IotDeployAppVo;
import org.dromara.iot.deploy.domain.vo.IotDeployEnvVo;
import org.dromara.iot.deploy.domain.vo.IotDeployTaskVo;
import org.dromara.iot.deploy.service.IDeployService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 自动化部署
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/deploy")
public class DeployController extends BaseController {

    private final IDeployService deployService;

    /**
     * 分页查询部署任务
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 部署任务分页结果
     */
    @SaCheckPermission("iot:deploy:list")
    @GetMapping("/list")
    public R<PageResult<IotDeployTaskVo>> list(IotDeployTaskBo bo, PageQuery pageQuery) {
        return R.ok(deployService.queryPageList(bo, pageQuery));
    }

    /**
     * 触发部署发布
     *
     * @param bo 发布参数（appCode / version / targetEnv）
     * @return 部署任务ID
     */
    @SaCheckPermission("iot:deploy:release")
    @Log(title = "自动化部署", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/release")
    public R<Long> release(IotDeployTaskBo bo) {
        return R.ok(deployService.release(bo));
    }

    /**
     * 登记构建任务
     *
     * @param bo 构建参数（appCode / version / targetEnv）
     * @return 任务ID
     */
    @SaCheckPermission("iot:deploy:build")
    @PostMapping("/task/build")
    public R<Long> createBuildTask(IotDeployTaskBo bo) {
        return R.ok(deployService.createBuildTask(bo));
    }

    /**
     * 回写构建任务结果
     *
     * @param taskId      任务ID
     * @param status      结果状态（2成功 3失败）
     * @param buildNumber Jenkins 构建号（可空）
     * @return 操作结果
     */
    @SaCheckPermission("iot:deploy:build")
    @PutMapping("/task/{taskId}/finish")
    public R<Void> finishBuildTask(@PathVariable Long taskId,
                                   @RequestParam String status,
                                   @RequestParam(required = false) Integer buildNumber) {
        deployService.finishBuildTask(taskId, status, buildNumber);
        return R.ok();
    }

    /**
     * 查询已登记的应用列表
     *
     * @return 应用列表
     */
    @SaCheckPermission("iot:deploy:list")
    @GetMapping("/app/list")
    public R<List<IotDeployAppVo>> appList() {
        return R.ok(deployService.listApps());
    }

    /**
     * 分页查询部署应用
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 应用分页结果
     */
    @SaCheckPermission("iot:deploy:list")
    @GetMapping("/app/page")
    public R<PageResult<IotDeployAppVo>> appPage(IotDeployAppBo bo, PageQuery pageQuery) {
        return R.ok(deployService.queryAppPageList(bo, pageQuery));
    }

    /**
     * 获取部署应用详情
     *
     * @param appId 应用ID
     * @return 应用详情
     */
    @SaCheckPermission("iot:deploy:query")
    @GetMapping("/app/{appId}")
    public R<IotDeployAppVo> getApp(@PathVariable Long appId) {
        return R.ok(deployService.queryAppById(appId));
    }

    /**
     * 新增部署应用
     *
     * @param bo 应用业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:deploy:app:add")
    @Log(title = "部署应用", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/app")
    public R<Void> addApp(@Validated @RequestBody IotDeployAppBo bo) {
        return toAjax(deployService.insertAppByBo(bo));
    }

    /**
     * 修改部署应用
     *
     * @param bo 应用业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:deploy:app:edit")
    @Log(title = "部署应用", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/app")
    public R<Void> editApp(@Validated @RequestBody IotDeployAppBo bo) {
        return toAjax(deployService.updateAppByBo(bo));
    }

    /**
     * 删除部署应用
     *
     * @param appIds 应用ID数组
     * @return 操作结果
     */
    @SaCheckPermission("iot:deploy:app:remove")
    @Log(title = "部署应用", businessType = BusinessType.DELETE)
    @DeleteMapping("/app/{appIds}")
    public R<Void> removeApp(@PathVariable Long[] appIds) {
        return toAjax(deployService.deleteAppByIds(List.of(appIds)));
    }

    /**
     * 查询已登记的环境列表
     *
     * @return 环境列表
     */
    @SaCheckPermission("iot:deploy:list")
    @GetMapping("/env/list")
    public R<List<IotDeployEnvVo>> envList() {
        return R.ok(deployService.listEnvs());
    }

    /**
     * 分页查询部署环境
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 环境分页结果
     */
    @SaCheckPermission("iot:deploy:list")
    @GetMapping("/env/page")
    public R<PageResult<IotDeployEnvVo>> envPage(IotDeployEnvBo bo, PageQuery pageQuery) {
        return R.ok(deployService.queryEnvPageList(bo, pageQuery));
    }

    /**
     * 获取部署环境详情
     *
     * @param envId 环境ID
     * @return 环境详情
     */
    @SaCheckPermission("iot:deploy:query")
    @GetMapping("/env/{envId}")
    public R<IotDeployEnvVo> getEnv(@PathVariable Long envId) {
        return R.ok(deployService.queryEnvById(envId));
    }

    /**
     * 新增部署环境
     *
     * @param bo 环境业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:deploy:env:add")
    @Log(title = "部署环境", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/env")
    public R<Void> addEnv(@Validated @RequestBody IotDeployEnvBo bo) {
        return toAjax(deployService.insertEnvByBo(bo));
    }

    /**
     * 修改部署环境
     *
     * @param bo 环境业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:deploy:env:edit")
    @Log(title = "部署环境", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/env")
    public R<Void> editEnv(@Validated @RequestBody IotDeployEnvBo bo) {
        return toAjax(deployService.updateEnvByBo(bo));
    }

    /**
     * 删除部署环境
     *
     * @param envIds 环境ID数组
     * @return 操作结果
     */
    @SaCheckPermission("iot:deploy:env:remove")
    @Log(title = "部署环境", businessType = BusinessType.DELETE)
    @DeleteMapping("/env/{envIds}")
    public R<Void> removeEnv(@PathVariable Long[] envIds) {
        return toAjax(deployService.deleteEnvByIds(List.of(envIds)));
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
