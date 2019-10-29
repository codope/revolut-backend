package com.revolut.backend.domain;

import com.revolut.backend.model.Account;

public interface AccountRepo
{
    String createAccount(Account account);

    Account getAccount(String accountID);

    Boolean updateAccount(Account updatedAccount);
}
