package org.dromara.iot.cloud.provider.support;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * 云平台 HTTP 调用器
 * <p>
 * 基于 JDK 内置 {@link HttpClient}，无需额外依赖；统一异常包装为 {@link ServiceException}。
 *
 * @author iot team
 */
@Component
public class CloudHttpClient {

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .version(HttpClient.Version.HTTP_1_1)
        .build();

    /**
     * GET 请求
     */
    public String get(String url, Map<String, String> headers) {
        return send("GET", url, headers, null);
    }

    /**
     * POST 请求
     */
    public String post(String url, Map<String, String> headers, String body) {
        return send("POST", url, headers, body);
    }

    /**
     * PATCH 请求
     */
    public String patch(String url, Map<String, String> headers, String body) {
        return send("PATCH", url, headers, body);
    }

    /**
     * 统一发送请求并做异常包装
     */
    private String send(String method, String url, Map<String, String> headers, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15));
            if (headers != null) {
                headers.forEach(builder::header);
            }
            if (body == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            }
            HttpResponse<String> response = client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new ServiceException(StringUtils.format("云平台接口调用失败，HTTP {}：{}",
                    response.statusCode(), response.body()));
            }
            return response.body();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("云平台接口调用异常：" + e.getMessage());
        }
    }

}
