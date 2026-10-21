package org.dromara.iot.ticket.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.ticket.domain.IotTicketRule;
import org.dromara.iot.ticket.domain.bo.IotTicketRuleBo;
import org.dromara.iot.ticket.domain.vo.IotTicketRuleVo;
import org.dromara.iot.ticket.enums.TicketRuleTypeEnum;
import org.dromara.iot.ticket.mapper.IotTicketRuleMapper;
import org.dromara.iot.ticket.service.IotTicketRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 工单规则 服务层实现
 *
 * @author iot team
 */
@Service
@RequiredArgsConstructor
public class IotTicketRuleServiceImpl implements IotTicketRuleService {

    private final IotTicketRuleMapper ruleMapper;

    @Override
    public PageResult<IotTicketRuleVo> queryPageList(IotTicketRuleBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotTicketRule> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getRuleCode()), IotTicketRule::getRuleCode, bo.getRuleCode());
        lqw.like(StringUtils.isNotBlank(bo.getRuleName()), IotTicketRule::getRuleName, bo.getRuleName());
        lqw.eq(StringUtils.isNotBlank(bo.getRuleType()), IotTicketRule::getRuleType, bo.getRuleType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotTicketRule::getStatus, bo.getStatus());
        lqw.orderByAsc(IotTicketRule::getSortNo);
        lqw.orderByDesc(IotTicketRule::getCreateTime);
        Page<IotTicketRuleVo> page = ruleMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<IotTicketRuleVo> listEnabled() {
        return ruleMapper.selectVoList(Wrappers.<IotTicketRule>lambdaQuery()
            .eq(IotTicketRule::getStatus, "0")
            .orderByAsc(IotTicketRule::getSortNo));
    }

    @Override
    public IotTicketRuleVo queryById(Long ruleId) {
        return ruleMapper.selectVoById(ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createRule(IotTicketRuleBo bo) {
        requireRuleType(bo.getRuleType());
        checkRuleCodeUnique(bo.getRuleCode(), null);
        IotTicketRule add = MapstructUtils.convert(bo, IotTicketRule.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus("0");
        }
        if (add.getSortNo() == null) {
            add.setSortNo(0);
        }
        return ruleMapper.insert(add) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRule(IotTicketRuleBo bo) {
        IotTicketRule exist = ruleMapper.selectById(bo.getRuleId());
        if (exist == null) {
            throw new ServiceException("工单规则不存在：" + bo.getRuleId());
        }
        requireRuleType(bo.getRuleType());
        checkRuleCodeUnique(bo.getRuleCode(), bo.getRuleId());
        IotTicketRule update = MapstructUtils.convert(bo, IotTicketRule.class);
        return ruleMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRule(Collection<Long> ruleIds) {
        if (CollUtil.isEmpty(ruleIds)) {
            return false;
        }
        return ruleMapper.deleteByIds(ruleIds) > 0;
    }

    /**
     * 校验规则类型合法性
     */
    private void requireRuleType(String ruleType) {
        if (TicketRuleTypeEnum.fromCode(ruleType) == null) {
            throw new ServiceException("不支持的规则类型：" + ruleType);
        }
    }

    /**
     * 校验规则编码唯一
     */
    private void checkRuleCodeUnique(String ruleCode, Long ruleId) {
        boolean exists = ruleMapper.exists(Wrappers.<IotTicketRule>lambdaQuery()
            .eq(IotTicketRule::getRuleCode, ruleCode)
            .ne(ruleId != null, IotTicketRule::getRuleId, ruleId));
        if (exists) {
            throw new ServiceException("规则编码已存在：" + ruleCode);
        }
    }

}
