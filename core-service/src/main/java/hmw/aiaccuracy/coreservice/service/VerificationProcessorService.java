package hmw.aiaccuracy.coreservice.service;

import hmw.aiaccuracy.coreservice.config.KafkaConstants;
import hmw.aiaccuracy.coreservice.domain.VerificationResult;
import hmw.aiaccuracy.coreservice.dto.AIResponse;
import hmw.aiaccuracy.coreservice.dto.FinalVerificationResult;
import hmw.aiaccuracy.coreservice.dto.JobStatus;
import hmw.aiaccuracy.coreservice.dto.JobStatusUpdate;
import hmw.aiaccuracy.coreservice.dto.ScoreResponse;
import hmw.aiaccuracy.coreservice.repository.VerificationResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationProcessorService {

    private final AIAdapterClient aiAdapterClient;
    private final VerificationResultRepository verificationResultRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void processVerification(String jobId, String prompt) {
        log.info("Starting verification for jobId: {} with prompt: {}", jobId, prompt);
        sendMsgToUser(jobId, JobStatus.STARTED, "질문에 대한 답변을 ChatGPT와 Gemini에게 받아오고 있습니다.", null);

        Mono<AIResponse> chatgptMono = getInitialResponseMono("chatgpt", prompt, jobId);
        Mono<AIResponse> geminiMono = getInitialResponseMono("gemini", prompt, jobId);

        Mono.zip(chatgptMono, geminiMono)
                .flatMap(initialAnswers -> {
                    AIResponse chatgptResponse = initialAnswers.getT1();
                    AIResponse geminiResponse = initialAnswers.getT2();

                    if (chatgptResponse.isFallback() || geminiResponse.isFallback()) {
                        int chatGptScore = chatgptResponse.isFallback() ? -1 : 100;
                        int geminiScore = geminiResponse.isFallback() ? -1 : 100;
                        log.warn("One or both AI responses are fallbacks for jobId: {}. Skipping scoring.", jobId);
                        sendMsgToUser(jobId, JobStatus.INCORRECT_VALIDATION, "정확도 분석 하는 데에 실패하였습니다.", null);
                        return Mono.just(new Object[]{chatgptResponse.answer(), geminiResponse.answer(), chatGptScore, geminiScore});
                    }

                    sendMsgToUser(jobId, JobStatus.VALIDATING, "답변을 받아왔고 정확도를 분석 중입니다.", null);

                    Mono<ScoreResponse> scoreFromGemini = aiAdapterClient.getScore("gemini", prompt, chatgptResponse.answer())
                            .doOnNext(res -> sendMsgToUser(jobId, JobStatus.GEMINI_SCORED, "Gemini로부터 ChatGpt의 응답 점수를 받아왔습니다.", res))
                            .onErrorReturn(new ScoreResponse(-1));

                    Mono<ScoreResponse> scoreFromChatgpt = aiAdapterClient.getScore("chatgpt", prompt, geminiResponse.answer())
                            .doOnNext(res -> sendMsgToUser(jobId, JobStatus.CHATGPT_SCORED, "ChatGPT로부터 Gemini의 응답 점수를 받아왔습니다.", res))
                            .onErrorReturn(new ScoreResponse(-1));

                    return Mono.zip(scoreFromGemini, scoreFromChatgpt)
                            .map(scores -> new Object[]{chatgptResponse.answer(), geminiResponse.answer(), scores.getT1().score(), scores.getT2().score()});
                })
                .map(results -> processAndSaveFinalResult(jobId, prompt, results))
                .doOnError(e -> handleProcessingError(jobId, e))
                .subscribe(result -> log.info("Verification process completed for jobId: {}", result.getJobId()),
                        e -> log.error("Error in verification subscription for jobId: {}", jobId, e));
    }

    private void sendMsgToUser(String jobId, JobStatus status, String msg, Object data) {
        kafkaTemplate.send(
                KafkaConstants.TOPIC_WS_MSG_REQUEST,
                new JobStatusUpdate(jobId, status, msg, data)
        );
    }

    private Mono<AIResponse> getInitialResponseMono(String model, String prompt, String jobId) {
        Mono<AIResponse> responseMono = (model.equals("chatgpt"))
                ? aiAdapterClient.getChatGPTResponse(prompt)
                : aiAdapterClient.getGeminiResponse(prompt);

        return responseMono
                .doOnNext(res -> {
                    if (res != null && res.answer() != null) {
                        if (res.isFallback()) {
                            log.warn("{} AI call for jobId {} returned a fallback response.", model, jobId);
                            sendMsgToUser(jobId, JobStatus.FALLBACK_PROVIDED, model + "의 응답을 받아오는 데 실패하였습니다.", res.answer());
                        } else {
                            sendMsgToUser(jobId, JobStatus.FETCHED, model + "의 응답을 받아왔습니다.", null);
                        }
                    } else {
                        log.warn("{} AI call for jobId {} returned null or empty answer in doOnNext. Treating as skipped/failed.", model, jobId);
                        sendMsgToUser(jobId, JobStatus.FAILED, model + "의 응답을 받아오는데 실패하였습니다.", null);
                    }
                })
                .onErrorResume(throwable -> {
                    log.warn("Error during {} AI call for jobId {}: {}", model, jobId, throwable.getMessage());
                    sendMsgToUser(jobId, JobStatus.FAILED, model + "의 응답을 받아오는데 실패하였습니다.", throwable.getMessage());
                    return Mono.just(new AIResponse(null, true));
                });
    }

    private VerificationResult processAndSaveFinalResult(String jobId, String prompt, Object[] results) {
        String chatgptAnswer = (String) results[0];
        String geminiAnswer = (String) results[1];
        int scoreFromGemini = (int) results[2];
        int scoreFromChatgpt = (int) results[3];

        String chosenModel;
        String finalAnswer;
        int finalScore;

        if (scoreFromChatgpt > scoreFromGemini) {
            chosenModel = "Gemini";
            finalAnswer = geminiAnswer;
            finalScore = scoreFromChatgpt;
        } else {
            chosenModel = "ChatGPT";
            finalAnswer = chatgptAnswer;
            finalScore = scoreFromGemini;
        }

        VerificationResult savedResult = verificationResultRepository.save(
                VerificationResult.create(
                        jobId, prompt, chatgptAnswer, geminiAnswer,
                        scoreFromGemini, scoreFromChatgpt, chosenModel, finalAnswer, finalScore)
        );
        log.info("Verification completed and result saved for jobId: {}", savedResult.getJobId());

        FinalVerificationResult finalDto = new FinalVerificationResult(chosenModel, finalAnswer, finalScore);
        sendMsgToUser(jobId, JobStatus.COMPLETED, "검증이 완료되었습니다.", finalDto);

        return savedResult;
    }

    private void handleProcessingError(String jobId, Throwable e) {
        log.error("Overall verification process failed for jobId {}: {}", jobId, e.getMessage());
        sendMsgToUser(jobId, JobStatus.FAILED, "응답을 받아오지 못했습니다.", e.getMessage());
    }

}
