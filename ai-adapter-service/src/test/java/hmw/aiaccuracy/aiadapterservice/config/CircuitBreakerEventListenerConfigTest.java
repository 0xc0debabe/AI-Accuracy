package hmw.aiaccuracy.aiadapterservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CircuitBreakerEventListenerConfigTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private CircuitBreakerEventListenerConfig config;
    private final String circuitName = "test-circuit";

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        config = new CircuitBreakerEventListenerConfig(webClientBuilder);
    }

    @Test
    @DisplayName("서킷 OPEN 시, 서킷 서버에게 통보해줘야한다.")
    void shouldNotifyCircuitBreakerServiceWhenCircuitOpens() {
        CircuitBreakerOnStateTransitionEvent openEvent = new CircuitBreakerOnStateTransitionEvent(
                circuitName,
                CircuitBreaker.StateTransition.CLOSED_TO_OPEN
        );

        config.onCircuitBreakerStateTransition(openEvent);

        verify(responseSpec, times(1)).bodyToMono(Void.class);
    }

    @Test
    @DisplayName("서킷 CLOSE 시, 서킷 서버에게 통보해주지 않는다.")
    void shouldNotifyCircuitBreakerServiceWhenCircuitDoesNotOpen() {
        CircuitBreakerOnStateTransitionEvent closeEvent = new CircuitBreakerOnStateTransitionEvent(
                circuitName,
                CircuitBreaker.StateTransition.OPEN_TO_CLOSED
        );

        config.onCircuitBreakerStateTransition(closeEvent);

        verify(webClientBuilder, never()).build();
        verify(webClient, never()).post();
        verify(requestBodyUriSpec, never()).uri(anyString());
        verify(responseSpec, never()).bodyToMono(Void.class);
    }

}