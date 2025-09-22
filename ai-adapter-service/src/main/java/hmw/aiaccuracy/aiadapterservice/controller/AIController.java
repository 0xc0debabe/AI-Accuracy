package hmw.aiaccuracy.aiadapterservice.controller;

import hmw.aiaccuracy.aiadapterservice.dto.AIRequest;
import hmw.aiaccuracy.aiadapterservice.dto.AIResponse;
import hmw.aiaccuracy.aiadapterservice.dto.ScoreRequest;
import hmw.aiaccuracy.aiadapterservice.dto.ScoreResponse;
import hmw.aiaccuracy.aiadapterservice.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/adapter")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chatgpt")
    public Mono<AIResponse> getChatGPTResponse(@RequestBody AIRequest request) {
        return Mono.fromFuture(aiService.getChatGPTResponse(request.prompt()));
    }

    @PostMapping("/gemini")
    public Mono<AIResponse> getGeminiResponse(@RequestBody AIRequest request) {
        return Mono.fromFuture(aiService.getGeminiResponse(request.prompt()));
    }

    @PostMapping("/score")
    public Mono<ScoreResponse> scoreAnswer(@RequestBody ScoreRequest request) {
        return Mono.fromFuture(aiService.getScore(request));
    }
}