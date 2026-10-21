package org.dromara.iot.pay.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.linpeilie.Converter;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.pay.channel.PayChannelFactory;
import org.dromara.iot.pay.domain.IotPayChannelConfig;
import org.dromara.iot.pay.domain.bo.IotPayChannelConfigBo;
import org.dromara.iot.pay.domain.vo.IotPayChannelConfigVo;
import org.dromara.iot.pay.mapper.IotPayChannelConfigMapper;
import org.dromara.iot.pay.mapper.IotPayOrderMapper;
import org.dromara.iot.pay.mapper.IotPayRefundMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 渠道配置相关服务层单元测试
 * <p>
 * 覆盖分页查询、状态/唯一校验、缓存失效等核心分支，Mapper 与静态工具均以 Mock 隔离。
 *
 * @author iot team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IotPayServiceImpl 渠道配置")
class IotPayServiceImplChannelConfigTest {

    @Mock
    private IotPayOrderMapper orderMapper;

    @Mock
    private IotPayRefundMapper refundMapper;

    @Mock
    private IotPayChannelConfigMapper channelConfigMapper;

    @Mock
    private PayChannelFactory channelFactory;

    @InjectMocks
    private IotPayServiceImpl payService;

    /**
     * 注入仅含 mock 依赖的内存 Spring 容器，满足 MapstructUtils / RedisUtils 静态初始化契约，
     * 避免因缺少 Spring 环境导致类初始化失败。
     */
    @BeforeAll
    static void initializeSpringContext() {
        StaticApplicationContext context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("converter", mock(Converter.class));
        context.getBeanFactory().registerSingleton("redissonClient", mock(RedissonClient.class));
        context.refresh();
        new SpringUtil().setApplicationContext(context);
    }

