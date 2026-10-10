package org.dromara.iot.device.controller;

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
import org.dromara.iot.device.domain.bo.IotDeviceBo;
import org.dromara.iot.device.domain.vo.IotDeviceVo;
import org.dromara.iot.device.service.IIotDeviceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备管理
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/device")
public class IotDeviceController extends BaseController {

    private final IIotDeviceService deviceService;

    /**
     * 分页查询设备列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 设备分页结果
     */
    @SaCheckPermission("iot:device:list")
    @GetMapping("/list")
    public R<PageResult<IotDeviceVo>> list(IotDeviceBo bo, PageQuery pageQuery) {
        return R.ok(deviceService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出设备列表
     *
     * @param bo       查询条件
     * @param response HTTP 响应
     */
    @Log(title = "设备管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("iot:device:export")
    @PostMapping("/export")
    public void export(IotDeviceBo bo, HttpServletResponse response) {
        List<IotDeviceVo> list = deviceService.queryList(bo);
        ExcelBuilder.of(list, IotDeviceVo.class).sheetName("设备数据").toResponse(response);
    }

    /**
     * 设备在线状态统计（监控大屏使用）
     *
     * @return 在线/离线/未知设备数量
     */
    @SaCheckPermission("iot:device:list")
    @GetMapping("/statistics")
    public R<Map<String, Long>> statistics() {
        Map<String, Long> result = new HashMap<>(4);
        result.put("total", deviceService.countByOnlineStatus(null));
        result.put("online", deviceService.countByOnlineStatus("1"));
        result.put("offline", deviceService.countByOnlineStatus("0"));
        result.put("unknown", deviceService.countByOnlineStatus("2"));
        return R.ok(result);
    }

    /**
     * 获取设备详细信息
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @SaCheckPermission("iot:device:query")
    @GetMapping("/{deviceId}")
    public R<IotDeviceVo> getInfo(@PathVariable Long deviceId) {
        return R.ok(deviceService.queryById(deviceId));
    }

    /**
     * 新增设备
     *
     * @param bo 设备业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:device:add")
    @Log(title = "设备管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody IotDeviceBo bo) {
        return toAjax(deviceService.insertByBo(bo));
    }

    /**
     * 修改设备
     *
     * @param bo 设备业务对象
     * @return 操作结果
     */
    @SaCheckPermission("iot:device:edit")
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody IotDeviceBo bo) {
        return toAjax(deviceService.updateByBo(bo));
    }

    /**
     * 删除设备
     *
     * @param deviceIds 设备ID数组
     * @return 操作结果
     */
    @SaCheckPermission("iot:device:remove")
    @Log(title = "设备管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deviceIds}")
    public R<Void> remove(@PathVariable Long[] deviceIds) {
        return toAjax(deviceService.deleteWithValidByIds(List.of(deviceIds), true));
    }

}
