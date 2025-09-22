package hmw.aiaccuracy.coreservice.controller;

import hmw.aiaccuracy.coreservice.config.RabbitMQConfig;
import hmw.aiaccuracy.coreservice.dto.VerificationRequest;
import hmw.aiaccuracy.coreservice.dto.VerificationTask;
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

        VerificationTask verificationTask = new VerificationTask(jobId, request.prompt());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.VERIFICATION_ROUTING_KEY, verificationTask);
        return ResponseEntity.ok(jobId);
    }
}