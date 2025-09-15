package hmw.aiaccuracy.coreservice.dto;

import java.time.LocalDateTime;

public record VerificationResultDto(
        String jobId,
        String prompt,
        String chatgptAnswer,
        String geminiAnswer,
        boolean isConsistent,
        String consistencyReasoning,
        String finalStatus,
        LocalDateTime createdAt
) {
}
