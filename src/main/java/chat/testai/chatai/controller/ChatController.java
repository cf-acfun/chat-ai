package chat.testai.chatai.controller;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
            @RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        String response = chatModel.call(message);
        return Map.of("generation", response);
    }

//    /**
//     * 同步调用 - 带系统提示词
//     */
//    @PostMapping("/chat")
//    public ChatResponse chat(@RequestBody ChatRequest request) {
//        List<Message> messages = List.of(
//                new SystemMessage("你是一个专业的Java技术专家，回答简洁准确。"),
//                new UserMessage(request.getMessage())
//        );
//
//        Prompt prompt = new Prompt(messages);
//        return chatModel.call(prompt);
//    }

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
