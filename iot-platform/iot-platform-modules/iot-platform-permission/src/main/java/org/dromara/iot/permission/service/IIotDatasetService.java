package org.dromara.iot.permission.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.permission.domain.bo.IotDatasetBo;
import org.dromara.iot.permission.domain.bo.IotDatasetRoleBo;
import org.dromara.iot.permission.domain.vo.IotDatasetRoleVo;
import org.dromara.iot.permission.domain.vo.IotDatasetVo;

import java.util.Collection;
import java.util.List;

/**
 * 数据集 服务层
 *
 * @author iot team
 */
public interface IIotDatasetService {

    /**
     * 查询数据集详情
     *
     * @param datasetId 数据集ID
     * @return 数据集视图对象
     */
    IotDatasetVo queryById(Long datasetId);

    /**
     * 分页查询数据集列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<IotDatasetVo> queryPageList(IotDatasetBo bo, PageQuery pageQuery);

    /**
     * 查询数据集列表（不分页，供应用绑定选择使用）
     *
     * @param bo 查询条件
     * @return 数据集列表
     */
    List<IotDatasetVo> queryList(IotDatasetBo bo);

    /**
     * 新增数据集
     *
     * @param bo 数据集业务对象
     * @return 是否成功
     */
    Boolean insertByBo(IotDatasetBo bo);

    /**
     * 修改数据集
     *
     * @param bo 数据集业务对象
     * @return 是否成功
     */
    Boolean updateByBo(IotDatasetBo bo);

    /**
     * 批量删除数据集
     *
     * @param ids     数据集ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 查询数据集已配置的角色数据权限
     *
     * @param datasetId 数据集ID
     * @return 角色数据权限列表
     */
    List<IotDatasetRoleVo> queryRoleScopes(Long datasetId);

    /**
     * 保存数据集的角色数据权限（全量覆盖）
     *
     * @param datasetId 数据集ID
     * @param scopes    角色数据权限列表
     * @return 是否成功
     */
    Boolean saveRoleScopes(Long datasetId, List<IotDatasetRoleBo> scopes);

}
