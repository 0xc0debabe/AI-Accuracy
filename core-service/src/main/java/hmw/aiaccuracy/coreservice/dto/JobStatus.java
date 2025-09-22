package hmw.aiaccuracy.coreservice.dto;

public enum JobStatus {
    STARTED,
    FETCHED,
    VALIDATING,
    GEMINI_SCORED,
    CHATGPT_SCORED,
    COMPLETED,
    PARTIAL_SUCCESS,
    FALLBACK_PROVIDED,
    INCORRECT_VALIDATION,
    FAILED
}
