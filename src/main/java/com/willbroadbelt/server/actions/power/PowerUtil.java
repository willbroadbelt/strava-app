package com.willbroadbelt.server.actions.power;

import org.openapitools.client.ApiClient;
import org.openapitools.client.api.ActivitiesApi;
import org.openapitools.client.api.StreamsApi;
import org.openapitools.client.model.ActivityType;
import org.openapitools.client.model.PowerStream;
import org.openapitools.client.model.StreamSet;
import org.openapitools.client.model.SummaryActivity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Functions to generate the best power for given lengths of time
 */
public class PowerUtil {

    private static final List<Integer> POWER_TIMES = List.of(1, 2, 5, 10, 20, 30, 60, 120, 300, 600, 1200, 1800, 3600, 3600 * 2, 3600 * 5);
    private static final List<String> REQUIRED_STREAM_KEYS = List.of("watts", "time");


    public static HashMap<Integer, Integer> calculatePowerMap(StreamSet streamSet) {
        var powerMap = new HashMap<Integer, Integer>();
        var powerStream = streamSet.getWatts();

        // Sanitise power stream
        removeNullValues(powerStream);
        addPausedTimePower(streamSet);

        for (int time: POWER_TIMES) {
            if (time > powerStream.getOriginalSize()){
                continue;
            }
            var maxPow = maxAvgSubarray(powerStream.getData(), time);
            powerMap.put(time, maxPow);
            System.out.println(time + " : " + maxPow);
        }

        return powerMap;
    }

    /**
     * Build best power map for the set user from ride activities from the given time till the current date
     * @param after
     * @param apiClient Must have the user access token already set
     * @return
     */
    public static HashMap<Integer, Integer> buildPowerMapFromHistory(LocalDate after, ApiClient apiClient) {
        var activitiesApi = new ActivitiesApi(apiClient);
        var streamsApi = new StreamsApi(apiClient);

        return callWithPageIndex(1, after, activitiesApi)
                .expand(pagedResponse -> pagedResponse.response.isEmpty() ? Mono.empty() : callWithPageIndex(pagedResponse.pageIndex + 1, after, activitiesApi))
                .map(pagedResponse -> pagedResponse.response)
                .flatMap(Flux::fromIterable)
                .filter(a -> a.getType() == ActivityType.RIDE || a.getType() == ActivityType.VIRTUALRIDE)
                .filter(SummaryActivity::getDeviceWatts)
                .map(SummaryActivity::getId)
                .map(id -> streamsApi.getActivityStreams(id, REQUIRED_STREAM_KEYS, true))
                .map(PowerUtil::calculatePowerMap)
                .reduce(PowerUtil::newBestPowers).block();

//        var activities = activitiesApi.getLoggedInAthleteActivities((Integer) null, (int) after.atStartOfDay().toEpochSecond(ZoneOffset.UTC), null, null);
//        return activities.stream()
//                .filter(a -> a.getType() == ActivityType.RIDE || a.getType() == ActivityType.VIRTUALRIDE)
//                .filter(a -> a.getDeviceWatts())
//                .map(SummaryActivity::getId)
//                .map(id -> streamsApi.getActivityStreams(id, List.of("watts", "time"), true))
//                .map(PowerUtil::calculatePowerMap)
//                .reduce(PowerUtil::newBestPowers).get();
    }

    private static Mono<PagedResponse<List<SummaryActivity>>> callWithPageIndex(int pageIdx, LocalDate after, ActivitiesApi api) {
        return  Mono.just((api.getLoggedInAthleteActivities(null, (int) after.atStartOfDay().toEpochSecond(ZoneOffset.UTC), pageIdx, null)))
                .map(response -> new PagedResponse<>(pageIdx, response));
    }

    static class PagedResponse<T> {
        int pageIndex;
        T response;

        public PagedResponse(int pageIdx, T response) {
            this.pageIndex = pageIdx;
            this.response = response;
        }
    }

    /**
     * Can get null values in the power stream - replace these with 0's
     */
    private static void removeNullValues(PowerStream powerStream) {
        powerStream.setData(powerStream.getData().stream()
                .map(x -> x == null ? 0 : x)
                .collect(Collectors.toList()));
    }

    /**
     * Add 0 Watt values into the power stream for the periods of time when the recording has been paused
     */
    private static void addPausedTimePower(StreamSet streamSet) {
        var timeStream = streamSet.getTime();
        var powerStream = streamSet.getWatts();

        int bound = timeStream.getOriginalSize() - 1;
        for (int i1 = 0; i1 < bound; i1++) {
            var val1 = timeStream.getData().get(i1);
            var val2 = timeStream.getData().get(i1 + 1);
            if (val2 - val1 != 1) {
                for (int j = 0; j < val2 - val1; j++) {
                    powerStream.getData().add(i1, 0);
                }
            }
        }
    }

    /**
     * Compare current best powers with a new rides best powers and return the updated bests
     */
    private static HashMap<Integer, Integer> newBestPowers(HashMap<Integer, Integer> newRidePower, HashMap<Integer, Integer> currentPRs) {
        for (int time: POWER_TIMES) {
            if (currentPRs.getOrDefault(time, 0) < newRidePower.getOrDefault(time, 0)) {
                currentPRs.put(time, newRidePower.get(time));
            }
        }
        return currentPRs;
    }

    private static int maxAvgSubarray(List<Integer> arr, int k) {
        int n = arr.size();

        int windowSum = 0;
        for (int i = 0; i < k; i++) {
            windowSum += arr.get(i);
        }
        int maxSum = windowSum;
        // Might be handy at some point to show when the best power was done?
//        int maxIndex = 0;

        // Slide the window and update the maximum sum
        for (int i = k; i < n; i++) {
            windowSum += arr.get(i) - arr.get(i - k);
            if (windowSum > maxSum) {
                maxSum = windowSum;
//                maxIndex = i - k + 1;
            }
        }
        return maxSum/k;
    }
}
