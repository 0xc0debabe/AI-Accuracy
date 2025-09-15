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

    @PostMapping("/report")
    public ResponseEntity<Void> reportFailure(@RequestBody FailureReport report) {
        circuitManagerService.reportFailure(report.circuitName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{circuitName}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String circuitName) {
        CircuitStatus status = circuitManagerService.getStatus(circuitName);
        return ResponseEntity.ok(Map.of("circuitName", circuitName, "status", status));
    }

    @PostMapping("/manual/{circuitName}/{status}")
    public ResponseEntity<Void> manualUpdate(@PathVariable String circuitName, @PathVariable CircuitStatus status) {
        circuitManagerService.manualUpdate(circuitName, status);
        return ResponseEntity.ok().build();
    }
}
