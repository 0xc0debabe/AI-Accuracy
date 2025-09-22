package hmw.aiaccuracy.aiadapterservice.dto;

import java.util.List;

public record OpenAIChatRequest(String model, List<Message> messages) {
    public record Message(String role, String content) {}
}
