package com.azenticsys.polaris.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String error,
        String message,
        List<FieldError> errors,
        LocalDateTime timestamp
) {
    public record FieldError(String field, String message) {}

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, null, LocalDateTime.now());
    }

    public static ApiError of(int status, String error, String message, List<FieldError> errors) {
        return new ApiError(status, error, message, errors, LocalDateTime.now());
    }
}
