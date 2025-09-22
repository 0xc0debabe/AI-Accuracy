package hmw.aiaccuracy.coreservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "exchange-name";
    public static final String VERIFICATION_QUEUE = "verification-queue";
    public static final String VERIFICATION_ROUTING_KEY = "verification-routing-key";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue queue() {
        return new Queue(VERIFICATION_QUEUE, false);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(VERIFICATION_ROUTING_KEY);
    }

    public static final String WS_QUEUE = "ws-queue";
    public static final String WS_ROUTING_KEY = "ws-routing-key";

    @Bean
    public Queue jobStatusWsQueue() {
        return new Queue(WS_QUEUE, false);
    }

    @Bean
    public Binding jobStatusWsBinding(Queue jobStatusWsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(jobStatusWsQueue).to(exchange).with(WS_ROUTING_KEY);
    }

}