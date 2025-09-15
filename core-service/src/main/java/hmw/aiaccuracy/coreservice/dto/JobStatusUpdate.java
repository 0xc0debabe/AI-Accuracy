package hmw.aiaccuracy.coreservice.dto;

public record JobStatusUpdate(String jobId, String status, String message, Object data) {
}
