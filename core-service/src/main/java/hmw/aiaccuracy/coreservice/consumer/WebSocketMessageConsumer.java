package hmw.aiaccuracy.coreservice.consumer;

import hmw.aiaccuracy.coreservice.config.KafkaConstants;
import hmw.aiaccuracy.coreservice.dto.JobStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaConstants.TOPIC_WS_MSG_REQUEST, groupId = KafkaConstants.GROUP_ID_WS_BROADCASTER)
    public void consumeJobStatusUpdate(JobStatusUpdate update, Acknowledgment acknowledgment) {
        try {
            log.debug("Consumed job status update from Kafka: {}", update);
//            messagingTemplate.convertAndSend("/topic/" + update.jobId(), update);
            messagingTemplate.convertAndSend("/topic/test", update);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Kafka message and sending to WebSocket: {}", e.getMessage());
        }
    }
}