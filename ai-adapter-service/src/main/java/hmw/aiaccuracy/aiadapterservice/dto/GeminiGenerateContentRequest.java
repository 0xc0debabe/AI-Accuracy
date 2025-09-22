package hmw.aiaccuracy.aiadapterservice.dto;

import java.util.List;

public record GeminiGenerateContentRequest(List<Content> contents) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
}
