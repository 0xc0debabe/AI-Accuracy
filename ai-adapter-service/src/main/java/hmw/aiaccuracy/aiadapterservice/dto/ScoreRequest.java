package hmw.aiaccuracy.aiadapterservice.dto;

public record ScoreRequest(String model, String originalPrompt, String answerToScore) {
}
