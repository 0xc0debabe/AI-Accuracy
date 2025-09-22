package hmw.aiaccuracy.circuitbreakerservice.controller;

import hmw.aiaccuracy.circuitbreakerservice.domain.CircuitStatus;
import hmw.aiaccuracy.circuitbreakerservice.dto.FailureReport;
import hmw.aiaccuracy.circuitbreakerservice.service.CircuitManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
