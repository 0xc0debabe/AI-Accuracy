package hmw.aiaccuracy.coreservice.service;

import hmw.aiaccuracy.coreservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final VerificationProcessorService verificationProcessorService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        log.info("Received message from RabbitMQ: {}", message);
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String jobId = parts[0];
            String prompt = parts[1];
            verificationProcessorService.processVerification(jobId, prompt);
        } else {
            log.error("Invalid message format received: {}", message);
        }
    }
}
