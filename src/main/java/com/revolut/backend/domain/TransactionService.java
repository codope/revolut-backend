package com.revolut.backend.domain;

import com.revolut.backend.api.dto.TransferRequest;
import com.revolut.backend.api.dto.TransferResponse;
import com.revolut.backend.model.Transaction;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransactionService
{
    private TransactionRepo transactionRepo;

    public String createTransaction(TransferRequest request)
            throws Exception
    {
        return transactionRepo.createTransaction(request);
    }

    public Transaction getTransaction(String transactionID)
    {
        return transactionRepo.getTransaction(transactionID);
    }

    public TransferResponse processTransaction(String transactionID, AccountService accountService)
    {
        return transactionRepo.processTransaction(transactionID, accountService);
    }
}
