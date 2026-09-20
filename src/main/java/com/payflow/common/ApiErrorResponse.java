package com.payflow.common;

import java.time.Instant;

public class ApiErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private Instant timestamp;

    public ApiErrorResponse(String message, String errorCode) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    public Instant getTimestamp() { return timestamp; }
}