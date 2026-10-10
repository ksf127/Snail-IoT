package org.dromara.iot.permission.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.permission.domain.bo.IotAppBo;
import org.dromara.iot.permission.domain.vo.IotAppVo;

import java.util.Collection;
import java.util.List;

/**
 * 应用 服务层
 *
 * @author iot team
 */
public interface IIotAppService {

    /**
     * 查询应用详情
     *
     * @param appId 应用ID
     * @return 应用视图对象
     */
    IotAppVo queryById(Long appId);

    /**
     * 分页查询应用列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<IotAppVo> queryPageList(IotAppBo bo, PageQuery pageQuery);

    /**
     * 查询应用列表（不分页）
     *
     * @param bo 查询条件
     * @return 应用列表
     */
    List<IotAppVo> queryList(IotAppBo bo);

    /**
     * 新增应用
     *
     * @param bo 应用业务对象
     * @return 是否成功
     */
    Boolean insertByBo(IotAppBo bo);

    /**
     * 修改应用
     *
     * @param bo 应用业务对象
     * @return 是否成功
     */
    Boolean updateByBo(IotAppBo bo);

    /**
     * 批量删除应用
     *
     * @param ids     应用ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 查询应用引用的数据集ID列表
     *
     * @param appId 应用ID
     * @return 数据集ID列表
     */
    List<Long> queryDatasetIds(Long appId);

    /**
     * 保存应用引用的数据集（全量覆盖）
     *
     * @param appId      应用ID
     * @param datasetIds 数据集ID列表
     * @return 是否成功
     */
    Boolean saveDatasets(Long appId, List<Long> datasetIds);

    /**
     * 查询应用已授权的角色ID列表
     *
     * @param appId 应用ID
     * @return 角色ID列表
     */
    List<Long> queryRoleIds(Long appId);

    /**
     * 保存应用授权的角色（全量覆盖）
     *
     * @param appId   应用ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    Boolean saveRoles(Long appId, List<Long> roleIds);

}
