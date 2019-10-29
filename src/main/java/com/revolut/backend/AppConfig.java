package com.revolut.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.backend.api.exceptions.GlobalExceptionHandler;
import com.revolut.backend.dao.InMemoryAccountStore;
import com.revolut.backend.dao.InMemoryTransactionStore;
import com.revolut.backend.domain.AccountRepo;
import com.revolut.backend.domain.AccountService;
import com.revolut.backend.domain.TransactionRepo;
import com.revolut.backend.domain.TransactionService;

public class AppConfig
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AccountRepo ACCOUNT_REPO = new InMemoryAccountStore();
    private static final AccountService ACCOUNT_SERVICE = new AccountService(ACCOUNT_REPO);
    private static final TransactionRepo TRANSACTION_REPO = new InMemoryTransactionStore();
    private static final TransactionService TRANSACTION_SERVICE = new TransactionService(TRANSACTION_REPO);
    private static final GlobalExceptionHandler GLOBAL_ERROR_HANDLER = new GlobalExceptionHandler(OBJECT_MAPPER);

    static ObjectMapper getObjectMapper()
    {
        return OBJECT_MAPPER;
    }

    static AccountService getAccountService()
    {
        return ACCOUNT_SERVICE;
    }

    static AccountRepo getAccountRepo()
    {
        return ACCOUNT_REPO;
    }

    static TransactionService getTransactionService()
    {
        return TRANSACTION_SERVICE;
    }

    static TransactionRepo getTransactionRepo()
    {
        return TRANSACTION_REPO;
    }

    public static GlobalExceptionHandler getErrorHandler()
    {
        return GLOBAL_ERROR_HANDLER;
    }
}
