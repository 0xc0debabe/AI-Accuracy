package hmw.aiaccuracy.circuitbreakerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitManagerService {

    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;
    private final JavaMailSender mailSender;

    public void propagateCircuitOpen(String circuitName) {
        log.info("Propagating request to force OPEN circuit '{}' across all ai-adapter-service instances.", circuitName);
        
        sendEmailNotification(circuitName); 

        discoveryClient.getInstances("ai-adapter-service").forEach(serviceInstance -> {
            String url = serviceInstance.getUri() + "/internal/circuits/force-open/" + circuitName;
            log.info("Notifying ai-adapter-service instance at {} to force OPEN circuit '{}'", serviceInstance.getUri(), circuitName);

            webClientBuilder.build().post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnError(e -> log.error("Failed to notify ai-adapter-service instance at {} to force OPEN circuit {}: {}", serviceInstance.getUri(), circuitName, e.getMessage()))
                    .subscribe();
        });
    }

    private void sendEmailNotification(String circuitName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("aiaccuracy@gmail.com");
            message.setTo("kongminoo0421@gmail.com");
            message.setSubject("[Circuit Breaker] '" + circuitName + "이 OPEN 되었습니다.");
            message.setText(
                "다른 인스턴스의 서킷 브레이커 또한 OPEN 시키겠습니다."
            );
            mailSender.send(message);
            log.info("Successfully sent force-open notification email for circuit: {}", circuitName);
        } catch (Exception e) {
            log.error("Failed to send force-open notification email for circuit: {}", circuitName, e);
        }
    }
}
