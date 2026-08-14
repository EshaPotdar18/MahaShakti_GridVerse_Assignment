package com.fleet.telematics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Structured API Error Response")
public class ErrorResponse {

    @Schema(description = "HTTP Status code", example = "400")
    private int status;

    @Schema(description = "Error categorization code", example = "VALIDATION_FAILED")
    private String errorCode;

    @Schema(description = "Human-readable error message", example = "Ownership validation failed for vehicle VEH-LOGIX-101")
    private String message;

    @Schema(description = "Field-level validation error details, if applicable")
    private Map<String, String> fieldErrors;

    @Schema(description = "Timestamp when error occurred")
    private Instant timestamp;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(int status, String errorCode, String message, Map<String, String> fieldErrors) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.timestamp = Instant.now();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
