package hmw.aiaccuracy.coreservice.dto;

public record JobStatusUpdate(String jobId, JobStatus status, String message, Object data) {
}
