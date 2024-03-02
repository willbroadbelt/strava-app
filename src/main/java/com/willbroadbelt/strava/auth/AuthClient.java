package com.willbroadbelt.strava.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openapitools.client.ApiClient;
import org.openapitools.client.model.DetailedAthlete;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: Add logging

/**
 * A HTTP Client to manage authorisation of Strava users for a Strava 'client' application
 */
public class AuthClient {

    private static final String STRAVA_AUTH_PATH = "https://www.strava.com/oauth";
    private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
    private static final String AUTH_CODE_GRANT_TYPE = "authorization_code";

    private static String clientId;
    private static String clientSecret;
    private final ApiClient apiClient = new ApiClient();

    private final Logger logger = Logger.getLogger(AuthClient.class.getName());


    public AuthClient(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        apiClient.setBasePath(STRAVA_AUTH_PATH);
    }

    //@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)// <- does not currently work
    //Have to add the JsonProperty to fields (of more than one word) for records, will be fixed in Jackson 2.15
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuthResponse(
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_at") Long expiresAt,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            DetailedAthlete athlete //Returned upon initialising only
    ){}


    public AuthResponse initialiseUser(String code) {
        MultiValueMap<String, String> initialQueryParams = new LinkedMultiValueMap();
        initialQueryParams.add("grant_type", AUTH_CODE_GRANT_TYPE);
        initialQueryParams.add("code", code);

        ParameterizedTypeReference<AuthResponse> localReturnType = new ParameterizedTypeReference<>() {};

        var response = invokeAPI("/token", HttpMethod.POST, initialQueryParams, localReturnType);

        var body = response.getBody();
        return body;
    }

    @Nullable
    public AuthResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> refreshQueryParams = new LinkedMultiValueMap();
        refreshQueryParams.add("grant_type", REFRESH_TOKEN_GRANT_TYPE);
        refreshQueryParams.add("refresh_token", refreshToken);

        ParameterizedTypeReference<AuthResponse> localReturnType = new ParameterizedTypeReference<>() {};

        var response = invokeAPI("/token", HttpMethod.POST, refreshQueryParams, localReturnType);

        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.log(Level.WARNING, "Refresh token failed");
            logger.fine(response.toString());
            return null;
        }

        return response.getBody();
    }

    public void deauthoriseUser(String accessToken) {
        MultiValueMap<String, String> refreshQueryParams = new LinkedMultiValueMap();
        refreshQueryParams.add("access_token", accessToken);

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<>() {};

        invokeAPI("/deauthorize", HttpMethod.POST, refreshQueryParams, localReturnType);
    }

    private <T> ResponseEntity<T> invokeAPI(String path, HttpMethod method, MultiValueMap<String, String> queryParams, ParameterizedTypeReference<T> returnType) {
        queryParams.add("client_id", clientId);
        queryParams.add("client_secret", clientSecret);
        return apiClient.invokeAPI(path, method, Collections.emptyMap(), queryParams, null,  new HttpHeaders(), new LinkedMultiValueMap<>(), null, null, null, new String[]{}, returnType);
    }

}
