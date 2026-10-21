package org.dromara.iot.pay.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.pay.domain.bo.IotPayChannelConfigBo;
import org.dromara.iot.pay.domain.bo.IotPayOrderBo;
import org.dromara.iot.pay.domain.bo.IotPayRefundBo;
import org.dromara.iot.pay.domain.vo.IotPayChannelConfigVo;
import org.dromara.iot.pay.domain.vo.IotPayOrderVo;
import org.dromara.iot.pay.domain.vo.IotPayRefundVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 聚合支付 服务层
 *
 * @author iot team
 */
public interface IotPayService {

    /**
     * 统一下单（预下单，返回二维码）
     */
    IotPayOrderVo createOrder(IotPayOrderBo bo);

    /**
     * 按支付单号查单
     */
    IotPayOrderVo queryByOrderNo(String orderNo);

    /**
     * 分页查询支付单
     */
    PageResult<IotPayOrderVo> queryPageList(IotPayOrderBo bo, PageQuery pageQuery);

    /**
     * 分页查询退款单
     */
    PageResult<IotPayRefundVo> queryRefundPageList(IotPayRefundBo bo, PageQuery pageQuery);

    /**
     * 处理渠道异步回调，返回给渠道的应答内容
     *
     * @param channel 渠道标识
     * @param params  回调表单参数
     * @param body    回调原始报文
     */
    String handleNotify(String channel, Map<String, String> params, String body);

    /**
     * 发起退款
     */
    IotPayRefundVo refund(IotPayRefundBo bo);

    /**
     * 查询全部渠道配置
     */
    List<IotPayChannelConfigVo> listChannels();

    /**
     * 分页查询渠道配置
     */
    PageResult<IotPayChannelConfigVo> queryChannelConfigPageList(IotPayChannelConfigBo bo, PageQuery pageQuery);

    /**
     * 查询渠道配置详情
     */
    IotPayChannelConfigVo queryChannelConfigById(Long configId);

    /**
     * 新增渠道配置
     */
    Boolean createChannelConfig(IotPayChannelConfigBo bo);

    /**
     * 修改渠道配置
     */
    Boolean updateChannelConfig(IotPayChannelConfigBo bo);

    /**
     * 批量删除渠道配置
     */
    Boolean deleteChannelConfig(Collection<Long> configIds);

}
