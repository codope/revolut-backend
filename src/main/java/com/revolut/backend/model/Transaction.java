package com.revolut.backend.model;

import com.revolut.backend.api.constants.TransactionStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Transaction
{
    String id;
    String type;
    String requestID;
    TransactionStatus status;
    String createdAt;
    String updatedAt;
    String completedAt;
    String reference;
    Handshake[] handshakes;

    @Value
    @Builder
    public static class Handshake
    {
        String id;
        String accountID;
        Double amount;
        String currency;
    }
}
