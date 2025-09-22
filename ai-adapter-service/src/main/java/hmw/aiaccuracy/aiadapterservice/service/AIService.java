package hmw.aiaccuracy.aiadapterservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.aiaccuracy.aiadapterservice.dto.AIResponse;
import hmw.aiaccuracy.aiadapterservice.dto.GeminiGenerateContentRequest;
import hmw.aiaccuracy.aiadapterservice.dto.OpenAIChatRequest;
import hmw.aiaccuracy.aiadapterservice.dto.ScoreRequest;
import hmw.aiaccuracy.aiadapterservice.dto.ScoreResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AIService {

    private final WebClient.Builder nonLoadBalancedWebClientBuilder; // 외부 API 호출용
    private final ObjectMapper objectMapper; // JSON 파싱을 위해 ObjectMapper 주입

    @Value("${ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;

    // 정규 표현식 패턴: 응답 텍스트에서 숫자(0-100)를 찾기 위함
    private static final Pattern SCORE_PATTERN = Pattern.compile("\\b(100|[1-9]?[0-9])\\b");

    // 생성자를 통해 두 WebClient.Builder를 주입받습니다.
    public AIService(@Qualifier("nonLoadBalancedWebClientBuilder") WebClient.Builder nonLoadBalancedWebClientBuilder,
                     ObjectMapper objectMapper) {
        this.nonLoadBalancedWebClientBuilder = nonLoadBalancedWebClientBuilder;
        this.objectMapper = objectMapper;
    }

    @CircuitBreaker(name = "chatgpt", fallbackMethod = "fallbackChatGPT")
    public CompletableFuture<AIResponse> getChatGPTResponse(String prompt) {
        log.info("Calling ChatGPT API for prompt: {}", prompt);
        return callChatGPTApi(prompt);
    }

    @CircuitBreaker(name = "gemini", fallbackMethod = "fallbackGemini")
    public CompletableFuture<AIResponse> getGeminiResponse(String prompt) {
        log.info("Calling Gemini API for prompt: {}", prompt);
        return callGeminiApi(prompt);
    }

    /**
     * AI 모델을 사용하여 다른 AI의 답변을 평가하고 점수를 반환하는 메소드.
     * @param request 점수 평가 요청
     * @return 평가 점수를 담은 CompletableFuture
     */
    public CompletableFuture<ScoreResponse> getScore(ScoreRequest request) {
        log.info("Scoring request for model '{}' on prompt: '{}' with answer: '{}'", request.model(), request.originalPrompt(), request.answerToScore());

        String scoringPrompt = buildScoringPrompt(request.originalPrompt(), request.answerToScore());

        CompletableFuture<AIResponse> aiRawResponseFuture;
        if ("chatgpt".equalsIgnoreCase(request.model())) {
            aiRawResponseFuture = callChatGPTApi(scoringPrompt);
        } else { // gemini
            aiRawResponseFuture = callGeminiApi(scoringPrompt);
        }

        return aiRawResponseFuture
                .thenApply(AIResponse::answer) // AIResponse에서 답변 텍스트만 추출
                .thenApply(this::parseScoreFromAIResponse) // 답변 텍스트에서 점수 파싱
                .thenApply(ScoreResponse::new); // 파싱된 점수로 ScoreResponse 객체 생성
    }

    private CompletableFuture<AIResponse> callChatGPTApi(String prompt) {
        try {
            OpenAIChatRequest chatRequest = new OpenAIChatRequest(
                    "gpt-3.5-turbo",
                    Collections.singletonList(new OpenAIChatRequest.Message("user", prompt))
            );
            String requestBody = objectMapper.writeValueAsString(chatRequest);

            return nonLoadBalancedWebClientBuilder.build().post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(jsonNode -> {
                        String answer = jsonNode.path("choices").get(0).path("message").path("content").asText();
                        return new AIResponse(answer, false);
                    })
                    .toFuture();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e); // JSON 변환 실패 시 에러 반환
        }
    }

    private CompletableFuture<AIResponse> callGeminiApi(String prompt) {
        try {
            GeminiGenerateContentRequest geminiRequest = new GeminiGenerateContentRequest(
                    Collections.singletonList(new GeminiGenerateContentRequest.Content(
                            Collections.singletonList(new GeminiGenerateContentRequest.Part(prompt))
                    ))
            );
            String requestBody = objectMapper.writeValueAsString(geminiRequest);

            return nonLoadBalancedWebClientBuilder.build().post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=" + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(jsonNode -> {
                        String answer = jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                        return new AIResponse(answer, false);
                    })
                    .toFuture();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e); // JSON 변환 실패 시 에러 반환
        }
    }

    private String buildScoringPrompt(String originalPrompt, String answerToScore) {
        return String.format(
                "Original question: '%s'. Given answer: '%s'. On a scale of 0 to 100, how accurate is this answer? Respond with only the integer number.",
                originalPrompt.replace("'", "''"), // 프롬프트 내의 따옴표 이스케이프
                answerToScore.replace("'", "''")
        );
    }

    private int parseScoreFromAIResponse(String aiResponseText) {
        Matcher matcher = SCORE_PATTERN.matcher(aiResponseText);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Could not parse score from AI response: {}. Returning default 50.", aiResponseText);
                return 50; // 파싱 실패 시 기본값
            }
        } else {
            log.warn("No score found in AI response: {}. Returning default 50.", aiResponseText);
            return 50; // 점수 패턴을 찾지 못했을 시 기본값
        }
    }

    // --- Fallback and Circuit Breaker Notification Methods ---

    private CompletableFuture<AIResponse> fallbackChatGPT(String prompt, Throwable t) {
        log.error("Circuit breaker opened for ChatGPT. Falling back. Error: {}", t.getMessage());
        return CompletableFuture.completedFuture(new AIResponse("Fallback response for ChatGPT due to circuit open.", true));
    }

    private CompletableFuture<AIResponse> fallbackGemini(String prompt, Throwable t) {
        log.error("Circuit breaker opened for Gemini. Falling back. Error: {}", t.getMessage());
        return CompletableFuture.completedFuture(new AIResponse("Fallback response for Gemini due to circuit open.", true));
    }

}
