package org.dromara.iot.deploy.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.iot.deploy.domain.IotGithubRepo;
import org.dromara.iot.deploy.domain.IotGithubRepoUser;
import org.dromara.iot.deploy.domain.bo.IotGithubRepoBo;
import org.dromara.iot.deploy.domain.vo.IotGithubRepoVo;
import org.dromara.iot.deploy.mapper.IotGithubRepoMapper;
import org.dromara.iot.deploy.mapper.IotGithubRepoUserMapper;
import org.dromara.iot.deploy.service.IGithubProjectService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GitHub 项目管理 服务层实现
 * <p>
 * - 通过 GitHub API 按页拉取仓库，按 repo_id 幂等写入；
 * - 未配置 token 时走公开接口，仅能拿到公开仓库；
 * - 配置 token 后走 /user/repos，可同步该 token 所属账号的私有仓库；
 * - 授权关系存 iot_github_repo_user，普通账号查询时按 user_id 过滤；
 * - 超级管理员可查看全部项目并维护授权。
 *
 * @author iot team
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GithubProjectServiceImpl implements IGithubProjectService {

    /**
     * GitHub 公开 API 地址
     */
    private static final String GITHUB_API = "https://api.github.com";

    /**
     * 每页拉取数量（GitHub 上限 100）
     */
    private static final int PER_PAGE = 100;

    /**
     * 最多拉取页数，避免异常情况下无限翻页
     */
    private static final int MAX_PAGES = 10;

    private final IotGithubRepoMapper repoMapper;

    private final IotGithubRepoUserMapper repoUserMapper;

    /**
     * 同步的 GitHub 账号
     */
    @Value("${iot.github.owner:ksf127}")
    private String owner;

    /**
     * GitHub Personal Access Token，配置后可同步私有仓库
     */
    @Value("${iot.github.token:}")
    private String token;

    @Override
    public PageResult<IotGithubRepoVo> queryPageList(IotGithubRepoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IotGithubRepo> lqw = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(bo.getKeyword())) {
            lqw.and(w -> w.like(IotGithubRepo::getFullName, bo.getKeyword())
                .or().like(IotGithubRepo::getDescription, bo.getKeyword()));
        }
        lqw.eq(StrUtil.isNotBlank(bo.getLanguage()), IotGithubRepo::getLanguage, bo.getLanguage());
        if (!LoginHelper.isSuperAdmin()) {
            List<Long> repoIds = listAuthorizedRepoIds(LoginHelper.getUserId());
            if (repoIds.isEmpty()) {
                return PageResult.build(List.of(), 0L);
            }
            lqw.in(IotGithubRepo::getRepoId, repoIds);
        }
        lqw.orderByDesc(IotGithubRepo::getPushedAt);
        Page<IotGithubRepoVo> page = repoMapper.selectVoPage(pageQuery.build(), lqw);
        fillUserCount(page.getRecords());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public int syncRepos() {
        String login = StrUtil.trim(owner);
        if (StrUtil.isBlank(login)) {
            throw new ServiceException("未配置同步的 GitHub 账号");
        }
        List<IotGithubRepo> repos = new ArrayList<>();
        LocalDateTime syncTime = LocalDateTime.now();
        for (int page = 1; page <= MAX_PAGES; page++) {
            JSONArray array = fetchPage(login, page);
            if (array == null || array.isEmpty()) {
                break;
            }
            for (Object item : array) {
                repos.add(convert((JSONObject) item, login, syncTime));
            }
            if (array.size() < PER_PAGE) {
                break;
            }
        }
        if (repos.isEmpty()) {
            return 0;
        }
        Map<Long, IotGithubRepo> existing = repoMapper.selectList(Wrappers.<IotGithubRepo>lambdaQuery()
                .in(IotGithubRepo::getRepoId, repos.stream().map(IotGithubRepo::getRepoId).toList()))
            .stream()
            .collect(Collectors.toMap(IotGithubRepo::getRepoId, repo -> repo));
        for (IotGithubRepo repo : repos) {
            if (existing.containsKey(repo.getRepoId())) {
                repoMapper.updateById(repo);
            } else {
                repoMapper.insert(repo);
            }
        }
        long privateCount = repos.stream().filter(repo -> "1".equals(repo.getPrivateFlag())).count();
        log.info("[IoT] GitHub 项目同步完成, owner={}, 数量={}, 私有={}, token模式={}", login, repos.size(), privateCount,
            StrUtil.isNotBlank(token));
        return repos.size();
    }

    @Override
    public List<Long> queryAuthorizedUserIds(Long repoId) {
        return repoUserMapper.selectList(Wrappers.<IotGithubRepoUser>lambdaQuery()
                .eq(IotGithubRepoUser::getRepoId, repoId))
            .stream()
            .map(IotGithubRepoUser::getUserId)
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAuthorizedUsers(Long repoId, List<Long> userIds) {
        if (repoMapper.selectById(repoId) == null) {
            throw new ServiceException("GitHub 项目不存在：" + repoId);
        }
        repoUserMapper.delete(Wrappers.<IotGithubRepoUser>lambdaQuery()
            .eq(IotGithubRepoUser::getRepoId, repoId));
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long userId : new LinkedHashSet<>(userIds)) {
            if (userId == null) {
                continue;
            }
            IotGithubRepoUser relation = new IotGithubRepoUser();
            relation.setRepoId(repoId);
            relation.setUserId(userId);
            relation.setCreateTime(now);
            repoUserMapper.insert(relation);
        }
    }

    /**
     * 拉取一页 GitHub 仓库
     *
     * @param login GitHub 账号
     * @param page  页码
     * @return 仓库 JSON 数组
     */
    private JSONArray fetchPage(String login, int page) {
        String apiToken = StrUtil.trim(token);
        boolean useToken = StrUtil.isNotBlank(apiToken);
        // 配置 token 时走认证接口：affiliation=owner + visibility=all 可同时拿到私有仓库
        String url = useToken
            ? StrUtil.format("{}/user/repos?per_page={}&page={}&sort=updated&affiliation=owner&visibility=all",
                GITHUB_API, PER_PAGE, page)
            : StrUtil.format("{}/users/{}/repos?per_page={}&page={}&sort=updated&type=owner",
                GITHUB_API, login, PER_PAGE, page);
        HttpRequest request = HttpRequest.get(url)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "snail-iot")
            .timeout(20000);
        if (useToken) {
            request.header("Authorization", "Bearer " + apiToken);
        }
        try (HttpResponse response = request.execute()) {
            String body = response.body();
            if (!response.isOk()) {
                if (response.getStatus() == 401 && useToken) {
                    throw new ServiceException("GitHub Token 无效或已过期，请检查 iot.github.token 配置");
                }
                throw new ServiceException("GitHub API 调用失败（HTTP " + response.getStatus() + "）："
                    + StrUtil.maxLength(body, 200));
            }
            return JSONUtil.parseArray(body);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[IoT] 调用 GitHub API 失败, owner={}, page={}", login, page, e);
            throw new ServiceException("调用 GitHub API 失败：" + e.getMessage());
        }
    }

    /**
     * 将 GitHub 仓库 JSON 转换为实体
     *
     * @param obj      GitHub 仓库 JSON
     * @param login    账号
     * @param syncTime 同步时间
     * @return 实体
     */
    private IotGithubRepo convert(JSONObject obj, String login, LocalDateTime syncTime) {
        IotGithubRepo repo = new IotGithubRepo();
        repo.setRepoId(obj.getLong("id"));
        JSONObject ownerObj = obj.getJSONObject("owner");
        repo.setOwnerLogin(ownerObj == null ? login : ownerObj.getStr("login", login));
        repo.setRepoName(obj.getStr("name"));
        repo.setFullName(obj.getStr("full_name"));
        repo.setHtmlUrl(obj.getStr("html_url"));
        repo.setCloneUrl(obj.getStr("clone_url"));
        repo.setDescription(obj.getStr("description"));
        repo.setLanguage(obj.getStr("language"));
        repo.setDefaultBranch(obj.getStr("default_branch"));
        repo.setPrivateFlag(Boolean.TRUE.equals(obj.getBool("private")) ? "1" : "0");
        repo.setArchived(Boolean.TRUE.equals(obj.getBool("archived")) ? "1" : "0");
        repo.setPushedAt(parseTime(obj.getStr("pushed_at")));
        repo.setSyncTime(syncTime);
        return repo;
    }

    /**
     * 解析 GitHub 的 ISO-8601 时间
     *
     * @param value 时间字符串
     * @return 本地时间，解析失败返回 null
     */
    private LocalDateTime parseTime(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询账号已授权的项目ID
     *
     * @param userId 账号ID
     * @return 项目ID列表
     */
    private List<Long> listAuthorizedRepoIds(Long userId) {
        return repoUserMapper.selectList(Wrappers.<IotGithubRepoUser>lambdaQuery()
                .eq(IotGithubRepoUser::getUserId, userId))
            .stream()
            .map(IotGithubRepoUser::getRepoId)
            .toList();
    }

    /**
     * 补充每个项目的授权账号数量
     *
     * @param records 项目列表
     */
    private void fillUserCount(List<IotGithubRepoVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> repoIds = records.stream().map(IotGithubRepoVo::getRepoId).toList();
        Map<Long, Long> counts = repoUserMapper.selectList(Wrappers.<IotGithubRepoUser>lambdaQuery()
                .in(IotGithubRepoUser::getRepoId, repoIds))
            .stream()
            .collect(Collectors.groupingBy(IotGithubRepoUser::getRepoId, Collectors.counting()));
        records.forEach(record -> record.setUserCount(counts.getOrDefault(record.getRepoId(), 0L)));
    }

}
