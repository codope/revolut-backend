package com.revolut.backend.api.dto;

import lombok.Value;

@Value
public class TransferRequest
{
    String requestID;
    String sourceAccountID;
    String targetAccountID;
    Double amount;
    String currency;
    String reference;
}
