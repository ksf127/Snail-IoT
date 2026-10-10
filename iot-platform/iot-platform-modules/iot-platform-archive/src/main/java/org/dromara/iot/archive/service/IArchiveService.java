package org.dromara.iot.archive.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.archive.domain.bo.IotArchiveBo;
import org.dromara.iot.archive.domain.vo.IotArchiveVo;

/**
 * 档案管理 服务层
 * <p>
 * 档案包括设备档案、运维记录、归档附件等。
 *
 * @author iot team
 */
public interface IArchiveService {

    /**
     * 分页查询档案列表
     *
     * @param bo        查询条件（档案类型、业务主键等）
     * @param pageQuery 分页参数
     * @return 档案分页结果
     */
    PageResult<IotArchiveVo> queryPageList(IotArchiveBo bo, PageQuery pageQuery);

    /**
     * 归档上传的文件
     *
     * @param archiveType 档案类型
     * @param bizId       业务主键（如设备ID）
     * @param ossId       对象存储文件ID
     * @return 归档记录ID
     */
    Long archiveFile(String archiveType, Long bizId, Long ossId);

}
