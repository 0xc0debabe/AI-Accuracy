package hmw.aiaccuracy.coreservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_results")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationResult {

    @Id
    private String jobId;

    @Column(nullable = false, length = 1000)
    private String prompt;

    @Column(length = 4000)
    private String chatgptAnswer;

    @Column(length = 4000)
    private String geminiAnswer;

    // 점수 필드 추가
    private Integer chatgptScore; // ChatGPT가 Gemini의 답변에 매긴 점수
    private Integer geminiScore;  // Gemini가 ChatGPT의 답변에 매긴 점수

    // 최종 선택된 결과 필드 추가
    @Column(length = 255)
    private String chosenModel;

    @Column(length = 4000)
    private String finalAnswer;

    private Integer finalScore;

    @Column(nullable = false)
    private String finalStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static VerificationResult create(String jobId, String prompt, String chatgptAnswer, String geminiAnswer, int chatgptScore, int geminiScore, String chosenModel, String finalAnswer, int finalScore) {
        return new VerificationResult(
                jobId, prompt, chatgptAnswer, geminiAnswer, chatgptScore, geminiScore,
                chosenModel, finalAnswer, finalScore, "COMPLETED", LocalDateTime.now());
    }
}