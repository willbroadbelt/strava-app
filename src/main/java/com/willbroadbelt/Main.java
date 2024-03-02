package com.willbroadbelt;

import org.openapitools.client.ApiClient;
import org.openapitools.client.api.ActivitiesApi;
import com.willbroadbelt.strava.auth.AuthClient;

import java.io.IOException;


/** Can use this to set up the webhook to a given URL - say if the Server has been set to run from a new machine,
 *  and you need to delete your old Strava subscription and create a new one to link it.
 *  **/
public class Main {

    private static String clientId;
    private static String clientSecret;
    private static final Long userId = 104989871L; // The User Ids are publicly available

    public static void main(String[] args) throws IOException {
        clientId = args[0];
        clientSecret = args[1];
        String callbackUrl = args[2];
        String verifyToken = args[3];

//        WebhookClient webhookClient = new WebhookClient(clientId, clientSecret);
//        //webhookClient.deleteSubscription("255211");
//        webhookClient.createSubscription(callbackUrl, verifyToken);
//        webhookClient.getSubscription()
//                .forEach(m -> m
//                        .forEach((k, v) -> System.out.println(k + " " + v)));

//
//        System.out.println(System.currentTimeMillis()/1000);
//
        var apiClient = getApiClient();
        var activitiesApi = new ActivitiesApi(apiClient);
        var act = activitiesApi.getActivityById(10820330547L, false);
        System.out.println(act);




//        var now = LocalDate.now();
//        var from = now.minusWeeks(6);
//        var bestPower = PowerUtil.buildPowerMapFromHistory(from, apiClient);
//        System.out.println(bestPower);

    }

    private static ApiClient getApiClient() {
        AuthClient authClient = new AuthClient(clientId, clientSecret);
        //AthleteDatabaseCouchbase athleteDb = new AthleteDatabaseCouchbase(authClient, "localhost", "Administrator", "password");
        //var athlete = athleteDb.getAthleteById(userId);

        ApiClient apiClient = new ApiClient();
        apiClient.setAccessToken("4c3f2cac8dde9b8f964905ce739c73abdbac9fcb");//athlete.accessToken());
        return apiClient;
    }
}

