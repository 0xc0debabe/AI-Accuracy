package hmw.aiaccuracy.coreservice.controller;

import hmw.aiaccuracy.coreservice.dto.VerificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VerificationController {

    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/verify")
    public ResponseEntity<String> startVerification(@RequestBody VerificationRequest request) {
        String jobId = UUID.randomUUID().toString();
        log.info("Verification request received for prompt: {} with jobId: {}", request.prompt(), jobId);

        rabbitTemplate.convertAndSend("verification-exchange", "verification-routing-key", jobId + ":" + request.prompt());
        return ResponseEntity.ok(jobId);
    }
}