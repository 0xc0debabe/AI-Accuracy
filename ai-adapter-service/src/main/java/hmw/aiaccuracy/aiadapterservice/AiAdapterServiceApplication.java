package hmw.aiaccuracy.aiadapterservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AiAdapterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAdapterServiceApplication.class, args);
    }

}
