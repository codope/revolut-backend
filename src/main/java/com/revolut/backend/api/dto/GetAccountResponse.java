package com.revolut.backend.api.dto;

import lombok.Value;

@Value
public class GetAccountResponse
{
    String id;
    String name;
    Double balance;
    String currency;
}
