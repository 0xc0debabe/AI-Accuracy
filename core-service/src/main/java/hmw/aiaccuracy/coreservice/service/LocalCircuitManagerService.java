package hmw.aiaccuracy.coreservice.service;

import hmw.aiaccuracy.coreservice.domain.CircuitStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LocalCircuitManagerService {

    private final ConcurrentHashMap<String, CircuitStatus> localCircuitStatuses = new ConcurrentHashMap<>();

    public void updateCircuitStatus(String circuitName, CircuitStatus status) {
        localCircuitStatuses.put(circuitName, status);
        log.info("Local circuit status updated for '{}': {}", circuitName, status);
    }

    public CircuitStatus getCircuitStatus(String circuitName) {
        return localCircuitStatuses.getOrDefault(circuitName, CircuitStatus.CLOSED);
    }
}
