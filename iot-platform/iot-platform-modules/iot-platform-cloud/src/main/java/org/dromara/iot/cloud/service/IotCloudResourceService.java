package org.dromara.iot.cloud.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.cloud.domain.bo.IotCloudResourceBo;
import org.dromara.iot.cloud.domain.vo.IotCloudResourceVo;

import java.util.Collection;
import java.util.List;

/**
 * 云资源 服务层
 *
 * @author iot team
 */
public interface IotCloudResourceService {

    /**
     * 分页查询纳管资源（本地库）
     */
    PageResult<IotCloudResourceVo> queryPageList(IotCloudResourceBo bo, PageQuery pageQuery);

    /**
     * 查询资源详情
     */
    IotCloudResourceVo queryById(Long resourceId);

    /**
     * 从云平台同步资源清单（增量 upsert）
     *
     * @param accountId 云账号ID
     * @param query     过滤条件，可为空
     */
    List<IotCloudResourceVo> syncResources(Long accountId, IotCloudResourceBo query);

    /**
     * 在云平台创建资源并纳管
     */
    Boolean createResource(IotCloudResourceBo bo);

    /**
     * 启动资源
     */
    Boolean startResource(Long resourceId);

    /**
     * 停止资源
     */
    Boolean stopResource(Long resourceId);

    /**
     * 释放资源（云端删除 + 本地移除）
     */
    Boolean deleteResourceById(Long resourceId);

    /**
     * 批量移除本地纳管记录（不调用云端）
     */
    Boolean deleteByIds(Collection<Long> resourceIds);

}
