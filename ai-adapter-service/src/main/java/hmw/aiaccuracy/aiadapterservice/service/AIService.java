package hmw.aiaccuracy.aiadapterservice.service;

import hmw.aiaccuracy.aiadapterservice.dto.AIResponse;
import hmw.aiaccuracy.aiadapterservice.dto.ValidationRequest;
import hmw.aiaccuracy.aiadapterservice.dto.ValidationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "chatgpt", fallbackMethod = "fallbackChatGPT")
    public CompletableFuture<AIResponse> getChatGPTResponse(String prompt) {
        // TODO: Implement actual WebClient call to OpenAI API
        log.info("Calling ChatGPT API for prompt: {}", prompt);
        return CompletableFuture.completedFuture(new AIResponse("Response from ChatGPT for: " + prompt));
    }

    @CircuitBreaker(name = "gemini", fallbackMethod = "fallbackGemini")
    public CompletableFuture<AIResponse> getGeminiResponse(String prompt) {
        // TODO: Implement actual WebClient call to Gemini API
        log.info("Calling Gemini API for prompt: {}", prompt);
        return CompletableFuture.completedFuture(new AIResponse("Response from Gemini for: " + prompt));
    }

    public CompletableFuture<ValidationResponse> validateAnswers(ValidationRequest request) {
        // TODO: Implement validation logic, possibly by calling a third AI model
        log.info("Validating answers for prompt: {}", request.prompt());
        boolean isConsistent = request.answer1().contains(request.answer2()) || request.answer2().contains(request.answer1());
        return CompletableFuture.completedFuture(new ValidationResponse(isConsistent, "Validation logic placeholder"));
    }

    private CompletableFuture<AIResponse> fallbackChatGPT(String prompt, Throwable t) {
        log.error("Circuit breaker opened for ChatGPT. Falling back. Error: {}", t.getMessage());
        notifyCircuitBreaker("chatgpt");
        return CompletableFuture.completedFuture(new AIResponse("Fallback response for ChatGPT due to circuit open."));
    }

    private CompletableFuture<AIResponse> fallbackGemini(String prompt, Throwable t) {
        log.error("Circuit breaker opened for Gemini. Falling back. Error: {}", t.getMessage());
        notifyCircuitBreaker("gemini");
        return CompletableFuture.completedFuture(new AIResponse("Fallback response for Gemini due to circuit open."));
    }

    private void notifyCircuitBreaker(String circuitName) {
        // Assuming circuit-breaker-service is registered with Eureka
        String url = "http://circuit-breaker-service/circuits/report";
        webClientBuilder.build().post()
                .uri(url)
                .bodyValue(new FailureReport(circuitName, "ai-adapter-instance-id")) // instanceId can be improved
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to report failure for circuit {}: {}", circuitName, e.getMessage()))
                .subscribe();
    }

    private record FailureReport(String circuitName, String instanceId) {}
}
