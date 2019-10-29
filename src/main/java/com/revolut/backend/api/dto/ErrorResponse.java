package com.revolut.backend.api.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Response DTO for error
 */
@Value
@Builder
public class ErrorResponse
{
    int code;
    String message;
}
