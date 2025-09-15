package hmw.aiaccuracy.coreservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.aiaccuracy.coreservice.dto.JobStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(JobStatusUpdate update) {
        try {
            String message = objectMapper.writeValueAsString(update);
            redisTemplate.convertAndSend("job-status-topic", message);
            log.debug("Published to Redis topic 'job-status-topic': {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error publishing message to Redis: {}", e.getMessage());
        }
    }
}