    @Test
    @DisplayName("分页查询：透传分页参数并回填记录与总数")
    void queryChannelConfigPageList_shouldReturnRowsAndTotal() {
        IotPayChannelConfigBo bo = new IotPayChannelConfigBo();
        bo.setConfigKey("wx");
        bo.setConfigName("微信");
        bo.setChannel("wx_native");
        bo.setStatus("0");

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(2);
        pageQuery.setPageSize(20);

        IotPayChannelConfigVo vo = new IotPayChannelConfigVo();
        vo.setConfigId(1900000000000004002L);
        vo.setConfigKey("wx_native_default");

        Page<IotPayChannelConfigVo> page = new Page<>(2, 20);
        page.setRecords(List.of(vo));
        page.setTotal(21L);
        doReturn(page).when(channelConfigMapper).selectVoPage(
            ArgumentMatchers.<IPage<IotPayChannelConfig>>any(),
            ArgumentMatchers.<Wrapper<IotPayChannelConfig>>any());

        PageResult<IotPayChannelConfigVo> result = payService.queryChannelConfigPageList(bo, pageQuery);

        assertThat(result.getTotal()).isEqualTo(21L);
        assertThat(result.getRows()).containsExactly(vo);

        ArgumentCaptor<IPage<IotPayChannelConfig>> pageCaptor = ArgumentCaptor.forClass(IPage.class);
        verify(channelConfigMapper).selectVoPage(pageCaptor.capture(),
            ArgumentMatchers.<Wrapper<IotPayChannelConfig>>any());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("查询全部渠道：返回 T 列表")
    void listChannels_shouldReturnAll() {
        IotPayChannelConfigVo vo = new IotPayChannelConfigVo();
        vo.setConfigId(1L);
        when(channelConfigMapper.selectVoList(any())).thenReturn(List.of(vo));

        assertThat(payService.listChannels()).containsExactly(vo);
    }

    @Test
    @DisplayName("新增：渠道不受支持时直接拒绝")
    void createChannelConfig_shouldRejectUnsupportedChannel() {
        IotPayChannelConfigBo bo = new IotPayChannelConfigBo();
        bo.setChannel("unknown");
        bo.setConfigKey("k");

        assertThatThrownBy(() -> payService.createChannelConfig(bo))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("不支持的支付渠道");
        verify(channelConfigMapper, never()).insert(any(IotPayChannelConfig.class));
    }

    @Test
    @DisplayName("新增：配置键重复时拒绝")
    void createChannelConfig_shouldRejectDuplicateConfigKey() {
        IotPayChannelConfigBo bo = new IotPayChannelConfigBo();
        bo.setChannel("mock");
        bo.setConfigKey("dup_key");
        when(channelConfigMapper.exists(any())).thenReturn(true);

        assertThatThrownBy(() -> payService.createChannelConfig(bo))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("配置键已存在");
        verify(channelConfigMapper, never()).insert(any(IotPayChannelConfig.class));
    }

    @Test
    @DisplayName("新增：写入成功并清理渠道缓存")
    void createChannelConfig_shouldInsertAndEvictCache() {
        IotPayChannelConfigBo bo = new IotPayChannelConfigBo();
        bo.setChannel("mock");
        bo.setConfigKey("new_key");
        when(channelConfigMapper.exists(any())).thenReturn(false);

        IotPayChannelConfig entity = new IotPayChannelConfig();
        entity.setChannel("mock");
        entity.setConfigKey("new_key");

        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class);
             MockedStatic<RedisUtils> redis = mockStatic(RedisUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(bo, IotPayChannelConfig.class)).thenReturn(entity);
            when(channelConfigMapper.insert(entity)).thenReturn(1);

            assertThat(payService.createChannelConfig(bo)).isTrue();

            verify(channelConfigMapper).insert(entity);
            redis.verify(() -> RedisUtils.deleteObject(IotConstants.PAY_CHANNEL_CONFIG_CACHE_PREFIX + "mock"));
        }
    }

    @Test
    @DisplayName("修改：配置不存在时拒绝")
    void updateChannelConfig_shouldRejectMissingConfig() {
        IotPayChannelConfigBo bo = new IotPayChannelConfigBo();
        bo.setConfigId(99L);
        bo.setChannel("mock");
        when(channelConfigMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> payService.updateChannelConfig(bo))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("渠道配置不存在");
        verify(channelConfigMapper, never()).updateById(any(IotPayChannelConfig.class));
    }

    @Test
    @DisplayName("修改：渠道变更时新旧缓存一并清理")
    void updateChannelConfig_shouldEvictOldAndNewChannelCache() {
        IotPayChannelConfigBo bo = new IotPayChannelConfigBo();
        bo.setConfigId(1L);
        bo.setChannel("wx_native");
        bo.setConfigKey("k");

        IotPayChannelConfig exist = new IotPayChannelConfig();
        exist.setConfigId(1L);
        exist.setChannel("mock");
        when(channelConfigMapper.selectById(1L)).thenReturn(exist);

        IotPayChannelConfig update = new IotPayChannelConfig();
        update.setConfigId(1L);
        update.setChannel("wx_native");

        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class);
             MockedStatic<RedisUtils> redis = mockStatic(RedisUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(bo, IotPayChannelConfig.class)).thenReturn(update);
            when(channelConfigMapper.updateById(update)).thenReturn(1);

            assertThat(payService.updateChannelConfig(bo)).isTrue();

            redis.verify(() -> RedisUtils.deleteObject(IotConstants.PAY_CHANNEL_CONFIG_CACHE_PREFIX + "mock"));
            redis.verify(() -> RedisUtils.deleteObject(IotConstants.PAY_CHANNEL_CONFIG_CACHE_PREFIX + "wx_native"));
        }
    }

    @Test
    @DisplayName("删除：空集合直接返回 false，不触碰数据库")
    void deleteChannelConfig_shouldReturnFalseWhenEmpty() {
        assertThat(payService.deleteChannelConfig(List.of())).isFalse();
        verifyNoInteractions(channelConfigMapper);
    }

    @Test
    @DisplayName("删除：逐条清理对应渠道缓存")
    void deleteChannelConfig_shouldEvictEachChannelCache() {
        IotPayChannelConfig first = new IotPayChannelConfig();
        first.setChannel("mock");
        IotPayChannelConfig second = new IotPayChannelConfig();
        second.setChannel("ali_qr");
        when(channelConfigMapper.selectByIds(List.of(1L, 2L))).thenReturn(List.of(first, second));
        when(channelConfigMapper.deleteByIds(List.of(1L, 2L))).thenReturn(2);

        try (MockedStatic<RedisUtils> redis = mockStatic(RedisUtils.class)) {
            assertThat(payService.deleteChannelConfig(List.of(1L, 2L))).isTrue();

            redis.verify(() -> RedisUtils.deleteObject(IotConstants.PAY_CHANNEL_CONFIG_CACHE_PREFIX + "mock"));
            redis.verify(() -> RedisUtils.deleteObject(IotConstants.PAY_CHANNEL_CONFIG_CACHE_PREFIX + "ali_qr"));
        }
    }

    @Test
    @DisplayName("详情：按 ID 查询")
    void queryChannelConfigById_shouldDelegateToMapper() {
        IotPayChannelConfigVo vo = new IotPayChannelConfigVo();
        vo.setConfigId(1L);
        when(channelConfigMapper.selectVoById(anyLong())).thenReturn(vo);

        assertThat(payService.queryChannelConfigById(1L)).isEqualTo(vo);
    }

}
