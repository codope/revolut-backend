package com.revolut.backend.api.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusCode
{
    OK(200),
    CREATED(201),
    ACCEPTED(202),

    BAD_REQUEST(400),
    RESOURCE_NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),

    INTERNAL_SERVER_ERROR(500);

    private int code;
}
