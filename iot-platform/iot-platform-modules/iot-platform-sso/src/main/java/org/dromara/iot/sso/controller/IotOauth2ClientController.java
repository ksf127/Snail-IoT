package org.dromara.iot.sso.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.iot.sso.domain.bo.IotOauth2ClientBo;
import org.dromara.iot.sso.domain.vo.IotOauth2ClientVo;
import org.dromara.iot.sso.service.IIotOauth2ClientService;
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
 * OAuth2 客户端注册管理
 * <p>
 * 维护统一认证中心对外签发票据所需的客户端注册信息（Web / 移动端 / 三方系统）。
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/oauth2-client")
public class IotOauth2ClientController extends BaseController {

    private final IIotOauth2ClientService oauth2ClientService;

    /**
     * 查询客户端列表
     *
     * @param bo 查询条件
     * @return 客户端列表
     */
    @SaCheckPermission("iot:oauth2Client:list")
    @GetMapping("/list")
    public R<List<IotOauth2ClientVo>> list(IotOauth2ClientBo bo) {
        return R.ok(oauth2ClientService.queryList(bo));
    }

    /**
     * 导出客户端列表
     *
     * @param bo       查询条件
     * @param response HTTP 响应
     */
    @Log(title = "OAuth2客户端", businessType = BusinessType.EXPORT)
    @SaCheckPermission("iot:oauth2Client:export")
    @PostMapping("/export")
    public void export(IotOauth2ClientBo bo, HttpServletResponse response) {
        List<IotOauth2ClientVo> list = oauth2ClientService.queryList(bo);
        ExcelBuilder.of(list, IotOauth2ClientVo.class).sheetName("OAuth2客户端").toResponse(response);
    }

    /**
     * 获取客户端详细信息
     *
     * @param id 主键ID
     * @return 客户端详情
     */
    @SaCheckPermission("iot:oauth2Client:query")
    @GetMapping("/{id}")
    public R<IotOauth2ClientVo> getInfo(@PathVariable Long id) {
        return R.ok(oauth2ClientService.queryById(id));
    }

    /**
     * 新增客户端
     *
     * @param bo 客户端业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:oauth2Client:add")
    @Log(title = "OAuth2客户端", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotOauth2ClientBo bo) {
        return toAjax(oauth2ClientService.insertByBo(bo));
    }

    /**
     * 修改客户端
     *
     * @param bo 客户端业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:oauth2Client:edit")
    @Log(title = "OAuth2客户端", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotOauth2ClientBo bo) {
        return toAjax(oauth2ClientService.updateByBo(bo));
    }

    /**
     * 删除客户端
     *
     * @param ids 主键ID数组
     * @return 操作结果
     */
    @SaCheckPermission("iot:oauth2Client:remove")
    @Log(title = "OAuth2客户端", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(oauth2ClientService.deleteWithValidByIds(List.of(ids)));
    }

}
