package org.dromara.iot.sso.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.sso.domain.IotOauth2Client;
import org.dromara.iot.sso.domain.bo.IotOauth2ClientBo;
import org.dromara.iot.sso.domain.vo.IotOauth2ClientVo;
import org.dromara.iot.sso.mapper.IotOauth2ClientMapper;
import org.dromara.iot.sso.service.IIotOauth2ClientService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 客户端注册 服务层实现
 * <p>
 * 客户端注册信息会被 OAuth2 服务端在每次授权/换令牌时读取，故按 clientId 做 Redis 缓存，
 * 写操作时主动失效，避免热点查询频繁穿透数据库。
 *
 * @author iot team
 */
@RequiredArgsConstructor
@Service
public class IotOauth2ClientServiceImpl implements IIotOauth2ClientService {

    /**
     * 客户端注册信息缓存有效期
     */
    private static final Duration CLIENT_CACHE_TTL = Duration.ofHours(2);

    private final IotOauth2ClientMapper baseMapper;

    /**
     * 查询客户端详情
     *
     * @param id 主键ID
     * @return 客户端视图对象
     */
    @Override
    public IotOauth2ClientVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询客户端列表
     *
     * @param bo 查询条件
     * @return 客户端列表
     */
    @Override
    public List<IotOauth2ClientVo> queryList(IotOauth2ClientBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    /**
     * 按客户端标识查询（带缓存）
     *
     * @param clientId 客户端标识
     * @return 客户端视图对象，不存在时返回 null
     */
    @Override
    public IotOauth2ClientVo queryByClientId(String clientId) {
        if (StrUtil.isBlank(clientId)) {
            return null;
        }
        String cacheKey = IotConstants.OAUTH2_CLIENT_CACHE_PREFIX + clientId;
        IotOauth2ClientVo cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }
        IotOauth2ClientVo vo = baseMapper.lambda()
            .eq(IotOauth2Client::getClientId, clientId)
            .voOne();
        if (vo != null) {
            RedisUtils.setCacheObject(cacheKey, vo, CLIENT_CACHE_TTL);
        }
        return vo;
    }

    /**
     * 新增客户端
     *
     * @param bo 客户端业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(IotOauth2ClientBo bo) {
        checkClientIdUnique(bo.getId(), bo.getClientId());
        IotOauth2Client add = MapstructUtils.convert(bo, IotOauth2Client.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            evictCache(add.getClientId());
        }
        return flag;
    }

    /**
     * 修改客户端
     *
     * @param bo 客户端业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(IotOauth2ClientBo bo) {
        checkClientIdUnique(bo.getId(), bo.getClientId());
        IotOauth2Client old = baseMapper.selectById(bo.getId());
        IotOauth2Client update = MapstructUtils.convert(bo, IotOauth2Client.class);
        boolean flag = baseMapper.updateById(update) > 0;
        if (flag) {
            if (old != null) {
                evictCache(old.getClientId());
            }
            evictCache(update.getClientId());
        }
        return flag;
    }

    /**
     * 批量删除客户端
     *
     * @param ids 主键ID集合
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Boolean.FALSE;
        }
        List<IotOauth2Client> clients = baseMapper.selectByIds(ids);
        boolean flag = baseMapper.deleteByIds(ids) > 0;
        if (flag && CollUtil.isNotEmpty(clients)) {
            clients.forEach(client -> evictCache(client.getClientId()));
        }
        return flag;
    }

    /**
     * 校验客户端标识唯一
     *
     * @param id       主键ID（修改时排除自身）
     * @param clientId 客户端标识
     */
    private void checkClientIdUnique(Long id, String clientId) {
        boolean exists = baseMapper.exists(Wrappers.<IotOauth2Client>lambdaQuery()
            .eq(IotOauth2Client::getClientId, clientId)
            .ne(id != null, IotOauth2Client::getId, id));
        if (exists) {
            throw new ServiceException("客户端标识已存在：" + clientId);
        }
    }

    /**
     * 失效客户端注册信息缓存
     *
     * @param clientId 客户端标识
     */
    private void evictCache(String clientId) {
        if (StrUtil.isNotBlank(clientId)) {
            RedisUtils.deleteObject(IotConstants.OAUTH2_CLIENT_CACHE_PREFIX + clientId);
        }
    }

    /**
     * 构建客户端查询条件
     *
     * @param bo 查询条件
     * @return Lambda 查询条件
     */
    private LambdaQueryWrapper<IotOauth2Client> buildQueryWrapper(IotOauth2ClientBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IotOauth2Client> lqw = Wrappers.lambdaQuery();
        lqw.like(StrUtil.isNotBlank(bo.getClientId()), IotOauth2Client::getClientId, bo.getClientId());
        lqw.like(StrUtil.isNotBlank(bo.getClientName()), IotOauth2Client::getClientName, bo.getClientName());
        lqw.eq(StrUtil.isNotBlank(bo.getDeviceType()), IotOauth2Client::getDeviceType, bo.getDeviceType());
        lqw.eq(StrUtil.isNotBlank(bo.getStatus()), IotOauth2Client::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            IotOauth2Client::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(IotOauth2Client::getCreateTime);
        return lqw;
    }

}
