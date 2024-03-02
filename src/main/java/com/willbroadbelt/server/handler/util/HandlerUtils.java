package com.willbroadbelt.server.handler.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HandlerUtils {

    /**
     * Utility to parse query parameters 'key1=val1&key2=val2&..' into a map
     */
    public static Map<String,String> parseQueryParams(String query) {
        return Arrays.stream(query.split("&"))
                .map(kv -> kv.split("="))
                .filter(f -> f.length > 1)
                .collect(Collectors.toMap(x -> x[0], x -> x[1]));
    }

    public static void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, 0);
        var outputStream = exchange.getResponseBody();
        outputStream.flush();
        outputStream.close();
    }
}
