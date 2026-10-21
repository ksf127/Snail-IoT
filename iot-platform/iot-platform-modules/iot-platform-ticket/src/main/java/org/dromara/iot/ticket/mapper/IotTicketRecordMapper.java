package org.dromara.iot.ticket.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.ticket.domain.IotTicketRecord;
import org.dromara.iot.ticket.domain.vo.IotTicketRecordVo;

/**
 * 工单流转记录 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotTicketRecordMapper extends BaseMapperPlus<IotTicketRecord, IotTicketRecordVo> {

}
