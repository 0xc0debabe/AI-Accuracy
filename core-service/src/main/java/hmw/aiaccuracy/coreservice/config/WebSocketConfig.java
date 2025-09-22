package hmw.aiaccuracy.coreservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    private String rabbitmqHost;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // STOMP 브로커 릴레이를 활성화하여 외부 RabbitMQ와 연동합니다.
        config.enableStompBrokerRelay("/topic")
              .setRelayHost(rabbitmqHost)
              .setRelayPort(61613)
              .setClientLogin("guest")
              .setClientPasscode("guest");

        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }
}
