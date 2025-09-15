package hmw.aiaccuracy.coreservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_results")
@Data
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

    private boolean isConsistent;

    @Column(length = 2000)
    private String consistencyReasoning;

    @Column(nullable = false)
    private String finalStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
