package org.dromara.iot.sso.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.iot.sso.config.SsoProperties;
import org.dromara.iot.sso.domain.CasTicket;
import org.dromara.iot.sso.domain.SsoTicket;
import org.dromara.iot.sso.domain.vo.SsoLoginVo;
import org.dromara.iot.sso.service.ISsoLoginSupport;
import org.dromara.iot.sso.service.ISsoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CAS 协议端点（双角色）
 * <p>
 * 本平台同时扮演两种角色：
 * <ul>
 *     <li><b>CAS Server（对外服务）</b>：{@code /cas/login}、{@code /cas/serviceValidate}、{@code /cas/logout}，
 *     供第三方系统以标准 CAS 协议接入本平台统一认证中心；</li>
 *     <li><b>CAS Client（对接外部）</b>：{@code /iot/sso/cas-client/login}、{@code /iot/sso/cas-callback}，
 *     支持对接外部 CAS / MaxKey / TOPIAM 等标准 CAS 服务端。</li>
 * </ul>
 * 说明：本平台令牌存放于请求头（非 Cookie），顶层浏览器导航无法携带登录态，
 * 因此 CAS Server 未登录时跳转前端桥接页，由桥接页携带令牌调用
 * {@code POST /iot/sso/cas/ticket} 换取服务票据后再回跳目标系统。
 *
 * @author iot team
 */
@RequiredArgsConstructor
@RestController
public class CasController {

    /**
     * CAS 用户名提取表达式（兼容带命名空间前缀的报文）
     */
    private static final Pattern CAS_USER_PATTERN = Pattern.compile("<cas:user>\\s*(.*?)\\s*</cas:user>", Pattern.DOTALL);

    /**
     * 外部 CAS 校验响应中表示认证成功的标记
     */
    private static final String CAS_SUCCESS_FLAG = "authenticationSuccess";

    private final ISsoService ssoService;

    private final ISsoLoginSupport loginSupport;

    private final SsoProperties ssoProperties;

    // ============================ CAS Server（对外提供） ============================

    /**
     * CAS 登录入口：未登录跳转前端桥接页，已登录直接签发服务票据并回跳目标系统
     *
     * @param service 目标系统回调地址
     */
    @SaIgnore
    @GetMapping("/cas/login")
    public void casLogin(@RequestParam String service) {
        if (StpUtil.isLogin()) {
            CasTicket ticket = ssoService.issueCasTicket(service);
            SaHolder.getResponse().redirect(appendParam(service, "ticket", ticket.getTicket()));
            return;
        }
        String currentUrl = ssoProperties.getServerUrl() + "/cas/login?service=" + URLUtil.encode(service);
        SaHolder.getResponse().redirect(appendParam(ssoProperties.getLoginUrl(), "redirect_uri", currentUrl));
    }

    /**
     * 为已登录用户签发 CAS 服务票据（供前端桥接页换取票据后回跳目标系统）
     *
     * @param service 目标系统回调地址
     * @return 服务票据
     */
    @SaCheckLogin
    @PostMapping("/iot/sso/cas/ticket")
    public R<CasTicket> casTicket(@RequestParam String service) {
        return R.ok(ssoService.issueCasTicket(service));
    }

    /**
     * CAS 2.0/3.0 服务票据校验：校验并一次性消费服务票据
     *
     * @param service  票据绑定的服务地址
     * @param ticket   服务票据
     * @param format   响应格式，取值为 json 时返回 JSON，否则返回 CAS 标准 XML
     * @param response HTTP 响应
     * @throws IOException 响应写出异常
     */
    @SaIgnore
    @GetMapping({"/cas/serviceValidate", "/cas/p3/serviceValidate"})
    public void casServiceValidate(@RequestParam String service,
                                   @RequestParam(required = false) String ticket,
                                   @RequestParam(required = false) String format,
                                   HttpServletResponse response) throws IOException {
        CasTicket casTicket = ssoService.consumeCasTicket(ticket, service);
        if (casTicket == null) {
            writeCasFailure(response, format, "INVALID_TICKET", "票据无效、已过期或服务地址不匹配");
            return;
        }
        writeCasSuccess(response, format, casTicket);
    }

