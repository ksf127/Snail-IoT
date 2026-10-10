package org.dromara.iot.device.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.device.domain.bo.IotProductBo;
import org.dromara.iot.device.domain.vo.IotProductVo;

import java.util.Collection;
import java.util.List;

/**
 * 产品 服务层
 *
 * @author iot team
 */
public interface IIotProductService {

    /**
     * 查询产品详情
     *
     * @param productId 产品ID
     * @return 产品视图对象
     */
    IotProductVo queryById(Long productId);

    /**
     * 分页查询产品列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<IotProductVo> queryPageList(IotProductBo bo, PageQuery pageQuery);

    /**
     * 查询产品列表（不分页）
     *
     * @param bo 查询条件
     * @return 产品列表
     */
    List<IotProductVo> queryList(IotProductBo bo);

    /**
     * 新增产品
     *
     * @param bo 产品业务对象
     * @return 是否成功
     */
    Boolean insertByBo(IotProductBo bo);

    /**
     * 修改产品
     *
     * @param bo 产品业务对象
     * @return 是否成功
     */
    Boolean updateByBo(IotProductBo bo);

    /**
     * 批量删除产品
     *
     * @param ids    产品ID集合
     * @param isValid 是否进行业务校验
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
