package com.willbroadbelt.server.actions;

import com.willbroadbelt.db.AthleteDatabaseCouchbase;
import org.openapitools.client.api.ActivitiesApi;
import org.openapitools.client.api.StreamsApi;
import org.openapitools.client.model.*;
import com.willbroadbelt.strava.model.Event;

import com.willbroadbelt.server.actions.power.PowerUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BestPowerAction extends EventAction {

    ActivitiesApi activitiesApi;
    private final AthleteDatabaseCouchbase db;
    private Logger logger = Logger.getLogger(HelloAction.class.getName());
    private static final List<Integer> powerWindows = List.of(1, 2, 5, 10, 20, 30, 60, 120, 300, 600, 1200, 1800, 3600, 3600 * 2, 3600 * 5);

    public BestPowerAction(AthleteDatabaseCouchbase db){
        this.requiredAspectTypes = Arrays.asList(Event.AspectType.CREATE, Event.AspectType.UPDATE);
        this.db = db;
    }

    @Override
    public void doAction(Event event, DetailedActivity detailedActivity) {
        logger.info("Updating activity");
        // Rides only
        if(detailedActivity.getType() != ActivityType.RIDE || detailedActivity.getType() != ActivityType.VIRTUALRIDE){
            return;
        }

        // Power numbers from a device
        if (Boolean.FALSE.equals(detailedActivity.getDeviceWatts())) {
            return;
        }

        var streamsApi = new StreamsApi(apiClient);
        var streamSet = streamsApi.getActivityStreams(event.objectId(), List.of("watts", "time"), true);

        var powerMap = PowerUtil.calculatePowerMap(streamSet);

        var newPrs = newBestPowers(powerMap, event.ownerId());
        if (newPrs.isEmpty()){
            logger.info("No new PRs");
            return;
        }

        var powerDescription = powerDescription(newPrs);

        var updatedActivity = new UpdatableActivity();
        var currentDescription = detailedActivity.getDescription();
        updatedActivity.setDescription(currentDescription.concat(powerDescription));
        activitiesApi.updateActivityById(event.objectId(), updatedActivity);
        logger.info("Added best power to activity");
    }

    private String powerDescription(HashMap<Integer, Integer> newBests) {
        var str = "Congrats! You broke " + newBests.size() + " power records this ride!\n";
        for (Map.Entry<Integer, Integer> entry : newBests.entrySet()) {
            // Medal emojis - https://emojipedia.org/1st-place-medal#emoji
            str = str.concat("\uD83E\uDD47 " + convertSecondsToHumanTime(entry.getKey()) + " : " + entry.getValue() +  "W\n");
        }
        return str;
    }

    /**
     * Compare this ride's best powers to the athlete's historical PRs and return
     * Update the PRs, and return the PRs that have been broken this ride
     */
    private HashMap<Integer, Integer> newBestPowers(HashMap<Integer, Integer> newRidePower, Long athleteId) {
        // Get the power map from the DB
        var currentPRs = db.getPowerRecords(athleteId);
        // Compare it to the new ride
        var beatenPRs = new HashMap<Integer, Integer>();
        for (int time: powerWindows) {
            if (currentPRs.getOrDefault(time, 0) < newRidePower.getOrDefault(time, 0)) {
                beatenPRs.put(time, newRidePower.get(time));
                currentPRs.put(time, newRidePower.get(time));
            }
        }
        // Update the power map if any better and push back to DB
        db.updatePowerRecords(athleteId, currentPRs);
        // Return the power map containing only the new best powers, if any
        return beatenPRs;
    }


    private String convertSecondsToHumanTime(int time) {
        if (time < 60) {
            return time + "s";
        }
        if (time < 3600) {
            return time/60 + "m";
        }
        return time/(3600) + "h";
    }
}
