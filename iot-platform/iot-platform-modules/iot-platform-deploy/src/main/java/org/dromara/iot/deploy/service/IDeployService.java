package org.dromara.iot.deploy.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.iot.deploy.domain.bo.IotDeployAppBo;
import org.dromara.iot.deploy.domain.bo.IotDeployEnvBo;
import org.dromara.iot.deploy.domain.bo.IotDeployTaskBo;
import org.dromara.iot.deploy.domain.vo.IotDeployAppVo;
import org.dromara.iot.deploy.domain.vo.IotDeployEnvVo;
import org.dromara.iot.deploy.domain.vo.IotDeployTaskVo;

import java.util.Collection;
import java.util.List;

/**
 * 自动化部署 服务层
 * <p>
 * 负责应用/环境登记管理、应用制品发布编排、部署进度与日志查询。
 *
 * @author iot team
 */
public interface IDeployService {

    /**
     * 分页查询部署任务
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 部署任务分页结果
     */
    PageResult<IotDeployTaskVo> queryPageList(IotDeployTaskBo bo, PageQuery pageQuery);

    /**
     * 触发一次部署发布
     *
     * @param bo 发布参数（appCode / version / targetEnv）
     * @return 部署任务ID
     */
    Long release(IotDeployTaskBo bo);

    /**
     * 登记一条构建任务（由页面触发 Jenkins 构建时调用）
     *
     * @param bo 构建参数（appCode / version / targetEnv）
     * @return 任务ID
     */
    Long createBuildTask(IotDeployTaskBo bo);

    /**
     * 回写构建任务结果
     *
     * @param taskId      任务ID
     * @param status      结果状态（2成功 3失败）
     * @param buildNumber Jenkins 构建号（可空）
     */
    void finishBuildTask(Long taskId, String status, Integer buildNumber);

    /**
     * 查询部署日志
     *
     * @param taskId 部署任务ID
     * @return 部署日志文本
     */
    String queryLog(Long taskId);

    /**
     * 查询已登记的应用列表（用于发布时选择应用）
     *
     * @return 应用列表
     */
    List<IotDeployAppVo> listApps();

    /**
     * 查询已登记的环境列表（用于发布时选择目标环境）
     *
     * @return 环境列表
     */
    List<IotDeployEnvVo> listEnvs();

    /**
     * 分页查询应用
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 应用分页结果
     */
    PageResult<IotDeployAppVo> queryAppPageList(IotDeployAppBo bo, PageQuery pageQuery);

    /**
     * 查询应用详情
     *
     * @param appId 应用ID
     * @return 应用视图对象
     */
    IotDeployAppVo queryAppById(Long appId);

    /**
     * 新增应用
     *
     * @param bo 应用业务对象
     * @return 是否成功
     */
    Boolean insertAppByBo(IotDeployAppBo bo);

    /**
     * 修改应用
     *
     * @param bo 应用业务对象
     * @return 是否成功
     */
    Boolean updateAppByBo(IotDeployAppBo bo);

    /**
     * 批量删除应用
     *
     * @param ids 应用ID集合
     * @return 是否成功
     */
    Boolean deleteAppByIds(Collection<Long> ids);

    /**
     * 分页查询环境
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 环境分页结果
     */
    PageResult<IotDeployEnvVo> queryEnvPageList(IotDeployEnvBo bo, PageQuery pageQuery);

    /**
     * 查询环境详情
     *
     * @param envId 环境ID
     * @return 环境视图对象
     */
    IotDeployEnvVo queryEnvById(Long envId);

    /**
     * 新增环境
     *
     * @param bo 环境业务对象
     * @return 是否成功
     */
    Boolean insertEnvByBo(IotDeployEnvBo bo);

    /**
     * 修改环境
     *
     * @param bo 环境业务对象
     * @return 是否成功
     */
    Boolean updateEnvByBo(IotDeployEnvBo bo);

    /**
     * 批量删除环境
     *
     * @param ids 环境ID集合
     * @return 是否成功
     */
    Boolean deleteEnvByIds(Collection<Long> ids);

}
