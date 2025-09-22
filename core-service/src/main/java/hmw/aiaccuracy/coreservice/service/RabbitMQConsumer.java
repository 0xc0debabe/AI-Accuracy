package hmw.aiaccuracy.coreservice.service;

import hmw.aiaccuracy.coreservice.config.RabbitMQConfig;
import hmw.aiaccuracy.coreservice.dto.JobStatusUpdate;
import hmw.aiaccuracy.coreservice.dto.VerificationTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final VerificationProcessorService verificationProcessorService;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.VERIFICATION_QUEUE)
    public void receiveVerificationMessage(VerificationTask task) {
        log.info("Received message from RabbitMQ: {}, {}", task.jobId(), task.prompt());
        verificationProcessorService.processVerification(task.jobId(), task.prompt());
    }

    @RabbitListener(queues = RabbitMQConfig.WS_QUEUE)
    public void receiveWsMessage(JobStatusUpdate update) {
        try {
            log.debug("Received message from RabbitMQ: {}", update);
//            messagingTemplate.convertAndSend("/topic/" + update.jobId(), update);
            messagingTemplate.convertAndSend("/topic/test", update);
        } catch (Exception e) {
            log.error("Error processing RabbitMQ message and sending to WebSocket: {}", e.getMessage());
        }
    }

}
