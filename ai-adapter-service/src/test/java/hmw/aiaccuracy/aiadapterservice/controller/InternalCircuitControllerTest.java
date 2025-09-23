package hmw.aiaccuracy.aiadapterservice.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalCircuitControllerTest {

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private InternalCircuitController internalCircuitController;

    @BeforeEach
    void setUp() {
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
    }

    @Test
    @DisplayName("메서드가 호출 되었을 때, 서킷은 강제로 OPEN 되어야한다.")
    void shouldForceOpenCircuitWhenForceOpenCircuitIsCalled() {
        String circuitName = "test-circuit";

        ResponseEntity<Void> response = internalCircuitController.forceOpenCircuit(circuitName);

        verify(circuitBreakerRegistry, times(1)).circuitBreaker(circuitName);
        verify(circuitBreaker, times(1)).transitionToOpenState();
        assertEquals(ResponseEntity.ok().build(), response);
    }

}