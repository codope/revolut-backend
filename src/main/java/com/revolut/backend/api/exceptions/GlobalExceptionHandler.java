package com.revolut.backend.api.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.backend.api.constants.ApiConstants;
import com.revolut.backend.api.constants.StatusCode;
import com.revolut.backend.api.dto.ErrorResponse;
import com.revolut.backend.api.dto.ErrorResponse.ErrorResponseBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class GlobalExceptionHandler
{
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public void handle(Throwable throwable, HttpExchange exchange)
    {
        try {
            throwable.printStackTrace();
            exchange.getResponseHeaders().set(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_JSON);
            ErrorResponse response = getErrorResponse(throwable, exchange);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(objectMapper.writeValueAsBytes(response));
            responseBody.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ErrorResponse getErrorResponse(Throwable throwable, HttpExchange exchange)
            throws IOException
    {
        ErrorResponseBuilder responseBuilder = ErrorResponse.builder();
        if (throwable instanceof InvalidRequestException) {
            InvalidRequestException exc = (InvalidRequestException) throwable;
            responseBuilder.message(exc.getMessage()).code(exc.getCode());
            exchange.sendResponseHeaders(StatusCode.BAD_REQUEST.getCode(), 0);
        }
        else if (throwable instanceof ResourceNotFoundException) {
            ResourceNotFoundException exc = (ResourceNotFoundException) throwable;
            responseBuilder.message(exc.getMessage()).code(exc.getCode());
            exchange.sendResponseHeaders(StatusCode.RESOURCE_NOT_FOUND.getCode(), 0);
        }
        else if (throwable instanceof MethodNotAllowedException) {
            MethodNotAllowedException exc = (MethodNotAllowedException) throwable;
            responseBuilder.message(exc.getMessage()).code(exc.getCode());
            exchange.sendResponseHeaders(StatusCode.METHOD_NOT_ALLOWED.getCode(), 0);
        }
        else {
            responseBuilder.code(StatusCode.INTERNAL_SERVER_ERROR.getCode()).message(throwable.getMessage());
            exchange.sendResponseHeaders(StatusCode.INTERNAL_SERVER_ERROR.getCode(), 0);
        }
        return responseBuilder.build();
    }
}
