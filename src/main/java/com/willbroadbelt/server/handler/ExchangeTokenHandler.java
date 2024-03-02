package com.willbroadbelt.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.willbroadbelt.db.AthleteDatabase;
import com.willbroadbelt.strava.auth.AuthClient;
import com.willbroadbelt.strava.model.AthleteRecord;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.willbroadbelt.server.handler.util.HandlerUtils.handleMethodNotAllowed;
import static com.willbroadbelt.server.handler.util.HandlerUtils.parseQueryParams;

/**
 * Handle user signup
 *  - Create Athlete com.willbroadbelt.db obj with access token, etc.
 */
public class ExchangeTokenHandler implements HttpHandler {
    private Logger logger = Logger.getLogger(ExchangeTokenHandler.class.getName());

    private AuthClient authClient;

    private AthleteDatabase athleteDb;

    public ExchangeTokenHandler(final AuthClient authClient, final AthleteDatabase athleteDb) {
        this.authClient = authClient;
        this.athleteDb = athleteDb;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            try {
                handleGet(exchange);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } else {
            handleMethodNotAllowed(exchange);
        }
    }


    private void handleGet(HttpExchange exchange) throws IOException {
        logger.log(Level.INFO,"GET on /exchange_token");
        var outputStream = exchange.getResponseBody();
        var query = parseQueryParams(exchange.getRequestURI().getQuery());

        //TODO: Validation of code and scopes
        var code = query.get("code");
        var scopes = Arrays.stream(query.get("scope").split(","))
                .map(s -> AthleteRecord.Scope.from(s))
                .collect(Collectors.toList());

        String response = "";
        int status = 500;

        try {
            var initUser = authClient.initialiseUser(code);
            logger.log(Level.INFO, initUser.toString());

            var athleteRecord = new AthleteRecord(initUser.athlete().getId(),
                    scopes,
                    initUser.accessToken(),
                    initUser.refreshToken(),
                    initUser.expiresAt());
            logger.log(Level.INFO, athleteRecord.toString());

            try {
                athleteDb.insertAthlete(athleteRecord);
                response = "You have successfully been signed up to ThisApp.TM";
                status = 200;
            } catch (Exception e) {
                logger.warning(e.getMessage());
                response = "Internal Server Error";
                status = 500;
            }

        } catch (Exception e) {
            response = e.getMessage();
            status = 500;
        }
        exchange.sendResponseHeaders(status, response.length());
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
