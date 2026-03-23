package chat.testai.chatai.controller;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final MiniMaxChatModel chatModel;

    public ChatController(MiniMaxChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 同步调用 - 简单问答
     */
    @GetMapping("/generate")
    public Map<String, String> generate(
            @RequestParam(value = "message") String message) {
        String response = chatModel.call(message);
        return Map.of("generation", response);
    }

    @GetMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> fluxChat(@RequestParam(value = "message") String message) {
        List<Message> messages = List.of(
                new SystemMessage("你是一个专业的软件测试专家，请回答用户的问题"),
                new UserMessage(message)
        );
        Prompt prompt = new Prompt(messages);
        return chatModel.stream(prompt)
                .map(response -> response.getResult().getOutput().getText());
    }

    /**
     * 流式调用 - Server-Sent Events
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> stream(
            @RequestParam(value = "message", defaultValue = "写一首关于春天的诗") String message) {

        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }
}
