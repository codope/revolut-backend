package com.revolut.backend.api.exceptions;

class ResourceNotFoundException
        extends ApplicationException
{
    ResourceNotFoundException(int code, String message)
    {
        super(code, message);
    }
}
