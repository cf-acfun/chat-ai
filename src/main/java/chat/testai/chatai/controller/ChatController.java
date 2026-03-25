package chat.testai.chatai.controller;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    private final MiniMaxChatModel chatModel;

    public ChatController(MiniMaxChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * OpenAI 兼容的流式聊天接口
     * 接收 JSON 请求体，返回 SSE 流
     */
    @PostMapping(value = "/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatCompletions(@RequestBody ChatRequest request) {
        // 获取最后一条用户消息
        String userMessage = request.getMessages().stream()
                .filter(msg -> "user".equals(msg.getRole()))
                .reduce((first, second) -> second)
                .map(MessageDto::getContent)
                .orElse("");

        List<Message> messages = List.of(new UserMessage(userMessage));
        Prompt prompt = new Prompt(messages);

        return chatModel.stream(prompt)
                .map(response -> {
                    // 获取 Generation 对象
                    Generation generation = response.getResult();
                    // 获取 AssistantMessage 并转换为字符串
                    AssistantMessage assistantMessage = generation.getOutput();
                    // AssistantMessage 的 toString() 方法返回消息内容
                    String content = assistantMessage.getText();

                    String escapedContent = escapeJson(content);
                    // OpenAI 兼容的 SSE 格式
                    return "data: {" +
                            "\"id\":\"chatcmpl-xxx\"," +
                            "\"object\":\"chat.completion.chunk\"," +
                            "\"created\":" + System.currentTimeMillis() / 1000 + "," +
                            "\"model\":\"minimax\"," +
                            "\"choices\":[{\"index\":0,\"delta\":{\"content\":\"" + escapedContent + "\"},\"finish_reason\":null}]" +
                            "}\n\n";
                })
                .concatWith(Mono.just("data: [DONE]\n\n"));
    }

    /**
     * 简单的同步接口（用于测试）
     */
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = chatModel.call(message);
        return Map.of("response", response);
    }

    /**
     * 测试接口 - 用于查看响应结构
     */
    @GetMapping("/test")
    public String test() {
        List<Message> messages = List.of(new UserMessage("你好"));
        Prompt prompt = new Prompt(messages);
        ChatResponse response = chatModel.call(prompt);

        Generation generation = response.getResult();
        AssistantMessage assistantMessage = generation.getOutput();

        return "Message type: " + assistantMessage.getClass().getName() +
               "\nMethods: " + String.join(" ",
                   java.util.Arrays.stream(assistantMessage.getClass().getMethods())
                       .map(m -> m.getName())
                       .filter(n -> !n.startsWith("wait") && !n.startsWith("equals") && !n.startsWith("hashCode") && !n.startsWith("toString") && !n.startsWith("getClass") && !n.startsWith("notify"))
                       .distinct()
                       .toArray(String[]::new));
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // 请求体 DTO
    public static class ChatRequest {
        private List<MessageDto> messages;
        private String model;
        private boolean stream;

        public List<MessageDto> getMessages() { return messages; }
        public void setMessages(List<MessageDto> messages) { this.messages = messages; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public boolean isStream() { return stream; }
        public void setStream(boolean stream) { this.stream = stream; }
    }

    public static class MessageDto {
        private String role;
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
