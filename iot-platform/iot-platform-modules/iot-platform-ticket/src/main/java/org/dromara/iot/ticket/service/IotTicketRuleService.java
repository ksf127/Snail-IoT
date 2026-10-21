package org.dromara.iot.ticket.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.ticket.domain.bo.IotTicketRuleBo;
import org.dromara.iot.ticket.domain.vo.IotTicketRuleVo;

import java.util.Collection;
import java.util.List;

/**
 * 工单规则 服务层
 *
 * @author iot team
 */
public interface IotTicketRuleService {

    /**
     * 分页查询工单规则
     */
    PageResult<IotTicketRuleVo> queryPageList(IotTicketRuleBo bo, PageQuery pageQuery);

    /**
     * 查询启用的工单规则列表（按排序号升序）
     */
    List<IotTicketRuleVo> listEnabled();

    /**
     * 查询工单规则详情
     */
    IotTicketRuleVo queryById(Long ruleId);

    /**
     * 新增工单规则
     */
    Boolean createRule(IotTicketRuleBo bo);

    /**
     * 修改工单规则
     */
    Boolean updateRule(IotTicketRuleBo bo);

    /**
     * 批量删除工单规则
     */
    Boolean deleteRule(Collection<Long> ruleIds);

}
