package com.revolut.backend.api.exceptions;

class MethodNotAllowedException
        extends ApplicationException
{
    MethodNotAllowedException(int code, String message)
    {
        super(code, message);
    }
}
