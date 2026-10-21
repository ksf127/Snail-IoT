package org.dromara.iot.ticket.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.ticket.domain.bo.IotTicketBo;
import org.dromara.iot.ticket.domain.bo.IotTicketHandleBo;
import org.dromara.iot.ticket.domain.bo.IotTicketRecordBo;
import org.dromara.iot.ticket.domain.vo.IotTicketRecordVo;
import org.dromara.iot.ticket.domain.vo.IotTicketVo;

import java.util.Collection;
import java.util.List;

/**
 * 工单 服务层
 * <p>
 * 统一编排三类工单：普通工单（人工流转）、规则工单（LiteFlow 规则引擎）、
 * 流程工单（Warm-Flow 流程引擎）。
 *
 * @author iot team
 */
public interface IotTicketService {

    /**
     * 分页查询工单
     */
    PageResult<IotTicketVo> queryPageList(IotTicketBo bo, PageQuery pageQuery);

    /**
     * 查询工单详情
     */
    IotTicketVo queryById(Long ticketId);

    /**
     * 查询工单流转记录
     */
    List<IotTicketRecordVo> listRecords(IotTicketRecordBo bo);

    /**
     * 新建工单
     */
    Boolean createTicket(IotTicketBo bo);

    /**
     * 修改工单
     */
    Boolean updateTicket(IotTicketBo bo);

    /**
     * 批量删除工单
     */
    Boolean deleteTicket(Collection<Long> ticketIds);

    /**
     * 提交工单：按工单类型触发规则引擎或流程引擎
     *
     * @param ticketId 工单ID
     * @param flowCode 流程定义编码（流程工单可覆盖工单上的编码）
     * @param handler  办理人（可选，覆盖流程首节点办理人）
     */
    Boolean submitTicket(Long ticketId, String flowCode, String handler);

    /**
     * 执行（或重跑）规则引擎处理工单
     */
    Boolean runRuleEngine(Long ticketId);

    /**
     * 人工办理工单（受理 / 处理 / 完成 / 关闭 / 驳回）
     */
    Boolean handleTicket(IotTicketHandleBo bo);

    /**
     * 办理流程任务并同步工单状态
     */
    Boolean completeFlowTask(IotTicketHandleBo bo);

    /**
     * 同步流程状态回写工单
     */
    IotTicketVo syncFlowStatus(Long ticketId);

}
