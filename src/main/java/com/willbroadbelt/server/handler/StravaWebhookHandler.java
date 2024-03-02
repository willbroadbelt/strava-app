package com.willbroadbelt.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.willbroadbelt.server.WebhookProcessor;
import com.willbroadbelt.strava.model.Event;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.willbroadbelt.server.handler.util.HandlerUtils.handleMethodNotAllowed;
import static com.willbroadbelt.server.handler.util.HandlerUtils.parseQueryParams;

/**
 * Handler for /webhooks
 * - Get requests sent from Strava used only when setting up a subscription to this endpoint
 * - Post requests are push events from Strava for users new CRUD events (for signed-up users)
 */
public class StravaWebhookHandler implements HttpHandler {

    private static final String VERIFY_TOKEN = "STRAVA";
    private final Logger logger = Logger.getLogger(StravaWebhookHandler.class.getName());
    private final WebhookProcessor processor;
    private static final ObjectMapper mapper = new ObjectMapper();

    public StravaWebhookHandler(WebhookProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var method = exchange.getRequestMethod();
        if (method.equals("POST")) {
            handlePost(exchange);
        } else if (method.equals("GET")) {
            handleGet(exchange);
        } else {
            handleMethodNotAllowed(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        logger.log(Level.INFO, "POST on /webhook");

        String response = "";
        var code = 500;

        var outputStream = exchange.getResponseBody();
        var inputStream = exchange.getRequestBody();

        try {
            var event = mapper.readValue(inputStream, Event.class);
            logger.info(event.toString());
            processor.processEvent(event);
            response = "EVENT_RECEIVED";
            code = 200;
        } catch (Exception e) {
            logger.warning(String.format("unable to parse event: %s", e.getMessage()));
            response = "Unrecognised event format";
            code = 400;
        }

        exchange.sendResponseHeaders(code, response.length());
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        logger.log(Level.INFO,"GET on /webhook");
        var outputStream = exchange.getResponseBody();

        String response = "";
        int status = 500;

        try {
            var query = parseQueryParams(exchange.getRequestURI().getQuery());

            var mode = query.get("hub.mode");
            var token = query.get("hub.verify_token");
            var challenge = query.get("hub.challenge");

            if (!mode.isEmpty() && !token.isEmpty()) {
                if (mode.equals("subscribe") && token.equals(VERIFY_TOKEN)) {
                    logger.log(Level.INFO,"WEBHOOK_VERIFIED");
                    response = "{\"hub.challenge\":\"" + challenge + "\"}";
                    status = 200;
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                } else {
                    status = 403;
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            response = e.getMessage();
            status = 500;
        }
        exchange.sendResponseHeaders(status, response.length());
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}