package com.revolut.backend.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Account
{
    String id;
    String name;
    Double balance;
    String currency;
    Boolean state;
    String createdAt;
    String updatedAt;
}
