package com.revolut.backend.api.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.backend.api.Handler;
import com.revolut.backend.api.constants.ApiConstants;
import com.revolut.backend.api.constants.StatusCode;
import com.revolut.backend.api.dto.CreateAccountRequest;
import com.revolut.backend.api.dto.CreateAccountResponse;
import com.revolut.backend.api.dto.GetAccountResponse;
import com.revolut.backend.api.dto.ResponseEntity;
import com.revolut.backend.api.exceptions.ApplicationExceptions;
import com.revolut.backend.api.exceptions.GlobalExceptionHandler;
import com.revolut.backend.domain.AccountService;
import com.revolut.backend.model.Account;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.revolut.backend.api.ApiUtils.splitQuery;
import static com.revolut.backend.api.constants.ApiConstants.HTTP_GET;
import static com.revolut.backend.api.constants.ApiConstants.HTTP_POST;

/**
 * Handler for the {@link AccountService}
 */
public class AccountHandler
        extends Handler
{
    private static final Logger LOGGER = Logger.getLogger(AccountHandler.class.getName());
    private final AccountService accountService;

    public AccountHandler(AccountService accountService, ObjectMapper objectMapper, GlobalExceptionHandler exceptionHandler)
    {
        super(objectMapper, exceptionHandler);
        this.accountService = accountService;
    }

    @Override
    protected void execute(HttpExchange exchange)
            throws Exception
    {
        byte[] response;
        // only POST and GET allowed at this endpoint, otherwise throw MethodNotAllowedException
        if (HTTP_POST.equals(exchange.getRequestMethod())) {
            ResponseEntity e = doPost(exchange.getRequestBody());
            exchange.getResponseHeaders().putAll(e.getHeaders());
            exchange.sendResponseHeaders(e.getStatusCode().getCode(), 0);
            response = super.writeResponse(e.getBody());
        }
        else if (HTTP_GET.equals(exchange.getRequestMethod())) {
            Map<String, List<String>> params = splitQuery(exchange.getRequestURI().getRawQuery());
            String noNameText = "Anonymous";
            String id = params.getOrDefault("id", List.of(noNameText)).stream().findFirst().orElse(noNameText);
            ResponseEntity e = doGet(id);
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

    /**
     * Create {@link Account}
     *
     * @param requestBody
     * @return
     */
    private ResponseEntity<CreateAccountResponse> doPost(InputStream requestBody)
    {
        CreateAccountRequest request = super.readRequest(requestBody, CreateAccountRequest.class);
        LOGGER.log(Level.INFO, "CreateAccountRequest: " + request.toString());
        Account account = Account.builder()
                .name(request.getName())
                .balance(request.getBalance())
                .currency(request.getCurrency())
                .state(request.getState() == null ? true : request.getState())
                .build();
        String accountID = accountService.createAccount(account);
        CreateAccountResponse response = new CreateAccountResponse(accountID);

        return new ResponseEntity<>(response, getHeaders(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_JSON), StatusCode.OK);
    }

    /**
     * Get {@link Account} based on {@param accountID}
     *
     * @param accountID
     * @return
     * @throws Exception
     */
    private ResponseEntity<GetAccountResponse> doGet(String accountID)
            throws Exception
    {
        Account account = accountService.getAccount(accountID);
        LOGGER.log(Level.INFO, "GetAccountRequest: " + accountID);

        if (account == null) {
            LOGGER.log(Level.WARNING, "Account not found: " + accountID);
            throw ApplicationExceptions.notFound("Account not found: " + accountID).get();
        }

        GetAccountResponse response = new GetAccountResponse(account.getId(), account.getName(), account.getBalance(), account.getCurrency());

        return new ResponseEntity<>(response, getHeaders(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_JSON), StatusCode.OK);
    }
}
