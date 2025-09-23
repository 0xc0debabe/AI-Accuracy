package hmw.aiaccuracy.circuitbreakerservice.controller;

import hmw.aiaccuracy.circuitbreakerservice.service.CircuitManagerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CircuitBreakerControllerTest {

    @Mock
    private CircuitManagerService circuitManagerService;

    @InjectMocks
    private CircuitBreakerController circuitBreakerController;

    @Test
    @DisplayName("메서드가 호출 되었을 때, 서비스 로직을 호출한다.")
    void shouldCallPropagateCircuitOpenWhenNotifyCircuitOpenIsCalled() {
        String circuitName = "test-circuit";

        ResponseEntity<Void> response = circuitBreakerController.notifyCircuitOpen(circuitName);

        verify(circuitManagerService, times(1)).propagateCircuitOpen(circuitName);
        assertEquals(ResponseEntity.ok().build(), response);
    }

}