package com.revolut.backend.api.dto;

import com.revolut.backend.api.constants.TransactionStatus;
import lombok.Value;

@Value
public class TransferResponse
{
    String transactionID;
    TransactionStatus status;
    String createdAt;
    String completedAt;
}
