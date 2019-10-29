package com.revolut.backend.api.exceptions;

public class InvalidRequestException
        extends ApplicationException
{
    InvalidRequestException(int code, String message)
    {
        super(code, message);
    }
}
