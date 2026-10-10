package org.dromara.iot.sso.service;

import org.dromara.iot.sso.domain.bo.IotOauth2ClientBo;
import org.dromara.iot.sso.domain.vo.IotOauth2ClientVo;

import java.util.Collection;
import java.util.List;

/**
 * OAuth2 客户端注册 服务层
 *
 * @author iot team
 */
public interface IIotOauth2ClientService {

    /**
     * 查询客户端详情
     *
     * @param id 主键ID
     * @return 客户端视图对象
     */
    IotOauth2ClientVo queryById(Long id);

    /**
     * 查询客户端列表
     *
     * @param bo 查询条件
     * @return 客户端列表
     */
    List<IotOauth2ClientVo> queryList(IotOauth2ClientBo bo);

    /**
     * 按客户端标识查询（带缓存，供 OAuth2 服务端解析客户端注册信息）
     *
     * @param clientId 客户端标识
     * @return 客户端视图对象，不存在时返回 null
     */
    IotOauth2ClientVo queryByClientId(String clientId);

    /**
     * 新增客户端
     *
     * @param bo 客户端业务对象
     * @return 是否成功
     */
    Boolean insertByBo(IotOauth2ClientBo bo);

    /**
     * 修改客户端
     *
     * @param bo 客户端业务对象
     * @return 是否成功
     */
    Boolean updateByBo(IotOauth2ClientBo bo);

    /**
     * 批量删除客户端
     *
     * @param ids 主键ID集合
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

}
