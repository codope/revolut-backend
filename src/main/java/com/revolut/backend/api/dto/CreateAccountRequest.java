package com.revolut.backend.api.dto;

import lombok.Value;

/**
 * Request DTO for account creation
 */
@Value
public class CreateAccountRequest
{
    String name;
    Double balance;
    String currency;
    Boolean state;
}
