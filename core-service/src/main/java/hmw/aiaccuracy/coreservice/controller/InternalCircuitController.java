package hmw.aiaccuracy.coreservice.controller;

import hmw.aiaccuracy.coreservice.domain.CircuitStatus;
import hmw.aiaccuracy.coreservice.service.LocalCircuitManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/circuits")
@RequiredArgsConstructor
public class InternalCircuitController {

    private final LocalCircuitManagerService localCircuitManagerService;

    @PostMapping("/update")
    public ResponseEntity<Void> updateCircuitStatus(@RequestBody CircuitUpdate update) {
        localCircuitManagerService.updateCircuitStatus(update.circuitName(), update.status());
        return ResponseEntity.ok().build();
    }

    private record CircuitUpdate(String circuitName, CircuitStatus status) {}
}
