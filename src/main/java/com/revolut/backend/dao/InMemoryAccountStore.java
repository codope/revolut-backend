package com.revolut.backend.dao;

import com.revolut.backend.domain.AccountRepo;
import com.revolut.backend.model.Account;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.revolut.backend.api.ApiUtils.toTimeString;

public class InMemoryAccountStore
        implements AccountRepo
{
    private static final Double INITIAL_BALANCE = 100.00;
    private static final Map<String, Account> ACCOUNT_MAP = new ConcurrentHashMap();

    /**
     * Create account
     *
     * @param account
     * @return account ID
     */
    @Override
    public String createAccount(Account account)
    {
        // generate account ID
        String id = UUID.randomUUID().toString();
        // create new account
        Account newAccount = Account.builder()
                .id(id)
                .name(account.getName())
                .balance(account.getBalance() == null ? INITIAL_BALANCE : account.getBalance())
                .currency(account.getCurrency())
                .state(account.getState())
                .createdAt(toTimeString(LocalDateTime.now()))
                .updatedAt(null)
                .build();
        // save the new account
        ACCOUNT_MAP.put(id, newAccount);

        return id;
    }

    /**
     * Fetch account for the given account ID.
     *
     * @param accountID
     * @return {@link Account} details.
     */
    @Override
    public Account getAccount(String accountID)
    {
        Account account = ACCOUNT_MAP.get(accountID);

        return account;
    }

    /**
     * Generic account updater. Currently being used for account balance update. But it can be used for any account field update.
     *
     * @param updatedAccount
     * @return true if update is success, otherwise false
     */
    @Override
    public Boolean updateAccount(Account updatedAccount)
    {
        if (ACCOUNT_MAP.containsKey(updatedAccount.getId())) {
            ACCOUNT_MAP.put(updatedAccount.getId(), updatedAccount);
            return true;
        }

        return false;
    }
}
