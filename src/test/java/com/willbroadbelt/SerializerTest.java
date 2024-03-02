package com.willbroadbelt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import com.willbroadbelt.strava.auth.AuthClient;
import com.willbroadbelt.strava.model.Event;

import static org.junit.Assert.assertEquals;

public class SerializerTest {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
    //Setting the default LF - prevent test issues on Windows with CRLF
            .setDefaultPrettyPrinter(new DefaultPrettyPrinter()
                    .withObjectIndenter(new DefaultIndenter()
                            .withLinefeed("\n")));

    private static final AuthClient.AuthResponse testRefreshResponse = new AuthClient.AuthResponse(
            "Bearer",
            "06c3239ab8937ace12345678901293435566fkhg1",
            1675028632L,
            21263,
            "9ae5e8885c6bcee4e757c123456fhjklkdf4",
            null);

    private static String jsonRefresh = """
                {
                  "token_type" : "Bearer",
                  "access_token" : "06c3239ab8937ace12345678901293435566fkhg1",
                  "expires_at" : 1675028632,
                  "expires_in" : 21263,
                  "refresh_token" : "9ae5e8885c6bcee4e757c123456fhjklkdf4"
                }""";


    @Test
    public void refreshTokenSerialize() throws JsonProcessingException {
        var serialized = mapper.writeValueAsString(testRefreshResponse);
        assertEquals(jsonRefresh, serialized);
    }

    @Test
    public void refreshTokenDeserialize() throws JsonProcessingException {
        var deserialized = mapper.readValue(jsonRefresh, AuthClient.AuthResponse.class);
        assertEquals(testRefreshResponse, deserialized);
    }

    @Test
    public void eventDeserialize() throws JsonProcessingException {
        var eventJson = """
                  {
                  "aspect_type": "create",
                  "event_time": 1549560669,
                  "object_id": 1000000000,
                  "object_type": "activity",
                  "owner_id": 9999999,
                  "subscription_id": 999999
                }""";
        var deserialized = mapper.readValue(eventJson, Event.class);
        System.out.println(deserialized);
    }
}

