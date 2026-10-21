package org.dromara.iot.ticket.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.ticket.domain.IotTicket;
import org.dromara.iot.ticket.domain.vo.IotTicketVo;

/**
 * 工单 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotTicketMapper extends BaseMapperPlus<IotTicket, IotTicketVo> {

}
