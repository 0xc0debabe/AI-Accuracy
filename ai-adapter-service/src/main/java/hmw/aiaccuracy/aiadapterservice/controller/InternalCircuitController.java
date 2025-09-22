package hmw.aiaccuracy.aiadapterservice.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/circuits")
@RequiredArgsConstructor
@Slf4j
public class InternalCircuitController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostMapping("/force-open/{circuitName}")
    public ResponseEntity<Void> forceOpenCircuit(@PathVariable String circuitName) {
        log.info("Received request to force OPEN circuit '{}'", circuitName);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitName);
        circuitBreaker.transitionToOpenState();
        return ResponseEntity.ok().build();
    }

}
