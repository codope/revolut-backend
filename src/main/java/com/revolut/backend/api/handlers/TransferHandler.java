package com.revolut.backend.api.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.backend.api.Handler;
import com.revolut.backend.api.constants.ApiConstants;
import com.revolut.backend.api.constants.StatusCode;
import com.revolut.backend.api.dto.ResponseEntity;
import com.revolut.backend.api.dto.TransferRequest;
import com.revolut.backend.api.dto.TransferResponse;
import com.revolut.backend.api.exceptions.ApplicationExceptions;
import com.revolut.backend.api.exceptions.GlobalExceptionHandler;
import com.revolut.backend.domain.AccountService;
import com.revolut.backend.domain.TransactionService;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.revolut.backend.api.constants.ApiConstants.HTTP_POST;

/**
 * Handler for the {@link TransactionService}
 */
public class TransferHandler
        extends Handler
{
    private static final Logger LOGGER = Logger.getLogger(TransferHandler.class.getName());
    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransferHandler(TransactionService transactionService, AccountService accountService, ObjectMapper objectMapper, GlobalExceptionHandler exceptionHandler)
    {
        super(objectMapper, exceptionHandler);
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @Override
    protected void execute(HttpExchange exchange)
            throws Exception
    {
        byte[] response;
        // only POST allowed at this endpoint
        if (HTTP_POST.equals(exchange.getRequestMethod())) {
            ResponseEntity e = doPost(exchange.getRequestBody());
            exchange.getResponseHeaders().putAll(e.getHeaders());
            exchange.sendResponseHeaders(e.getStatusCode().getCode(), 0);
            response = super.writeResponse(e.getBody());
        }
        else {
            throw ApplicationExceptions.methodNotAllowed(
                    "Method " + exchange.getRequestMethod() + " is not allowed for " + exchange.getRequestURI()).get();
        }

        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    private ResponseEntity<TransferResponse> doPost(InputStream requestBody)
            throws Exception
    {
        TransferRequest request = super.readRequest(requestBody, TransferRequest.class);
        LOGGER.log(Level.INFO, "TransferRequest: " + request.toString());
        String id = transactionService.createTransaction(request);
        TransferResponse response = transactionService.processTransaction(id, accountService);

        return new ResponseEntity<>(response, getHeaders(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_JSON), StatusCode.OK);
    }
}
