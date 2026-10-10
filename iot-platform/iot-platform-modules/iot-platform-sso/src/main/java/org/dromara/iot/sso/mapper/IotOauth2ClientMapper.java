package org.dromara.iot.sso.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.sso.domain.IotOauth2Client;
import org.dromara.iot.sso.domain.vo.IotOauth2ClientVo;

/**
 * OAuth2 客户端注册 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotOauth2ClientMapper extends BaseMapperPlus<IotOauth2Client, IotOauth2ClientVo> {

}
