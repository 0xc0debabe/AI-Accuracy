package hmw.aiaccuracy.coreservice.consumer;

import hmw.aiaccuracy.coreservice.config.KafkaConstants;
import hmw.aiaccuracy.coreservice.dto.VerificationTask;
import hmw.aiaccuracy.coreservice.service.VerificationProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationEventConsumer {

    private final VerificationProcessorService verificationProcessorService;

    @KafkaListener(topics = KafkaConstants.TOPIC_VERIFICATION_REQUEST, groupId = KafkaConstants.GROUP_ID_VERIFICATION_PROCESSOR)
    public void consumeVerificationRequest(VerificationTask task, Acknowledgment acknowledgment) {
        log.info("Consumed verification task from Kafka: jobId {}", task.getJobId());
        verificationProcessorService.processVerification(task.getJobId(), task.getPrompt());
        acknowledgment.acknowledge();
    }

}