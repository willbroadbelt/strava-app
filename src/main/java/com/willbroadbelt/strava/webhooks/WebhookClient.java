package com.willbroadbelt.strava.webhooks;

import org.openapitools.client.ApiClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A Http CRUD client for managing webhooks on Strava.
 * Generally wouldn't use in an automated context
 */
public class WebhookClient {

    private static String clientId;
    private static String clientSecret;

    private final ApiClient apiClient = new ApiClient();

    public WebhookClient(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void createSubscription(String callbackUrl, String verifyToken) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap();
        queryParams.add("callback_url", callbackUrl);
        queryParams.add("verify_token", verifyToken);

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<>() {};

        var response = invokeAPI("/push_subscriptions", HttpMethod.POST, Collections.emptyMap(), queryParams, localReturnType);
    }

    public ArrayList<Map<String, Object>> getSubscription() {
        ParameterizedTypeReference<ArrayList<Map<String, Object>>> localReturnType = new ParameterizedTypeReference<>() {};

        var response = invokeAPI("/push_subscriptions", HttpMethod.GET, Collections.emptyMap(), new LinkedMultiValueMap<>(), localReturnType);
        var body = response.getBody();
        return body;
    }


    public void deleteSubscription(String id) {
        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<>() {};

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);

        var response = invokeAPI("/push_subscriptions/{id}", HttpMethod.DELETE, uriVariables, new LinkedMultiValueMap<>(), localReturnType);
        if (response.getStatusCodeValue() != 204) throw new RestClientException(String.format("Unexpected HTTP error code returned for delete: %i", response.getStatusCodeValue()));
    }

    private <T> ResponseEntity<T> invokeAPI(String path, HttpMethod method, Map<String, Object> pathParams, MultiValueMap<String, String> queryParams, ParameterizedTypeReference<T> returnType) {
        queryParams.add("client_id", clientId);
        queryParams.add("client_secret", clientSecret);
        return apiClient.invokeAPI(path, method, pathParams, queryParams, null,  new HttpHeaders(), new LinkedMultiValueMap<>(), null, null, null, new String[]{}, returnType);
    }
}
