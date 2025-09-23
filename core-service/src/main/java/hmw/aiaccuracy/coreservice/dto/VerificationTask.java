package hmw.aiaccuracy.coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationTask {
    private String jobId;
    private String prompt;
}