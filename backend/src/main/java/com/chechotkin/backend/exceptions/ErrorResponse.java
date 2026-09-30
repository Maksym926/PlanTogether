package com.chechotkin.backend.exceptions;

import com.chechotkin.backend.errors.BaseErrors;

import java.util.List;
import java.util.Map;


public record ErrorResponse(
        String code,
        String message,
        Map<String, List<String>> fieldErrors
) {
    public static ErrorResponse from(BaseErrors error) {
        return new ErrorResponse(error.getCode(), error.getMessage(), Map.of());
    }

    public static ErrorResponse validation(Map<String, List<String>> fieldErrors) {
        return new ErrorResponse("validation_failed", "The request has invalid fields", fieldErrors);
    }

    public static ErrorResponse malformedRequest() {
        return new ErrorResponse("malformed_request", "The request body could not be read", Map.of());
    }
}
