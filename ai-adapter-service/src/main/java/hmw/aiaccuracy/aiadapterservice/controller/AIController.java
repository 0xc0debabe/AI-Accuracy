package hmw.aiaccuracy.aiadapterservice.controller;

import hmw.aiaccuracy.aiadapterservice.dto.AIRequest;
import hmw.aiaccuracy.aiadapterservice.dto.AIResponse;
import hmw.aiaccuracy.aiadapterservice.dto.ValidationRequest;
import hmw.aiaccuracy.aiadapterservice.dto.ValidationResponse;
import hmw.aiaccuracy.aiadapterservice.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/adapter")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chatgpt")
    public CompletableFuture<AIResponse> getChatGPTResponse(@RequestBody AIRequest request) {
        return aiService.getChatGPTResponse(request.prompt());
    }

    @PostMapping("/gemini")
    public CompletableFuture<AIResponse> getGeminiResponse(@RequestBody AIRequest request) {
        return aiService.getGeminiResponse(request.prompt());
    }

    @PostMapping("/validate")
    public CompletableFuture<ValidationResponse> validateAnswers(@RequestBody ValidationRequest request) {
        return aiService.validateAnswers(request);
    }
}