    /**
     * CAS 登出：注销当前登录态并按需回跳目标系统
     *
     * @param service 登出后回跳地址（可选）
     * @param response HTTP 响应
     * @throws IOException 响应写出异常
     */
    @SaIgnore
    @GetMapping("/cas/logout")
    public void casLogout(@RequestParam(required = false) String service,
                          HttpServletResponse response) throws IOException {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        if (StrUtil.isNotBlank(service)) {
            SaHolder.getResponse().redirect(service);
            return;
        }
        writeText(response, HttpServletResponse.SC_OK, "已退出登录");
    }

    // ============================ CAS Client（对接外部 CAS） ============================

    /**
     * 发起外部 CAS 认证：跳转外部 CAS 登录页
     *
     * @param redirectUri 认证成功后的前端回跳地址（可选）
     * @param response    HTTP 响应
     * @throws IOException 响应写出异常
     */
    @SaIgnore
    @GetMapping("/iot/sso/cas-client/login")
    public void casClientLogin(@RequestParam(required = false) String redirectUri,
                               HttpServletResponse response) throws IOException {
        SsoProperties.CasClient client = ssoProperties.getClient();
        if (!client.isEnabled() || StrUtil.isBlank(client.getServerUrl())) {
            writeText(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "未启用外部 CAS 对接");
            return;
        }
        String loginUrl = appendParam(client.getServerUrl() + client.getLoginPath(), "service",
            resolveCallback(client, redirectUri));
        SaHolder.getResponse().redirect(loginUrl);
    }

    /**
     * 外部 CAS 回调：校验外部服务票据、映射本地用户并签发本平台登录态
     *
     * @param ticket      外部 CAS 下发的服务票据
     * @param redirectUri 认证成功后的前端回跳地址（可选）
     * @param response    HTTP 响应
     * @throws IOException          响应写出异常
     * @throws InterruptedException 外部请求被中断
     */
    @SaIgnore
    @GetMapping("/iot/sso/cas-callback")
    public void casCallback(@RequestParam(required = false) String ticket,
                            @RequestParam(required = false) String redirectUri,
                            HttpServletResponse response) throws IOException, InterruptedException {
        SsoProperties.CasClient client = ssoProperties.getClient();
        if (!client.isEnabled() || StrUtil.isBlank(client.getServerUrl())) {
            writeText(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "未启用外部 CAS 对接");
            return;
        }
        String userName = validateExternalCas(client, resolveCallback(client, redirectUri), ticket);
        if (StrUtil.isBlank(userName)) {
            writeText(response, HttpServletResponse.SC_UNAUTHORIZED, "外部 CAS 认证失败");
            return;
        }
        Long userId = loginSupport.findUserIdByUserName(userName);
        if (userId == null) {
            writeText(response, HttpServletResponse.SC_UNAUTHORIZED, "外部账号未映射到本地用户：" + userName);
            return;
        }
        String clientId = ssoProperties.getDefaultClientId();
        if (StrUtil.isNotBlank(redirectUri)) {
            // 前端回跳场景：签发一次性票据，由前端桥接页调用 /iot/sso/exchange 换取登录态
            SsoTicket ssoTicket = ssoService.issueTicketForUser(userId, userName, clientId);
            SaHolder.getResponse().redirect(appendParam(redirectUri, "ticket", ssoTicket.getTicket()));
            return;
        }
        SsoLoginVo loginVo = loginSupport.loginByUserId(userId, clientId);
        loginVo.setUserId(userId);
        loginVo.setUserName(userName);
        writeJson(response, R.ok(loginVo));
    }

    // ============================ 内部辅助 ============================

    /**
     * 计算外部 CAS 注册的服务地址（在回调地址上附加前端回跳参数）
     *
     * @param client      外部 CAS 配置
     * @param redirectUri 前端回跳地址
     * @return 服务地址
     */
    private String resolveCallback(SsoProperties.CasClient client, String redirectUri) {
        return StrUtil.isBlank(redirectUri)
            ? client.getService()
            : appendParam(client.getService(), "redirect_uri", redirectUri);
    }

    /**
     * 调用外部 CAS 校验服务票据并提取用户名
     *
     * @param client  外部 CAS 配置
     * @param service 服务地址
     * @param ticket  服务票据
     * @return 用户名，校验失败时返回 null
     * @throws IOException          外部请求异常
     * @throws InterruptedException 外部请求被中断
     */
    private String validateExternalCas(SsoProperties.CasClient client, String service, String ticket)
        throws IOException, InterruptedException {
        if (StrUtil.isBlank(ticket)) {
            return null;
        }
        String url = appendParam(client.getServerUrl() + client.getValidatePath(), "service", service);
        url = appendParam(url, "ticket", ticket);
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (httpResponse.statusCode() != HttpServletResponse.SC_OK || StrUtil.isBlank(httpResponse.body())) {
            return null;
        }
        return extractCasUser(httpResponse.body());
    }

