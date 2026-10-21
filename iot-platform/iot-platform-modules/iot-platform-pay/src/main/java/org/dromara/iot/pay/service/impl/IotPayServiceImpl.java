package org.dromara.iot.pay.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.pay.channel.IPayChannel;
import org.dromara.iot.pay.channel.PayChannelFactory;
import org.dromara.iot.pay.channel.model.PayNotifyResult;
import org.dromara.iot.pay.channel.model.PrePayResult;
import org.dromara.iot.pay.channel.model.RefundResult;
import org.dromara.iot.pay.domain.IotPayChannelConfig;
import org.dromara.iot.pay.domain.IotPayOrder;
import org.dromara.iot.pay.domain.IotPayProduct;
import org.dromara.iot.pay.domain.IotPayRefund;
import org.dromara.iot.pay.domain.bo.IotPayChannelConfigBo;
import org.dromara.iot.pay.domain.bo.IotPayOrderBo;
import org.dromara.iot.pay.domain.bo.IotPayRefundBo;
import org.dromara.iot.pay.domain.vo.IotPayChannelConfigVo;
import org.dromara.iot.pay.domain.vo.IotPayOrderVo;
import org.dromara.iot.pay.domain.vo.IotPayRefundVo;
import org.dromara.iot.pay.enums.PayChannelEnum;
import org.dromara.iot.pay.enums.PayStatusEnum;
import org.dromara.iot.pay.enums.RefundStatusEnum;
import org.dromara.iot.pay.mapper.IotPayChannelConfigMapper;
import org.dromara.iot.pay.mapper.IotPayOrderMapper;
import org.dromara.iot.pay.mapper.IotPayProductMapper;
import org.dromara.iot.pay.mapper.IotPayRefundMapper;
import org.dromara.iot.pay.service.IotPayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 聚合支付 服务层实现
 * <p>
 * 订单状态机：0待支付 -> 1已支付 -> 3已退款；回调通过状态判定 + Redis 锁保证幂等。
 *
 * @author iot team
 */
@Service
@RequiredArgsConstructor
public class IotPayServiceImpl implements IotPayService {

    /**
     * 订单有效期（分钟）
     */
    private static final long ORDER_EXPIRE_MINUTES = 30;

    /**
     * 渠道配置缓存有效期（分钟）
     */
    private static final long CONFIG_CACHE_MINUTES = 30;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final IotPayOrderMapper orderMapper;

    private final IotPayRefundMapper refundMapper;

    private final IotPayChannelConfigMapper channelConfigMapper;

    private final IotPayProductMapper productMapper;

    private final PayChannelFactory channelFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotPayOrderVo createOrder(IotPayOrderBo bo) {
        requireChannel(bo.getChannel());
        IotPayChannelConfig config = loadChannelConfig(bo.getChannel());

        IotPayOrder order = new IotPayOrder();
        order.setOrderNo(generateNo(IotConstants.PAY_ORDER_NO_PREFIX));
        order.setBizOrderNo(bo.getBizOrderNo());
        order.setChannel(bo.getChannel());
        order.setStatus(PayStatusEnum.WAIT.getCode());
        order.setUserId(LoginHelper.getUserId());
        order.setExpireTime(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_MINUTES));
        order.setRemark(bo.getRemark());

        // 指定商品：以商品名称与售价下单，避免客户端篡改金额；未指定则按自由金额下单
        IotPayProduct product = null;
        if (bo.getProductId() != null) {
            product = productMapper.selectById(bo.getProductId());
            if (product == null) {
                throw new ServiceException("商品不存在：" + bo.getProductId());
            }
            if (!"0".equals(product.getStatus())) {
                throw new ServiceException("商品已下架：" + product.getProductName());
            }
            order.setProductId(product.getProductId());
            order.setSubject(StringUtils.isNotBlank(bo.getSubject()) ? bo.getSubject() : product.getProductName());
            order.setAmount(product.getPrice());
        } else {
            if (bo.getAmount() == null) {
                throw new ServiceException("支付金额不能为空");
            }
            order.setSubject(bo.getSubject());
            order.setAmount(bo.getAmount());
        }
        String currency = bo.getCurrency();
        if (StringUtils.isBlank(currency)) {
            currency = product != null && StringUtils.isNotBlank(product.getCurrency())
                ? product.getCurrency() : "CNY";
        }
        order.setCurrency(currency);
        orderMapper.insert(order);

        IPayChannel channel = channelFactory.getChannel(bo.getChannel());
        PrePayResult prePay = channel.prePay(order, config);
        order.setPrePayId(prePay.getPrePayId());
        order.setCodeUrl(prePay.getCodeUrl());
        orderMapper.updateById(order);

