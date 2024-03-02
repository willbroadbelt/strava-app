package com.willbroadbelt.server;

import com.sun.net.httpserver.HttpServer;
import com.willbroadbelt.db.AthleteDatabase;
import com.willbroadbelt.db.AthleteDatabaseLocal;
import com.willbroadbelt.server.actions.HelloAction;
import com.willbroadbelt.server.handler.DefaultHandler;
import com.willbroadbelt.server.handler.ExchangeTokenHandler;
import com.willbroadbelt.server.handler.StravaWebhookHandler;
import com.willbroadbelt.strava.auth.AuthClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sign up using:
//  https://www.strava.com/oauth/authorize?client_id=93200&response_type=code&redirect_uri=https://688d-2a00-23c6-e887-1e01-116f-f8ed-c13d-a037.eu.ngrok.io/exchange_token&approval_prompt=force&scope=read,read_all,activity:read_all,activity:write,profile:read_allhttps://www.strava.com/oauth/authorize?client_id=93200&response_type=code&redirect_uri=https://688d-2a00-23c6-e887-1e01-116f-f8ed-c13d-a037.eu.ngrok.io/exchange_token&approval_prompt=force&scope=read,read_all,activity:read_all,activity:write,profile:read_all

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static String clientId;
    private static String clientSecret;
    private static String couchbaseHost;
    private static String couchbaseUser;
    private static String couchbasePassword;

    public static void main(String[] args) throws IOException {

        Logger logger = Logger.getLogger("com.willbroadbelt");
        logger.setLevel(Level.FINE);
        for (Handler h : logger.getParent().getHandlers()) {
            if (h instanceof ConsoleHandler) {
                h.setLevel(Level.FINE);
            }
        }

        String configFilePath = "config.properties";
        try (FileInputStream propsInput = new FileInputStream(configFilePath)) {
            Properties prop = new Properties();
            prop.load(propsInput);
            clientId = prop.getProperty("CLIENT_ID");
            clientSecret = prop.getProperty("CLIENT_SECRET");
            couchbaseHost = prop.getProperty("CB_HOST");
            couchbaseUser = prop.getProperty("CB_USER");
            couchbasePassword = prop.getProperty("CB_PASSWORD");
        }

        AuthClient authClient = new AuthClient(clientId, clientSecret);
        AthleteDatabase athleteDb = new AthleteDatabaseLocal(authClient);
        //AthleteDatabaseCouchbase athleteDb = new AthleteDatabaseCouchbase(authClient, couchbaseHost, couchbaseUser, couchbasePassword);

        WebhookProcessor processor = new WebhookProcessor(athleteDb)
                .registerEventAction(new HelloAction());

        HttpServer server = HttpServer.create(new InetSocketAddress( 80), 0);
        server.createContext("/webhook", new StravaWebhookHandler(processor));
        server.createContext("/exchange_token", new ExchangeTokenHandler(authClient, athleteDb));
        server.createContext("/", new DefaultHandler());
        server.start();
        logger.info("Started server");
    }

}
