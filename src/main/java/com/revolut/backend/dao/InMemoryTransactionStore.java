package com.revolut.backend.dao;

import com.revolut.backend.api.constants.TransactionStatus;
import com.revolut.backend.api.dto.TransferRequest;
import com.revolut.backend.api.dto.TransferResponse;
import com.revolut.backend.api.exceptions.ApplicationExceptions;
import com.revolut.backend.domain.AccountService;
import com.revolut.backend.domain.TransactionRepo;
import com.revolut.backend.model.Account;
import com.revolut.backend.model.Transaction;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.revolut.backend.api.ApiUtils.toTimeString;

public class InMemoryTransactionStore
        implements TransactionRepo
{
    private static final Map<String, Transaction> TRANSACTION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> TRANSFER_REQ_TO_TRANSACTION_MAP = new ConcurrentHashMap<>();

    /**
     * Creates the Transaction
     *
     * @param request
     * @return transaction ID
     * @throws Exception
     */
    @Override
    public String createTransaction(TransferRequest request)
            throws Exception
    {
        // check duplicate request for idempotency
        if (TRANSFER_REQ_TO_TRANSACTION_MAP.containsKey(request.getRequestID())) {
            String transactionID = TRANSFER_REQ_TO_TRANSACTION_MAP.get(request.getRequestID());
            if (TRANSACTION_MAP.get(transactionID).getStatus() == TransactionStatus.COMPLETED) {
                throw ApplicationExceptions.invalidRequest().apply(new Throwable("Duplicate Transaction: " + TRANSFER_REQ_TO_TRANSACTION_MAP.get(request.getRequestID())));
            }
        }
        // generate transaction ID
        String id = UUID.randomUUID().toString();
        String type = "transfer"; // currently, only money transfer type is supported
        String requestID = request.getRequestID();
        TransactionStatus status = TransactionStatus.PENDING;
        String createdAt = toTimeString(LocalDateTime.now());
        String reference = request.getReference();
        // a transaction involves a handshake, i.e. money moves from one account to another
        Transaction.Handshake hand1 = Transaction.Handshake.builder() // debit hand
                .id(UUID.randomUUID().toString())
                .accountID(request.getSourceAccountID())
                .amount(request.getAmount() * -1.0)
                .currency(request.getCurrency())
                .build();
        Transaction.Handshake hand2 = Transaction.Handshake.builder() // credit hand
                .id(UUID.randomUUID().toString())
                .accountID(request.getTargetAccountID())
                .amount(request.getAmount() * 1.0)
                .currency(request.getCurrency())
                .build();
        Transaction.Handshake[] handshakes = new Transaction.Handshake[] {hand1, hand2};
        // create the transaction
        Transaction transaction = Transaction.builder()
                .id(id)
                .type(type)
                .requestID(requestID)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(null)
                .completedAt(null)
                .reference(reference)
                .handshakes(handshakes)
                .build();
        // save the transaction
        TRANSACTION_MAP.put(id, transaction);
        // persist request ID and transaction ID for handling idempotency
        TRANSFER_REQ_TO_TRANSACTION_MAP.put(requestID, id);
        // return transaction ID
        return id;
    }

    /**
     * Fetch the transaction based on transaction ID
     *
     * @param transactionID
     * @return {@link Transaction} details
     */
    @Override
    public Transaction getTransaction(String transactionID)
    {
        Transaction transaction = TRANSACTION_MAP.get(transactionID);

        return transaction;
    }

    /**
     * Processes the transaction, i.e. the state of transaction changes from PENDING to either FAILED or COMPLETED state.
     *
     * @param transactionID
     * @param accountService
     * @return {@link TransferResponse}
     */
    @Override
    public TransferResponse processTransaction(String transactionID, AccountService accountService)
    {
        Transaction transaction = TRANSACTION_MAP.get(transactionID);
        if (transaction == null) {
            throw ApplicationExceptions.notFound("Transaction not found: " + transactionID).get();
        }

        Transaction.Handshake[] handshakes = transaction.getHandshakes();

        Account sourceAccount = accountService.getAccount(handshakes[0].getAccountID());
        if (sourceAccount == null) {
            throw ApplicationExceptions.notFound("Account not found: " + handshakes[0].getAccountID()).get();
        }

        Account targetAccount = accountService.getAccount(handshakes[1].getAccountID());
        if (targetAccount == null) {
            throw ApplicationExceptions.notFound("Account not found: " + handshakes[1].getAccountID()).get();
        }

        Double updatedSourceBalance = sourceAccount.getBalance() + handshakes[0].getAmount();
        Double updatedTargetBalance = targetAccount.getBalance() + handshakes[1].getAmount();
        // deep copy of the source and target account with updated balance
        Account updatedSourceAccount = sourceAccount.toBuilder().balance(updatedSourceBalance).build();
        Account updatedTargetAccount = targetAccount.toBuilder().balance(updatedTargetBalance).build();
        // update both accounts
        Boolean updateSourceSuccess = accountService.updateAccount(updatedSourceAccount);
        Boolean updateTargetSuccess = accountService.updateAccount(updatedTargetAccount);
        // if any update fails, return failed transaction in transfer response
        if (!updateSourceSuccess || !updateTargetSuccess) {
            Transaction updatedTransaction = transaction.toBuilder()
                    .status(TransactionStatus.FAILED)
                    .updatedAt(toTimeString(LocalDateTime.now()))
                    .build();
            TRANSACTION_MAP.put(transactionID, updatedTransaction);
            return new TransferResponse(transactionID, TransactionStatus.FAILED, transaction.getCreatedAt(), null);
        }
        // if both updates are successful, update transaction status and completed time
        String completedAt = toTimeString(LocalDateTime.now());
        Transaction updatedTransaction = transaction.toBuilder()
                .status(TransactionStatus.COMPLETED)
                .updatedAt(completedAt)
                .completedAt(completedAt).build();
        TRANSACTION_MAP.put(transactionID, updatedTransaction);

        return new TransferResponse(transactionID, TransactionStatus.COMPLETED, transaction.getCreatedAt(), completedAt);
    }
}
