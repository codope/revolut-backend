package com.revolut.backend;

import com.revolut.backend.api.constants.TransactionStatus;
import com.revolut.backend.api.dto.TransferRequest;
import com.revolut.backend.api.dto.TransferResponse;
import com.revolut.backend.api.exceptions.InvalidRequestException;
import com.revolut.backend.domain.AccountService;
import com.revolut.backend.domain.TransactionService;
import com.revolut.backend.model.Account;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

/**
 * Unit test for {@link TransactionService} which does the money transfer
 */
public class TransactionServiceTest
{
    private static TransactionService transactionService;
    private static AccountService accountService;
    private static Account sourceAccount;
    private static Account targetAccount;

    @BeforeClass
    public static void setup()
    {
        transactionService = AppConfig.getTransactionService();
        accountService = AppConfig.getAccountService();
        sourceAccount = createAndGetAccount(Account.builder().name("Pink").balance(200.00).currency("EUR").build());
        targetAccount = createAndGetAccount(Account.builder().name("Floyd").currency("EUR").build());
    }

    @AfterClass
    public static void tearDown()
    {
        transactionService = null;
        accountService = null;
        sourceAccount = null;
        targetAccount = null;
    }

    private static Account createAndGetAccount(Account account)
    {
        String id = accountService.createAccount(account);
        return accountService.getAccount(id);
    }

    /**
     * Test a transfer
     *
     * @throws Exception
     */
    @Test
    public void testCreateTransactionForTransfer()
            throws Exception
    {
        Double currentSourceBalance = sourceAccount.getBalance();
        Double currentTargetBalance = targetAccount.getBalance();
        Double amountToTransfer = 50.50;
        TransferRequest request = new TransferRequest("req1", sourceAccount.getId(), targetAccount.getId(), amountToTransfer, "EUR", "test transfer");
        String id = transactionService.createTransaction(request);

        assertEquals(TransactionStatus.PENDING, transactionService.getTransaction(id).getStatus());
        assertEquals(currentSourceBalance, sourceAccount.getBalance());
        assertEquals(currentTargetBalance, targetAccount.getBalance());

        TransferResponse response = transactionService.processTransaction(id, accountService);
        assertNotNull(response);
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());

        Double expectedSourceBalance = currentSourceBalance - amountToTransfer;
        Double expectedTargetBalance = currentTargetBalance + amountToTransfer;
        assertEquals(expectedSourceBalance, accountService.getAccount(sourceAccount.getId()).getBalance());
        assertEquals(expectedTargetBalance, accountService.getAccount(targetAccount.getId()).getBalance());
    }

    /**
     * Test a duplicate transfer request.
     *
     * @throws Exception
     */
    @Test
    public void testCreateTransactionIdempotency()
            throws Exception
    {
        Double currentSourceBalance = sourceAccount.getBalance();
        Double currentTargetBalance = targetAccount.getBalance();
        Double amountToTransfer = 50.50;
        TransferRequest request = new TransferRequest("req2", sourceAccount.getId(), targetAccount.getId(), amountToTransfer, "EUR", "test transfer");
        String id = transactionService.createTransaction(request);

        assertEquals(TransactionStatus.PENDING, transactionService.getTransaction(id).getStatus());
        assertEquals(currentSourceBalance, sourceAccount.getBalance());
        assertEquals(currentTargetBalance, targetAccount.getBalance());

        TransferResponse response = transactionService.processTransaction(id, accountService);
        assertNotNull(response);
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        // let's try the same transaction request again and assert that an exception is thrown
        assertThrows(InvalidRequestException.class, () -> transactionService.createTransaction(request));
    }
}
