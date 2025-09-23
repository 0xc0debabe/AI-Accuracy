package hmw.aiaccuracy.circuitbreakerservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CircuitManagerServiceTest {

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CircuitManagerService circuitManagerService;

    private final String circuitName = "test-circuit";
    private final String serviceId = "ai-adapter-service";

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Ai-Adapter 인스턴스 모두에게 서킷브레이커 OPEN이 전파 되어야한다.")
    void shouldPropagateCircuitOpenToAllAiAdapterInstances() {
        ServiceInstance instance1 = mock(ServiceInstance.class);
        when(instance1.getUri()).thenReturn(URI.create("http://localhost:8082"));
        ServiceInstance instance2 = mock(ServiceInstance.class);
        when(instance2.getUri()).thenReturn(URI.create("http://localhost:8083"));

        List<ServiceInstance> instances = Arrays.asList(instance1, instance2);
        when(discoveryClient.getInstances(serviceId)).thenReturn(instances);

        circuitManagerService.propagateCircuitOpen(circuitName);

        verify(discoveryClient, times(1)).getInstances(serviceId);

        String expectedUrl1 = "http://localhost:8082/internal/circuits/force-open/" + circuitName;
        String expectedUrl2 = "http://localhost:8083/internal/circuits/force-open/" + circuitName;

        verify(requestBodyUriSpec, times(1)).uri(expectedUrl1);
        verify(requestBodyUriSpec, times(1)).uri(expectedUrl2);

        verify(responseSpec, times(2)).bodyToMono(Void.class);
    }
}