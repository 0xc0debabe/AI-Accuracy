package hmw.aiaccuracy.circuitbreakerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitManagerService {

    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    public void propagateCircuitOpen(String circuitName) {
        log.info("Propagating request to force OPEN circuit '{}' across all ai-adapter-service instances.", circuitName);
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
}
