package org.dromara.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.dromara.common.core.domain.R;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring AI + Ollama 本地大模型联通测试
 */
@SaIgnore
@RestController
@RequestMapping("/ai")
public class TestAiController {

    private final ChatClient chatClient;

    public TestAiController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 简单对话测试
     *
     * @param message 用户消息
     */
    @GetMapping("/chat")
    public R<String> chat(@RequestParam("message") String message) {
        String content = chatClient.prompt().user(message).call().content();
        return R.ok(content);
    }
}
