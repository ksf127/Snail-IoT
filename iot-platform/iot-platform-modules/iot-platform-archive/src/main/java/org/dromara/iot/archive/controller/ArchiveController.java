package org.dromara.iot.archive.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.archive.domain.bo.IotArchiveBo;
import org.dromara.iot.archive.domain.vo.IotArchiveVo;
import org.dromara.iot.archive.service.IArchiveService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 档案管理
 *
 * @author iot team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/archive")
public class ArchiveController {

    private final IArchiveService archiveService;

    /**
     * 分页查询档案列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 档案分页结果
     */
    @SaCheckPermission("iot:archive:list")
    @GetMapping("/list")
    public R<PageResult<IotArchiveVo>> list(IotArchiveBo bo, PageQuery pageQuery) {
        return R.ok(archiveService.queryPageList(bo, pageQuery));
    }

    /**
     * 归档对象存储文件
     *
     * @param archiveType 档案类型
     * @param bizId       业务主键
     * @param ossId       对象存储文件ID
     * @return 归档记录ID
     */
    @Log(title = "档案管理", businessType = BusinessType.INSERT)
    @SaCheckPermission("iot:archive:add")
    @PostMapping("/archive")
    public R<Long> archive(String archiveType, Long bizId, Long ossId) {
        return R.ok(archiveService.archiveFile(archiveType, bizId, ossId));
    }

}
