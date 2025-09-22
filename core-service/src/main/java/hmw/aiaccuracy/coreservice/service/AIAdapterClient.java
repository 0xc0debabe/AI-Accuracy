package hmw.aiaccuracy.coreservice.service;

import hmw.aiaccuracy.coreservice.dto.AIRequest;
import hmw.aiaccuracy.coreservice.dto.AIResponse;
import hmw.aiaccuracy.coreservice.dto.ScoreRequest;
import hmw.aiaccuracy.coreservice.dto.ScoreResponse;
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

    /**
     * ai-adapter-service에 점수 평가를 요청하는 메소드
     */
    public Mono<ScoreResponse> getScore(String model, String originalPrompt, String answerToScore) {
        ScoreRequest request = new ScoreRequest(model, originalPrompt, answerToScore);
        return webClientBuilder.build().post()
                .uri("http://ai-adapter-service/adapter/score")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ScoreResponse.class);
    }
}