    /**
     * 从 CAS 校验报文中提取用户名
     *
     * @param body CAS 校验报文
     * @return 用户名，认证失败时返回 null
     */
    private String extractCasUser(String body) {
        if (!body.contains(CAS_SUCCESS_FLAG)) {
            return null;
        }
        Matcher matcher = CAS_USER_PATTERN.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 校验成功时写出 CAS 标准报文
     *
     * @param response HTTP 响应
     * @param format   响应格式
     * @param ticket   服务票据
     * @throws IOException 响应写出异常
     */
    private void writeCasSuccess(HttpServletResponse response, String format, CasTicket ticket) throws IOException {
        if (isJsonFormat(format)) {
            Map<String, Object> attributes = new LinkedHashMap<>();
            attributes.put("userId", String.valueOf(ticket.getUserId()));
            Map<String, Object> success = new LinkedHashMap<>();
            success.put("user", ticket.getUserName());
            success.put("attributes", attributes);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("serviceResponse", Map.of("authenticationSuccess", success));
            writeJson(response, body);
            return;
        }
        String xml = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
            + "<cas:authenticationSuccess>"
            + "<cas:user>" + escapeXml(ticket.getUserName()) + "</cas:user>"
            + "<cas:attributes>"
            + "<cas:userId>" + ticket.getUserId() + "</cas:userId>"
            + "</cas:attributes>"
            + "</cas:authenticationSuccess>"
            + "</cas:serviceResponse>";
        writeXml(response, xml);
    }

    /**
     * 校验失败时写出 CAS 标准报文
     *
     * @param response HTTP 响应
     * @param format   响应格式
     * @param code     失败原因码
     * @param message  失败描述
     * @throws IOException 响应写出异常
     */
    private void writeCasFailure(HttpServletResponse response, String format, String code, String message)
        throws IOException {
        if (isJsonFormat(format)) {
            Map<String, Object> failure = new LinkedHashMap<>();
            failure.put("code", code);
            failure.put("description", message);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("serviceResponse", Map.of("authenticationFailure", failure));
            writeJson(response, body);
            return;
        }
        String xml = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
            + "<cas:authenticationFailure code='" + escapeXml(code) + "'>"
            + escapeXml(message)
            + "</cas:authenticationFailure>"
            + "</cas:serviceResponse>";
        writeXml(response, xml);
    }

    /**
     * 判断是否要求 JSON 格式响应
     *
     * @param format 格式参数
     * @return 是否为 JSON
     */
    private boolean isJsonFormat(String format) {
        return StrUtil.equalsIgnoreCase(format, "json");
    }

    /**
     * 在 URL 上追加查询参数（自动选择 ? 或 &）
     *
     * @param url   原始地址
     * @param name  参数名
     * @param value 参数值
     * @return 追加后的地址
     */
    private String appendParam(String url, String name, String value) {
        if (StrUtil.isBlank(url)) {
            return url;
        }
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + name + "=" + URLUtil.encode(value);
    }

    /**
     * XML 特殊字符转义
     *
     * @param value 原始文本
     * @return 转义后的文本
     */
    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    /**
     * 写出 JSON 响应
     *
     * @param response HTTP 响应
     * @param body     响应体
     * @throws IOException 响应写出异常
     */
    private void writeJson(HttpServletResponse response, Object body) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtils.toJsonString(body));
    }

    /**
     * 写出 XML 响应
     *
     * @param response HTTP 响应
     * @param xml      XML 报文
     * @throws IOException 响应写出异常
     */
    private void writeXml(HttpServletResponse response, String xml) throws IOException {
        response.setContentType("application/xml;charset=UTF-8");
        response.getWriter().write(xml);
    }

    /**
     * 写出纯文本响应
     *
     * @param response HTTP 响应
     * @param status   HTTP 状态码
     * @param text     文本内容
     * @throws IOException 响应写出异常
     */
    private void writeText(HttpServletResponse response, int status, String text) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(text);
    }

}
