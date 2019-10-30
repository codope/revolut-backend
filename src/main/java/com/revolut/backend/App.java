package com.revolut.backend;

import com.revolut.backend.api.handlers.AccountHandler;
import com.revolut.backend.api.handlers.TransferHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.revolut.backend.AppConfig.getAccountService;
import static com.revolut.backend.AppConfig.getErrorHandler;
import static com.revolut.backend.AppConfig.getObjectMapper;
import static com.revolut.backend.AppConfig.getTransactionService;
import static com.revolut.backend.api.constants.ApiConstants.ACCOUNT_ENDPOINT;
import static com.revolut.backend.api.constants.ApiConstants.API_VERSION;
import static com.revolut.backend.api.constants.ApiConstants.BASE_PATH;
import static com.revolut.backend.api.constants.ApiConstants.TRANSFER_ENDPOINT;

/**
 * Starts the application server
 */
public class App
{
    private static final int SERVER_PORT = 8000;
    private static final int MAX_QUEUED_CONN = 100; // maximum number of queued incoming connections to allow on the listening socket
    private static final String ROUTE = BASE_PATH + API_VERSION;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args)
            throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), MAX_QUEUED_CONN);
        LOGGER.log(Level.INFO, "Started server at http://localhost:" + SERVER_PORT);
        // account service handler
        AccountHandler accountHandler = new AccountHandler(getAccountService(), getObjectMapper(), getErrorHandler());
        server.createContext(ROUTE + ACCOUNT_ENDPOINT, accountHandler::handle);
        // transfer service handler
        TransferHandler transferHandler = new TransferHandler(getTransactionService(), getAccountService(), getObjectMapper(), getErrorHandler());
        server.createContext(ROUTE + TRANSFER_ENDPOINT, transferHandler::handle);

        server.setExecutor(Executors.newFixedThreadPool(MAX_QUEUED_CONN)); // creates a default executor
        server.start();
    }
}
