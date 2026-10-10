package org.dromara.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @author iot team
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
public class IndexController {

    /**
     * 访问首页时返回引导提示。
     *
     * @return 首页提示语
     */
    @GetMapping("/")
    public String index() {
        return StringUtils.format("欢迎使用{}，请通过前端地址访问。", "Snail-IoT");
    }

}
