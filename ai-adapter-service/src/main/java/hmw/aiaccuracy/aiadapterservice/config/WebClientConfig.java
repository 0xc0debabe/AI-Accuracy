package hmw.aiaccuracy.aiadapterservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // 내부 마이크로서비스 호출용 (LoadBalanced 적용)
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    // 외부 API 호출용 (LoadBalanced 적용 안 함)
    @Bean
    public WebClient.Builder nonLoadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
