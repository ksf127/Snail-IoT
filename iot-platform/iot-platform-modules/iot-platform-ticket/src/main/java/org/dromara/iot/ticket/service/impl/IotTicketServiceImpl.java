package org.dromara.iot.ticket.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.enums.BusinessStatusEnum;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.liteflow.utils.LiteFlowUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.ticket.domain.IotTicket;
import org.dromara.iot.ticket.domain.IotTicketRecord;
import org.dromara.iot.ticket.domain.bo.IotTicketBo;
import org.dromara.iot.ticket.domain.bo.IotTicketHandleBo;
import org.dromara.iot.ticket.domain.bo.IotTicketRecordBo;
import org.dromara.iot.ticket.domain.vo.IotTicketRecordVo;
import org.dromara.iot.ticket.domain.vo.IotTicketVo;
import org.dromara.iot.ticket.enums.TicketPriorityEnum;
import org.dromara.iot.ticket.enums.TicketStatusEnum;
import org.dromara.iot.ticket.enums.TicketTypeEnum;
import org.dromara.iot.ticket.liteflow.TicketRuleContext;
import org.dromara.iot.ticket.mapper.IotTicketMapper;
import org.dromara.iot.ticket.mapper.IotTicketRecordMapper;
import org.dromara.iot.ticket.service.IotTicketService;
import org.dromara.system.api.UserService;
import org.dromara.workflow.api.WorkflowService;
import org.dromara.workflow.api.domain.CompleteTaskDTO;
import org.dromara.workflow.api.domain.FlowInstanceBizExtDTO;
import org.dromara.workflow.api.domain.StartProcessDTO;
import org.dromara.workflow.api.domain.StartProcessReturnDTO;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工单 服务层实现
 *
 * @author iot team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotTicketServiceImpl implements IotTicketService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final IotTicketMapper ticketMapper;
    private final IotTicketRecordMapper recordMapper;
    private final UserService userService;
    private final ObjectProvider<WorkflowService> workflowServiceProvider;

    @Override
    public PageResult<IotTicketVo> queryPageList(IotTicketBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotTicket> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTicketNo()), IotTicket::getTicketNo, bo.getTicketNo());
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), IotTicket::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getTicketType()), IotTicket::getTicketType, bo.getTicketType());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), IotTicket::getCategory, bo.getCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getPriority()), IotTicket::getPriority, bo.getPriority());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotTicket::getStatus, bo.getStatus());
        lqw.eq(bo.getHandlerId() != null, IotTicket::getHandlerId, bo.getHandlerId());
        lqw.eq(bo.getApplicantId() != null, IotTicket::getApplicantId, bo.getApplicantId());
        lqw.orderByDesc(IotTicket::getCreateTime);
        Page<IotTicketVo> page = ticketMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public IotTicketVo queryById(Long ticketId) {
        return ticketMapper.selectVoById(ticketId);
    }

    @Override
    public List<IotTicketRecordVo> listRecords(IotTicketRecordBo bo) {
        return recordMapper.selectVoList(Wrappers.<IotTicketRecord>lambdaQuery()
            .eq(bo.getTicketId() != null, IotTicketRecord::getTicketId, bo.getTicketId())
            .eq(StringUtils.isNotBlank(bo.getAction()), IotTicketRecord::getAction, bo.getAction())
            .like(StringUtils.isNotBlank(bo.getOperatorName()), IotTicketRecord::getOperatorName, bo.getOperatorName())
            .orderByAsc(IotTicketRecord::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createTicket(IotTicketBo bo) {
        String ticketType = StringUtils.isNotBlank(bo.getTicketType())
            ? bo.getTicketType() : TicketTypeEnum.NORMAL.getCode();
        if (TicketTypeEnum.fromCode(ticketType) == null) {
            throw new ServiceException("不支持的工单类型：" + ticketType);
        }
        bo.setTicketType(ticketType);
        bo.setTicketNo(StringUtils.isNotBlank(bo.getTicketNo()) ? bo.getTicketNo() : generateNo());
        checkTicketNoUnique(bo.getTicketNo(), null);
        IotTicket add = MapstructUtils.convert(bo, IotTicket.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(TicketStatusEnum.PENDING.getCode());
        }
        if (StringUtils.isBlank(add.getPriority())) {
            add.setPriority(TicketPriorityEnum.MEDIUM.getCode());
        }
        if (StringUtils.isBlank(add.getSource())) {
            add.setSource("web");
        }
        if (add.getApplicantId() == null) {
            add.setApplicantId(LoginHelper.getUserId());
        }
        if (StringUtils.isBlank(add.getApplicantName())) {
            add.setApplicantName(LoginHelper.getUsername());
        }
        add.setAutoProcessed("0");
        boolean flag = ticketMapper.insert(add) > 0;
        if (flag) {
            addRecord(add.getTicketId(), "create", "创建工单", add.getApplicantId(), add.getApplicantName(),
                "新建工单：" + add.getTitle(), null);
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTicket(IotTicketBo bo) {
        IotTicket exist = ticketMapper.selectById(bo.getTicketId());
        if (exist == null) {
            throw new ServiceException("工单不存在：" + bo.getTicketId());
        }
        if (StringUtils.isNotBlank(bo.getTicketNo())) {
            checkTicketNoUnique(bo.getTicketNo(), bo.getTicketId());
        }
        IotTicket update = MapstructUtils.convert(bo, IotTicket.class);
        return ticketMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTicket(Collection<Long> ticketIds) {
        if (CollUtil.isEmpty(ticketIds)) {
            return false;
        }
        recordMapper.delete(Wrappers.<IotTicketRecord>lambdaQuery()
            .in(IotTicketRecord::getTicketId, ticketIds));
        return ticketMapper.deleteByIds(ticketIds) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitTicket(Long ticketId, String flowCode, String handler) {
        IotTicket ticket = requireTicket(ticketId);
        String ticketType = StringUtils.isNotBlank(ticket.getTicketType())
            ? ticket.getTicketType() : TicketTypeEnum.NORMAL.getCode();
        TicketTypeEnum typeEnum = TicketTypeEnum.fromCode(ticketType);
        if (typeEnum == null) {
            throw new ServiceException("不支持的工单类型：" + ticketType);
        }
        switch (typeEnum) {
            case RULE -> applyRuleEngine(ticket);
            case FLOW -> startTicketFlow(ticket, flowCode, handler);
            case NORMAL -> {
                if (TicketStatusEnum.PENDING.getCode().equals(ticket.getStatus())) {
                    ticket.setStatus(TicketStatusEnum.PROCESSING.getCode());
                    ticketMapper.updateById(ticket);
                }
                addRecord(ticketId, "submit", "提交工单", LoginHelper.getUserId(), LoginHelper.getUsername(),
                    "提交工单进入受理流程", null);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean runRuleEngine(Long ticketId) {
        IotTicket ticket = requireTicket(ticketId);
        applyRuleEngine(ticket);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleTicket(IotTicketHandleBo bo) {
        IotTicket ticket = requireTicket(bo.getTicketId());
        Long operatorId = LoginHelper.getUserId();
        String operatorName = LoginHelper.getUsername();
        String action = bo.getAction();
        switch (action) {
            case "accept" -> {
                Long handlerId = bo.getHandlerId() != null ? bo.getHandlerId() : operatorId;
                ticket.setHandlerId(handlerId);
                ticket.setHandlerName(resolveOperatorName(bo.getHandlerName(), handlerId, operatorName));
                if (TicketStatusEnum.PENDING.getCode().equals(ticket.getStatus())) {
                    ticket.setStatus(TicketStatusEnum.PROCESSING.getCode());
                }
            }
            case "handle" -> ticket.setStatus(TicketStatusEnum.PROCESSING.getCode());
            case "finish" -> {
                ticket.setStatus(TicketStatusEnum.FINISHED.getCode());
                ticket.setFinishTime(LocalDateTime.now());
            }
            case "close" -> {
                ticket.setStatus(TicketStatusEnum.CLOSED.getCode());
                ticket.setCloseTime(LocalDateTime.now());
            }
            case "reject" -> ticket.setStatus(TicketStatusEnum.REJECTED.getCode());
            default -> throw new ServiceException("不支持的办理动作：" + action);
        }
        if (StringUtils.isNotBlank(bo.getPriority())) {
            ticket.setPriority(bo.getPriority());
        }
        boolean flag = ticketMapper.updateById(ticket) > 0;
        if (flag) {
            addRecord(ticket.getTicketId(), action, actionNodeName(action), operatorId, operatorName,
                StringUtils.isNotBlank(bo.getMessage()) ? bo.getMessage() : actionNodeName(action),
                bo.getProcessTaskId());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeFlowTask(IotTicketHandleBo bo) {
        WorkflowService workflowService = requireWorkflowService();
        if (bo.getProcessTaskId() == null) {
            throw new ServiceException("流程任务ID不能为空");
        }
        IotTicket ticket = requireTicket(bo.getTicketId());
        CompleteTaskDTO completeTask = new CompleteTaskDTO();
        completeTask.setTaskId(bo.getProcessTaskId());
        completeTask.setMessage(bo.getMessage());
        Map<String, Object> variables = new HashMap<>(4);
        // 系统后台办理 忽略权限校验
        variables.put("ignore", true);
        completeTask.setVariables(variables);
        workflowService.completeTask(completeTask);
        addRecord(ticket.getTicketId(), "flow-complete", "流程办理", LoginHelper.getUserId(), LoginHelper.getUsername(),
            StringUtils.isNotBlank(bo.getMessage()) ? bo.getMessage() : "办理流程任务", bo.getProcessTaskId());
        syncFlowStatus(ticket.getTicketId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotTicketVo syncFlowStatus(Long ticketId) {
        IotTicket ticket = requireTicket(ticketId);
        WorkflowService workflowService = requireWorkflowService();
        String businessStatus = workflowService.getBusinessStatus(String.valueOf(ticketId));
        if (StringUtils.isBlank(businessStatus)) {
            return ticketMapper.selectVoById(ticketId);
        }
        String oldStatus = ticket.getStatus();
        ticket.setBusinessStatus(businessStatus);
        BusinessStatusEnum statusEnum = BusinessStatusEnum.getByStatus(businessStatus);
        if (statusEnum != null) {
            switch (statusEnum) {
                case FINISH -> {
                    ticket.setStatus(TicketStatusEnum.FINISHED.getCode());
                    ticket.setFinishTime(LocalDateTime.now());
                }
                case INVALID, TERMINATION, CANCEL -> {
                    ticket.setStatus(TicketStatusEnum.CLOSED.getCode());
                    ticket.setCloseTime(LocalDateTime.now());
                }
                case BACK -> ticket.setStatus(TicketStatusEnum.REJECTED.getCode());
                case WAITING, DRAFT -> ticket.setStatus(TicketStatusEnum.PROCESSING.getCode());
            }
        }
        ticketMapper.updateById(ticket);
        if (!StringUtils.equals(oldStatus, ticket.getStatus())) {
            addRecord(ticketId, "flow-sync", "流程状态同步", LoginHelper.getUserId(), LoginHelper.getUsername(),
                "流程状态更新为：" + BusinessStatusEnum.findByStatus(businessStatus), null);
        }
        return ticketMapper.selectVoById(ticketId);
    }

    /**
     * 执行规则引擎：改写工单并落库、写入流转记录
     */
    private void applyRuleEngine(IotTicket ticket) {
        TicketRuleContext context = new TicketRuleContext(ticket);
        try {
            LiteFlowUtils.execute(IotConstants.TICKET_RULE_CHAIN_ID, context);
        } catch (Exception e) {
            log.error("工单规则引擎执行失败，ticketId={}", ticket.getTicketId(), e);
            throw new ServiceException("工单规则引擎执行失败：" + e.getMessage());
        }
        ticketMapper.updateById(context.getTicket());
        if (context.isAutoProcessed()) {
            addRecord(ticket.getTicketId(), "rule", "规则引擎", LoginHelper.getUserId(), LoginHelper.getUsername(),
                "规则引擎自动处理，命中规则：" + context.getMatchedRuleCode(), null);
        } else {
            addRecord(ticket.getTicketId(), "rule", "规则引擎", LoginHelper.getUserId(), LoginHelper.getUsername(),
                "规则引擎执行完成，未命中任何规则", null);
        }
    }

    /**
     * 发起流程工单
     */
    private void startTicketFlow(IotTicket ticket, String flowCode, String handler) {
        WorkflowService workflowService = requireWorkflowService();
        String code = StringUtils.isNotBlank(flowCode) ? flowCode : ticket.getFlowCode();
        if (StringUtils.isBlank(code)) {
            throw new ServiceException("未指定流程定义编码，无法发起流程工单");
        }
        String businessId = String.valueOf(ticket.getTicketId());
        StartProcessDTO startProcess = new StartProcessDTO();
        startProcess.setBusinessId(businessId);
        startProcess.setFlowCode(code);
        startProcess.setHandler(handler);
        Map<String, Object> variables = new HashMap<>(4);
        // 后端发起 忽略权限校验
        variables.put("ignore", true);
        startProcess.setVariables(variables);
        FlowInstanceBizExtDTO bizExt = new FlowInstanceBizExtDTO();
        bizExt.setBusinessId(businessId);
        bizExt.setBusinessCode(ticket.getTicketNo());
        bizExt.setBusinessTitle(ticket.getTitle());
        startProcess.setBizExt(bizExt);

        StartProcessReturnDTO result = workflowService.startWorkFlow(startProcess);
        ticket.setFlowCode(code);
        if (result != null) {
            ticket.setProcessInstanceId(result.processInstanceId());
        }
        ticket.setBusinessStatus(BusinessStatusEnum.WAITING.getStatus());
        ticket.setStatus(TicketStatusEnum.PROCESSING.getCode());
        ticketMapper.updateById(ticket);
        addRecord(ticket.getTicketId(), "flow-start", "发起流程", LoginHelper.getUserId(), LoginHelper.getUsername(),
            "发起流程审批：" + code, result != null ? result.taskId() : null);
    }

    /**
     * 校验工单存在并返回实体
     */
    private IotTicket requireTicket(Long ticketId) {
        IotTicket ticket = ticketId == null ? null : ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new ServiceException("工单不存在：" + ticketId);
        }
        return ticket;
    }

    /**
     * 获取流程引擎服务，未启用时抛出明确异常
     */
    private WorkflowService requireWorkflowService() {
        WorkflowService workflowService = workflowServiceProvider.getIfAvailable();
        if (workflowService == null) {
            throw new ServiceException("流程引擎未启用，无法处理流程工单");
        }
        return workflowService;
    }

    /**
     * 解析办理人名称
     */
    private String resolveOperatorName(String handlerName, Long handlerId, String fallback) {
        if (StringUtils.isNotBlank(handlerName)) {
            return handlerName;
        }
        String nickname = handlerId == null ? null : userService.selectNicknameById(handlerId);
        return StringUtils.isNotBlank(nickname) ? nickname : fallback;
    }

    /**
     * 办理动作对应节点名称
     */
    private String actionNodeName(String action) {
        return switch (action) {
            case "accept" -> "受理工单";
            case "handle" -> "处理工单";
            case "finish" -> "完成工单";
            case "close" -> "关闭工单";
            case "reject" -> "驳回工单";
            default -> "办理工单";
        };
    }

    /**
     * 写入工单流转记录
     */
    private void addRecord(Long ticketId, String action, String nodeName, Long operatorId, String operatorName,
                           String message, Long processTaskId) {
        IotTicketRecord record = new IotTicketRecord();
        record.setTicketId(ticketId);
        record.setAction(action);
        record.setNodeName(nodeName);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setMessage(message);
        record.setProcessTaskId(processTaskId);
        recordMapper.insert(record);
    }

    /**
     * 校验工单编号唯一
     */
    private void checkTicketNoUnique(String ticketNo, Long ticketId) {
        boolean exists = ticketMapper.exists(Wrappers.<IotTicket>lambdaQuery()
            .eq(IotTicket::getTicketNo, ticketNo)
            .ne(ticketId != null, IotTicket::getTicketId, ticketId));
        if (exists) {
            throw new ServiceException("工单编号已存在：" + ticketNo);
        }
    }

    /**
     * 生成工单编号
     */
    private String generateNo() {
        return IotConstants.TICKET_NO_PREFIX + NO_FORMATTER.format(LocalDateTime.now())
            + RandomUtil.randomNumbers(4);
    }

}
