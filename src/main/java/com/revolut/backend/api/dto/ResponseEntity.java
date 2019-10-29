package com.revolut.backend.api.dto;

import com.revolut.backend.api.constants.StatusCode;
import com.sun.net.httpserver.Headers;
import lombok.Value;

@Value
public class ResponseEntity<T>
{
    private final T body;
    private final Headers headers;
    private final StatusCode statusCode;
}
