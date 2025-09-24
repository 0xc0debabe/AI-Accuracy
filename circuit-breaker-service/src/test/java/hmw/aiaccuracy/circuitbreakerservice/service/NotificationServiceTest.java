package hmw.aiaccuracy.circuitbreakerservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private CircuitManagerService circuitManagerService;

    @Test
    @DisplayName("실제 Gmail 계정으로 알림 이메일을 성공적으로 발송한다.")
    void sendRealEmailTest() {
        String circuitName = "real-email-test-circuit";
        circuitManagerService.propagateCircuitOpen(circuitName);
    }
}
