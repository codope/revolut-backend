package com.revolut.backend;

import com.revolut.backend.domain.AccountService;
import com.revolut.backend.model.Account;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AccountServiceTest
{
    private static AccountService accountService;

    @BeforeClass
    public static void setup()
    {
        accountService = AppConfig.getAccountService();
    }

    @AfterClass
    public static void tearDown()
    {
        accountService = null;
    }

    @Test
    public void testCreateAndGetAccount()
    {
        String name = "Pink";
        Double balance = 200.00;
        String currency = "EUR";

        String id = accountService.createAccount(Account.builder().name(name).balance(balance).currency(currency).build());
        Account account = accountService.getAccount(id);

        assertEquals(id, account.getId());
        assertEquals(name, account.getName());
        assertEquals(balance, account.getBalance());
        assertEquals(currency, account.getCurrency());
    }

    @Test
    public void testCreateDefaultBalanceAndGetAccount()
    {
        String name = "Floyd";
        String currency = "EUR";

        String id = accountService.createAccount(Account.builder().name(name).currency(currency).build());
        Account account = accountService.getAccount(id);

        assertEquals(id, account.getId());
        assertEquals(name, account.getName());

        Double balance = 100.00;

        assertEquals(balance, account.getBalance());
        assertEquals(currency, account.getCurrency());
    }

    @Test
    public void testAccountNotFound()
    {
        String id = UUID.randomUUID().toString();
        assertNull(accountService.getAccount(id));
    }
}
