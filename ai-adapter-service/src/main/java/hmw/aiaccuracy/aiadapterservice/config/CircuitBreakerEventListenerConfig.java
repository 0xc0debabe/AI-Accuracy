package hmw.aiaccuracy.aiadapterservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class CircuitBreakerEventListenerConfig {

    private final WebClient.Builder webClientBuilder;

    public CircuitBreakerEventListenerConfig(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder
    ) {
        this.webClientBuilder = webClientBuilder;
    }


    @EventListener
    public void onCircuitBreakerStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        log.info("CircuitBreaker '{}' state changed from {} to {}",
                event.getCircuitBreakerName(), event.getStateTransition().getFromState(), event.getStateTransition().getToState());

        if (event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
            log.warn("CircuitBreaker '{}' is now OPEN! Reporting to circuit-breaker-service.", event.getCircuitBreakerName());
            notifyCircuitBreaker(event.getCircuitBreakerName());
        }
    }

    private void notifyCircuitBreaker(String circuitName) {
        String url = "http://circuit-breaker-service/circuits/notify-open/" + circuitName;
        webClientBuilder.build().post()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to notify circuit-breaker-service for circuit {}: {}", circuitName, e.getMessage()))
                .subscribe();
    }

}