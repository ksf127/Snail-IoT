package org.dromara.iot.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.archive.domain.IotArchive;
import org.dromara.iot.archive.domain.bo.IotArchiveBo;
import org.dromara.iot.archive.domain.vo.IotArchiveVo;
import org.dromara.iot.archive.mapper.IotArchiveMapper;
import org.dromara.iot.archive.service.IArchiveService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 档案管理 服务层实现
 * <p>
 * 档案元数据落 iot_archive 表，附件本体存对象存储（MinIO），通过 ossId 关联 sys_oss。
 *
 * @author iot team
 */
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements IArchiveService {

    private final IotArchiveMapper archiveMapper;

    /**
     * 分页查询档案列表
     *
     * @param bo        查询条件（档案类型、业务主键等）
     * @param pageQuery 分页参数
     * @return 档案分页结果
     */
    @Override
    public PageResult<IotArchiveVo> queryPageList(IotArchiveBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotArchive> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getArchiveType()), IotArchive::getArchiveType, bo.getArchiveType());
        lqw.eq(bo.getBizId() != null, IotArchive::getBizId, bo.getBizId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotArchive::getStatus, bo.getStatus());
        lqw.orderByDesc(IotArchive::getArchiveTime);
        Page<IotArchiveVo> page = archiveMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 归档上传的文件
     *
     * @param archiveType 档案类型
     * @param bizId       业务主键（如设备ID）
     * @param ossId       对象存储文件ID
     * @return 归档记录ID
     */
    @Override
    public Long archiveFile(String archiveType, Long bizId, Long ossId) {
        if (StringUtils.isBlank(archiveType)) {
            throw new ServiceException("档案类型不能为空");
        }
        if (ossId == null) {
            throw new ServiceException("对象存储文件ID不能为空");
        }
        IotArchive archive = new IotArchive();
        archive.setArchiveType(archiveType);
        archive.setBizId(bizId);
        archive.setOssId(ossId);
        archive.setArchiveTime(LocalDateTime.now());
        archive.setStatus("0");
        archiveMapper.insert(archive);
        return archive.getArchiveId();
    }

}
