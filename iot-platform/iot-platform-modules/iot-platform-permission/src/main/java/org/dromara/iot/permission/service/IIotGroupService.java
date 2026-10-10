package org.dromara.iot.permission.service;

import org.dromara.iot.permission.domain.bo.IotGroupBo;
import org.dromara.iot.permission.domain.vo.IotGroupVo;

import java.util.Collection;
import java.util.List;

/**
 * 数据分组 服务层
 *
 * @author iot team
 */
public interface IIotGroupService {

    /**
     * 查询分组详情
     *
     * @param groupId 分组ID
     * @return 分组视图对象
     */
    IotGroupVo queryById(Long groupId);

    /**
     * 查询分组列表
     *
     * @param bo 查询条件
     * @return 分组列表
     */
    List<IotGroupVo> queryList(IotGroupBo bo);

    /**
     * 新增分组
     *
     * @param bo 分组业务对象
     * @return 是否成功
     */
    Boolean insertByBo(IotGroupBo bo);

    /**
     * 修改分组
     *
     * @param bo 分组业务对象
     * @return 是否成功
     */
    Boolean updateByBo(IotGroupBo bo);

    /**
     * 批量删除分组
     *
     * @param ids     分组ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
