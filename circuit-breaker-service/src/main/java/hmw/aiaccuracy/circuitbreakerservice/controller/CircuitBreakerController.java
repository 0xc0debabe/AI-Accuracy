package hmw.aiaccuracy.circuitbreakerservice.controller;

import hmw.aiaccuracy.circuitbreakerservice.service.CircuitManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/circuits")
@RequiredArgsConstructor
public class CircuitBreakerController {

    private final CircuitManagerService circuitManagerService;

    @PostMapping("/notify-open/{circuitName}")
    public ResponseEntity<Void> notifyCircuitOpen(@PathVariable String circuitName) {
        circuitManagerService.propagateCircuitOpen(circuitName);
        return ResponseEntity.ok().build();
    }
}