        return orderMapper.selectVoById(order.getOrderId());
    }

    @Override
    public IotPayOrderVo queryByOrderNo(String orderNo) {
        IotPayOrderVo vo = orderMapper.selectVoOne(Wrappers.<IotPayOrder>lambdaQuery()
            .eq(IotPayOrder::getOrderNo, orderNo));
        if (vo == null) {
            throw new ServiceException("支付单不存在：" + orderNo);
        }
        return vo;
    }

    @Override
    public PageResult<IotPayOrderVo> queryPageList(IotPayOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotPayOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getChannel()), IotPayOrder::getChannel, bo.getChannel());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotPayOrder::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), IotPayOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getBizOrderNo()), IotPayOrder::getBizOrderNo, bo.getBizOrderNo());
        lqw.orderByDesc(IotPayOrder::getCreateTime);
        Page<IotPayOrderVo> page = orderMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<IotPayRefundVo> queryRefundPageList(IotPayRefundBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotPayRefund> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), IotPayRefund::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotPayRefund::getStatus, bo.getStatus());
        lqw.orderByDesc(IotPayRefund::getCreateTime);
        Page<IotPayRefundVo> page = refundMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(String channel, Map<String, String> params, String body) {
        IPayChannel payChannel = channelFactory.getChannel(channel);
        IotPayChannelConfig config = loadChannelConfig(channel);
        PayNotifyResult result = payChannel.parseNotify(config, params, body);
        if (!result.isPaid() || StringUtils.isBlank(result.getOrderNo())) {
            return "success";
        }
        String orderNo = result.getOrderNo();
        String lockKey = IotConstants.PAY_NOTIFY_LOCK_PREFIX + channel + ":" + orderNo;
        // 幂等锁：同一订单的回调只处理一次
        if (!RedisUtils.setObjectIfAbsent(lockKey, LocalDateTime.now(),
            Duration.ofSeconds(IotConstants.PAY_NOTIFY_LOCK_TTL_SECONDS))) {
            return "success";
        }
        IotPayOrder order = orderMapper.selectOne(Wrappers.<IotPayOrder>lambdaQuery()
            .eq(IotPayOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new ServiceException("支付单不存在：" + orderNo);
        }
        // 已处理过（重复回调）直接应答成功
        if (!PayStatusEnum.WAIT.getCode().equals(order.getStatus())) {
            return "success";
        }
        order.setStatus(PayStatusEnum.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        order.setNotifyData(result.getRawData());
        orderMapper.updateById(order);
        return "success";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotPayRefundVo refund(IotPayRefundBo bo) {
        IotPayOrder order = orderMapper.selectOne(Wrappers.<IotPayOrder>lambdaQuery()
            .eq(IotPayOrder::getOrderNo, bo.getOrderNo()));
        if (order == null) {
            throw new ServiceException("支付单不存在：" + bo.getOrderNo());
        }
        if (!PayStatusEnum.PAID.getCode().equals(order.getStatus())) {
            throw new ServiceException("仅已支付的订单可以退款");
        }
        if (bo.getRefundAmount().compareTo(order.getAmount()) > 0) {
            throw new ServiceException("退款金额不能大于支付金额");
        }
        IotPayChannelConfig config = loadChannelConfig(order.getChannel());
        IPayChannel channel = channelFactory.getChannel(order.getChannel());

        IotPayRefund refund = new IotPayRefund();
        refund.setRefundNo(generateNo(IotConstants.PAY_REFUND_NO_PREFIX));
        refund.setOrderNo(order.getOrderNo());
        refund.setRefundAmount(bo.getRefundAmount());
        refund.setReason(bo.getReason());
        refund.setStatus(RefundStatusEnum.REFUNDING.getCode());
        refund.setRemark(bo.getRemark());
        refundMapper.insert(refund);

        RefundResult result = channel.refund(order, refund, config);
        refund.setNotifyData(result.getRawData());
        if (result.isSuccess()) {
            refund.setStatus(RefundStatusEnum.SUCCESS.getCode());
            refund.setRefundTime(LocalDateTime.now());
            refundMapper.updateById(refund);
            order.setStatus(PayStatusEnum.REFUNDED.getCode());
            orderMapper.updateById(order);
        } else {
            refund.setStatus(RefundStatusEnum.FAIL.getCode());
            refundMapper.updateById(refund);
        }
        return refundMapper.selectVoById(refund.getRefundId());
    }

    @Override
    public List<IotPayChannelConfigVo> listChannels() {
        return channelConfigMapper.selectVoList(Wrappers.<IotPayChannelConfig>lambdaQuery()
            .orderByAsc(IotPayChannelConfig::getConfigId));
    }

    @Override
    public IotPayChannelConfigVo queryChannelConfigById(Long configId) {
        return channelConfigMapper.selectVoById(configId);
    }

    @Override
    public PageResult<IotPayChannelConfigVo> queryChannelConfigPageList(IotPayChannelConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotPayChannelConfig> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getConfigKey()), IotPayChannelConfig::getConfigKey, bo.getConfigKey());
        lqw.like(StringUtils.isNotBlank(bo.getConfigName()), IotPayChannelConfig::getConfigName, bo.getConfigName());
        lqw.eq(StringUtils.isNotBlank(bo.getChannel()), IotPayChannelConfig::getChannel, bo.getChannel());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), IotPayChannelConfig::getStatus, bo.getStatus());
        lqw.orderByAsc(IotPayChannelConfig::getConfigId);
        Page<IotPayChannelConfigVo> page = channelConfigMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createChannelConfig(IotPayChannelConfigBo bo) {
        requireChannel(bo.getChannel());
        checkConfigKeyUnique(bo.getConfigKey(), null);
        IotPayChannelConfig add = MapstructUtils.convert(bo, IotPayChannelConfig.class);
        boolean flag = channelConfigMapper.insert(add) > 0;
        evictChannelConfigCache(add.getChannel());
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateChannelConfig(IotPayChannelConfigBo bo) {
        IotPayChannelConfig exist = channelConfigMapper.selectById(bo.getConfigId());
        if (exist == null) {
            throw new ServiceException("渠道配置不存在：" + bo.getConfigId());
        }
        requireChannel(bo.getChannel());
        checkConfigKeyUnique(bo.getConfigKey(), bo.getConfigId());
        IotPayChannelConfig update = MapstructUtils.convert(bo, IotPayChannelConfig.class);
        boolean flag = channelConfigMapper.updateById(update) > 0;
        // 渠道编码可能变更，新旧缓存一并失效
        evictChannelConfigCache(exist.getChannel());
        evictChannelConfigCache(update.getChannel());
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteChannelConfig(Collection<Long> configIds) {
        if (CollUtil.isEmpty(configIds)) {
            return false;
        }
        List<IotPayChannelConfig> configs = channelConfigMapper.selectByIds(configIds);
        boolean flag = channelConfigMapper.deleteByIds(configIds) > 0;
        configs.forEach(config -> evictChannelConfigCache(config.getChannel()));
        return flag;
    }

    /**
     * 校验渠道标识合法性
     */
    private void requireChannel(String channel) {
        if (PayChannelEnum.fromCode(channel) == null) {
            throw new ServiceException("不支持的支付渠道：" + channel);
        }
    }

    /**
     * 校验配置键唯一
     */
    private void checkConfigKeyUnique(String configKey, Long configId) {
        boolean exists = channelConfigMapper.exists(Wrappers.<IotPayChannelConfig>lambdaQuery()
            .eq(IotPayChannelConfig::getConfigKey, configKey)
            .ne(configId != null, IotPayChannelConfig::getConfigId, configId));
        if (exists) {
            throw new ServiceException("配置键已存在：" + configKey);
        }
    }

    /**
     * 清除渠道配置缓存
     */
    private void evictChannelConfigCache(String channelCode) {
        if (StringUtils.isNotBlank(channelCode)) {
            RedisUtils.deleteObject(IotConstants.PAY_CHANNEL_CONFIG_CACHE_PREFIX + channelCode);
        }
    }

    /**
     * 加载渠道配置（优先走 Redis 缓存）
     */
    private IotPayChannelConfig loadChannelConfig(String channelCode) {
        String cacheKey = IotConstants.PAY_CHANNEL_CONFIG_CACHE_PREFIX + channelCode;
        IotPayChannelConfig config = RedisUtils.getCacheObject(cacheKey);
        if (config != null) {
            return config;
        }
        config = channelConfigMapper.selectOne(Wrappers.<IotPayChannelConfig>lambdaQuery()
            .eq(IotPayChannelConfig::getChannel, channelCode)
            .eq(IotPayChannelConfig::getStatus, "0")
            .last("limit 1"));
        if (config == null) {
            throw new ServiceException("未配置启用的支付渠道：" + channelCode);
        }
        RedisUtils.setCacheObject(cacheKey, config, Duration.ofMinutes(CONFIG_CACHE_MINUTES));
        return config;
    }

    /**
     * 生成业务单号
     */
    private String generateNo(String prefix) {
        return prefix + NO_FORMATTER.format(LocalDateTime.now()) + RandomUtil.randomNumbers(4);
    }

}
