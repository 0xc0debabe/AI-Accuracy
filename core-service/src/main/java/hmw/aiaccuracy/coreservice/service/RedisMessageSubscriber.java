package hmw.aiaccuracy.coreservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.aiaccuracy.coreservice.dto.JobStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String msg = new String(message.getBody());
            JobStatusUpdate update = objectMapper.readValue(msg, JobStatusUpdate.class);
            log.debug("Received message from Redis: {}", update);
//            messagingTemplate.convertAndSend("/topic/job-status/" + update.jobId(), update);
            messagingTemplate.convertAndSend("/topic/job-status/test", update);
        } catch (IOException e) {
            log.error("Error processing Redis message: {}", e.getMessage());
        }
    }
}
