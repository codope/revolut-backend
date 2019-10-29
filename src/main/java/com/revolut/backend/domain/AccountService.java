package com.revolut.backend.domain;

import com.revolut.backend.model.Account;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AccountService
{
    private AccountRepo accountRepo;

    public String createAccount(Account account)
    {
        return accountRepo.createAccount(account);
    }

    public Account getAccount(String accountID)
    {
        return accountRepo.getAccount(accountID);
    }

    public Boolean updateAccount(Account updatedAccount)
    {
        return accountRepo.updateAccount(updatedAccount);
    }
}
