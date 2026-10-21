package org.dromara.iot.ticket.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.ticket.domain.IotTicketRule;
import org.dromara.iot.ticket.domain.vo.IotTicketRuleVo;

/**
 * 工单规则 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotTicketRuleMapper extends BaseMapperPlus<IotTicketRule, IotTicketRuleVo> {

}
