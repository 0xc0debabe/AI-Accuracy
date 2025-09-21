package hmw.aiaccuracy.coreservice.service;

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

    public void publish(JobStatusUpdate update) {
        try {
            // 객체를 직접 RedisTemplate에 전달하여 직렬화를 위임합니다.
            redisTemplate.convertAndSend("job-status-topic", update);
            log.debug("Published to Redis topic 'job-status-topic': {}", update);
        } catch (Exception e) {
            log.error("Error publishing message to Redis: {}", e.getMessage());
        }
    }
}