package hmw.aiaccuracy.coreservice.dto;

/**
 * 점수 평가를 요청하기 위한 DTO
 */
public record ScoreRequest(String model, String originalPrompt, String answerToScore) {
}
