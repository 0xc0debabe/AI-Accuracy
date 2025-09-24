package hmw.aiaccuracy.coreservice.controller;

import hmw.aiaccuracy.coreservice.config.KafkaConstants;
import hmw.aiaccuracy.coreservice.dto.VerificationRequest;
import hmw.aiaccuracy.coreservice.dto.VerificationTask;
import hmw.aiaccuracy.coreservice.service.VerificationProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationProcessorService verificationProcessorService;

    @PostMapping("/verify")
    public ResponseEntity<String> startVerification(@RequestBody VerificationRequest request) {
        String jobId = UUID.randomUUID().toString();
        log.info("Verification request received for prompt: {} with jobId: {}", request.prompt(), jobId);

        VerificationTask task = new VerificationTask(jobId, request.prompt());
        CompletableFuture.runAsync(() -> verificationProcessorService.processVerification(task));

        return ResponseEntity.ok(jobId);
    }

}