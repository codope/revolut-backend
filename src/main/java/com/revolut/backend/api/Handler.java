package com.revolut.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.backend.api.exceptions.ApplicationExceptions;
import com.revolut.backend.api.exceptions.GlobalExceptionHandler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.vavr.control.Try;

import java.io.InputStream;

public abstract class Handler
{
    private final ObjectMapper objectMapper;
    private final GlobalExceptionHandler exceptionHandler;

    public Handler(ObjectMapper objectMapper,
            GlobalExceptionHandler exceptionHandler)
    {
        this.objectMapper = objectMapper;
        this.exceptionHandler = exceptionHandler;
    }

    protected static Headers getHeaders(String key, String value)
    {
        Headers headers = new Headers();
        headers.set(key, value);
        return headers;
    }

    public void handle(HttpExchange exchange)
    {
        Try.run(() -> execute(exchange))
                .onFailure(thr -> exceptionHandler.handle(thr, exchange));
    }

    protected abstract void execute(HttpExchange exchange)
            throws Exception;

    protected <T> T readRequest(InputStream is, Class<T> type)
    {
        return Try.of(() -> objectMapper.readValue(is, type))
                .getOrElseThrow(ApplicationExceptions.invalidRequest());
    }

    protected <T> byte[] writeResponse(T response)
    {
        return Try.of(() -> objectMapper.writeValueAsBytes(response))
                .getOrElseThrow(ApplicationExceptions.invalidRequest());
    }
}
