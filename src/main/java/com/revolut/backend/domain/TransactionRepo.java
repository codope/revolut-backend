package com.revolut.backend.domain;

import com.revolut.backend.api.dto.TransferRequest;
import com.revolut.backend.api.dto.TransferResponse;
import com.revolut.backend.model.Transaction;

public interface TransactionRepo
{
    String createTransaction(TransferRequest request)
            throws Exception;

    Transaction getTransaction(String transactionID);

    TransferResponse processTransaction(String transactionID, AccountService accountService);
}
