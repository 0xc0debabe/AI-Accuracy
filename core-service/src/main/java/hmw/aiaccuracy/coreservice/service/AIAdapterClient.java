package hmw.aiaccuracy.coreservice.service;

import hmw.aiaccuracy.coreservice.dto.AIRequest;
import hmw.aiaccuracy.coreservice.dto.AIResponse;
import hmw.aiaccuracy.coreservice.dto.ValidationRequest;
import hmw.aiaccuracy.coreservice.dto.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AIAdapterClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<AIResponse> getChatGPTResponse(String prompt) {
        return webClientBuilder.build().post()
                .uri("http://ai-adapter-service/adapter/chatgpt")
                .bodyValue(new AIRequest(prompt))
                .retrieve()
                .bodyToMono(AIResponse.class);
    }

    public Mono<AIResponse> getGeminiResponse(String prompt) {
        return webClientBuilder.build().post()
                .uri("http://ai-adapter-service/adapter/gemini")
                .bodyValue(new AIRequest(prompt))
                .retrieve()
                .bodyToMono(AIResponse.class);
    }

    public Mono<ValidationResponse> validateAnswers(ValidationRequest request) {
        return webClientBuilder.build().post()
                .uri("http://ai-adapter-service/adapter/validate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ValidationResponse.class);
    }
}
