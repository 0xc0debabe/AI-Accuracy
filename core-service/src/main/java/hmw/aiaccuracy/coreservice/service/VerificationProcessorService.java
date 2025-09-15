package hmw.aiaccuracy.coreservice.service;

import hmw.aiaccuracy.coreservice.domain.CircuitStatus;
import hmw.aiaccuracy.coreservice.domain.VerificationResult;
import hmw.aiaccuracy.coreservice.dto.AIRequest;
import hmw.aiaccuracy.coreservice.dto.AIResponse;
import hmw.aiaccuracy.coreservice.dto.JobStatusUpdate;
import hmw.aiaccuracy.coreservice.dto.ValidationRequest;
import hmw.aiaccuracy.coreservice.dto.ValidationResponse;
import hmw.aiaccuracy.coreservice.repository.VerificationResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationProcessorService {

    private final LocalCircuitManagerService localCircuitManagerService;
    private final AIAdapterClient aiAdapterClient;
    private final RedisPublisher redisPublisher;
    private final VerificationResultRepository verificationResultRepository;

    public void processVerification(String jobId, String prompt) {
        log.info("Starting verification for jobId: {} with prompt: {}", jobId, prompt);
        redisPublisher.publish(new JobStatusUpdate(jobId, "STARTED", "Verification started.", null));

        String chatgptAnswer = null;
        String geminiAnswer = null;
        boolean isConsistent = false;
        String consistencyReasoning = "";
        String finalStatus = "FAILED";

        Mono<AIResponse> chatgptMono = Mono.empty();
        Mono<AIResponse> geminiMono = Mono.empty();

        // Check circuit states and prepare AI calls
        CircuitStatus chatgptCircuit = localCircuitManagerService.getCircuitStatus("chatgpt");
        CircuitStatus geminiCircuit = localCircuitManagerService.getCircuitStatus("gemini");

        if (chatgptCircuit == CircuitStatus.CLOSED || chatgptCircuit == CircuitStatus.HALF_OPEN) {
            chatgptMono = aiAdapterClient.getChatGPTResponse(prompt)
                    .doOnNext(res -> redisPublisher.publish(new JobStatusUpdate(jobId, "CHATGPT_FETCHED", "ChatGPT response received.", res.answer())))
                    .doOnError(e -> log.error("Error fetching ChatGPT response for jobId {}: {}", jobId, e.getMessage()));
        } else {
            log.warn("ChatGPT circuit is OPEN for jobId: {}. Skipping call.", jobId);
            redisPublisher.publish(new JobStatusUpdate(jobId, "CHATGPT_SKIPPED", "ChatGPT call skipped due to open circuit.", null));
        }

        if (geminiCircuit == CircuitStatus.CLOSED || geminiCircuit == CircuitStatus.HALF_OPEN) {
            geminiMono = aiAdapterClient.getGeminiResponse(prompt)
                    .doOnNext(res -> redisPublisher.publish(new JobStatusUpdate(jobId, "GEMINI_FETCHED", "Gemini response received.", res.answer())))
                    .doOnError(e -> log.error("Error fetching Gemini response for jobId {}: {}", jobId, e.getMessage()));
        } else {
            log.warn("Gemini circuit is OPEN for jobId: {}. Skipping call.", jobId);
            redisPublisher.publish(new JobStatusUpdate(jobId, "GEMINI_SKIPPED", "Gemini call skipped due to open circuit.", null));
        }

        // Execute AI calls and process results
        Mono.zip(chatgptMono.defaultIfEmpty(new AIResponse(null)), geminiMono.defaultIfEmpty(new AIResponse(null)))
                .flatMap(tuple -> {
                    AIResponse chatgptRes = tuple.getT1();
                    AIResponse geminiRes = tuple.getT2();

                    String currentChatgptAnswer = chatgptRes != null ? chatgptRes.answer() : null;
                    String currentGeminiAnswer = geminiRes != null ? geminiRes.answer() : null;

                    if (currentChatgptAnswer != null && currentGeminiAnswer != null) {
                        redisPublisher.publish(new JobStatusUpdate(jobId, "VALIDATING", "Performing cross-validation.", null));
                        return aiAdapterClient.validateAnswers(new ValidationRequest(prompt, currentChatgptAnswer, currentGeminiAnswer))
                                .map(valRes -> new Object[] {currentChatgptAnswer, currentGeminiAnswer, valRes});
                    } else if (currentChatgptAnswer != null || currentGeminiAnswer != null) {
                        // Graceful degradation: one AI is available
                        log.info("Partial success for jobId {}: only one AI responded.", jobId);
                        redisPublisher.publish(new JobStatusUpdate(jobId, "PARTIAL_SUCCESS", "Only one AI responded, skipping cross-validation.", null));
                        return Mono.just(new Object[] {currentChatgptAnswer, currentGeminiAnswer, new ValidationResponse(false, "Only one AI responded.")});
                    } else {
                        // Both failed or skipped
                        log.error("Both AI calls failed or skipped for jobId: {}.", jobId);
                        redisPublisher.publish(new JobStatusUpdate(jobId, "BOTH_FAILED", "Both AI calls failed or skipped.", null));
                        return Mono.just(new Object[] {null, null, new ValidationResponse(false, "Both AI calls failed or skipped.")});
                    }
                })
                .doOnNext(results -> {
                    String finalChatgptAnswer = (String) results[0];
                    String finalGeminiAnswer = (String) results[1];
                    ValidationResponse validationResponse = (ValidationResponse) results[2];

                    VerificationResult verificationResult = VerificationResult.builder()
                            .jobId(jobId)
                            .prompt(prompt)
                            .chatgptAnswer(finalChatgptAnswer)
                            .geminiAnswer(finalGeminiAnswer)
                            .isConsistent(validationResponse.isConsistent())
                            .consistencyReasoning(validationResponse.reasoning())
                            .finalStatus(validationResponse.isConsistent() ? "COMPLETED_CONSISTENT" : "COMPLETED_INCONSISTENT")
                            .createdAt(LocalDateTime.now())
                            .build();
                    verificationResultRepository.save(verificationResult);
                    redisPublisher.publish(new JobStatusUpdate(jobId, verificationResult.getFinalStatus(), "Verification completed.", verificationResult));
                    log.info("Verification completed and result saved for jobId: {}", jobId);
                })
                .doOnError(e -> {
                    log.error("Overall verification process failed for jobId {}: {}", jobId, e.getMessage());
                    redisPublisher.publish(new JobStatusUpdate(jobId, "FAILED", "Overall verification process failed.", e.getMessage()));
                    // Save a failed result
                    verificationResultRepository.save(VerificationResult.builder()
                            .jobId(jobId)
                            .prompt(prompt)
                            .finalStatus("FAILED")
                            .createdAt(LocalDateTime.now())
                            .build());
                })
                .subscribe();
    }
}
