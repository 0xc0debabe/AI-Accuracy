package hmw.aiaccuracy.circuitbreakerservice.service;

import hmw.aiaccuracy.circuitbreakerservice.domain.CircuitStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitManagerService {

    private final ConcurrentHashMap<String, CircuitStatus> circuitStatuses = new ConcurrentHashMap<>();
    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    public void reportFailure(String circuitName) {
        circuitStatuses.put(circuitName, CircuitStatus.OPEN);
        log.info("Circuit '{}' has been manually set to OPEN due to a failure report.", circuitName);
        propagateStateChange(circuitName, CircuitStatus.OPEN);
    }

    public CircuitStatus getStatus(String circuitName) {
        return circuitStatuses.getOrDefault(circuitName, CircuitStatus.CLOSED);
    }

    public void manualUpdate(String circuitName, CircuitStatus status) {
        circuitStatuses.put(circuitName, status);
        log.info("Circuit '{}' has been manually set to {}.", circuitName, status);
        propagateStateChange(circuitName, status);
    }

    private void propagateStateChange(String circuitName, CircuitStatus status) {
        discoveryClient.getInstances("core-service").forEach(serviceInstance -> {
            String url = serviceInstance.getUri() + "/internal/circuits/update";
            log.info("Notifying core-service instance at {} about circuit {} status {}", url, circuitName, status);

            webClientBuilder.build().post()
                    .uri(url)
                    .bodyValue(new CircuitUpdate(circuitName, status))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnError(e -> log.error("Failed to notify core-service instance at {}: {}", serviceInstance.getUri(), e.getMessage()))
                    .subscribe();
        });
    }

    // Inner record for propagation payload
    private record CircuitUpdate(String circuitName, CircuitStatus status) {}
}
