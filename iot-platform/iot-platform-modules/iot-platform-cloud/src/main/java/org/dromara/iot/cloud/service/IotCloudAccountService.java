package org.dromara.iot.cloud.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.cloud.domain.bo.IotCloudAccountBo;
import org.dromara.iot.cloud.domain.vo.IotCloudAccountVo;
import org.dromara.iot.cloud.provider.model.CloudConnectionResult;
import org.dromara.iot.cloud.provider.model.CloudRegionDTO;

import java.util.Collection;
import java.util.List;

/**
 * 云账号 服务层
 *
 * @author iot team
 */
public interface IotCloudAccountService {

    /**
     * 分页查询云账号
     */
    PageResult<IotCloudAccountVo> queryPageList(IotCloudAccountBo bo, PageQuery pageQuery);

    /**
     * 查询云账号详情
     */
    IotCloudAccountVo queryById(Long accountId);

    /**
     * 新增云账号
     */
    Boolean insertByBo(IotCloudAccountBo bo);

    /**
     * 修改云账号
     */
    Boolean updateByBo(IotCloudAccountBo bo);

    /**
     * 删除云账号（级联清理纳管资源与 Flink 作业）
     */
    Boolean deleteByIds(Collection<Long> accountIds);

    /**
     * 连通性检测
     */
    CloudConnectionResult testConnection(Long accountId);

    /**
     * 查询该账号对应云平台的地域列表
     */
    List<CloudRegionDTO> listRegions(Long accountId);

}
