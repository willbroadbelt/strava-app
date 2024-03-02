package com.willbroadbelt.server.actions;

import org.openapitools.client.ApiClient;
import org.openapitools.client.api.ActivitiesApi;
import org.openapitools.client.model.DetailedActivity;
import org.openapitools.client.model.UpdatableActivity;
import com.willbroadbelt.strava.model.Event;

import java.util.*;
import java.util.logging.Logger;

/**
 * Simple example event action that adds a Hello message to the activity description
 */
public class HelloAction extends EventAction {

    ActivitiesApi activitiesApi;
    private final Logger logger = Logger.getLogger(HelloAction.class.getName());


    public HelloAction() {
        this.requiredAspectTypes = Arrays.asList(Event.AspectType.CREATE, Event.AspectType.UPDATE);
    }

    @Override
    public void doAction(Event event, DetailedActivity detailedActivity) {
        logger.info("Updating activity");
        var updatedActivity = new UpdatableActivity();
        var currentDescription = Objects.requireNonNullElse(detailedActivity.getDescription(), "");
        updatedActivity.setDescription(currentDescription.concat("\nHello there!"));

        activitiesApi.updateActivityById(event.objectId(), updatedActivity);
        logger.info("Added hello to activity");

    }

    @Override
    public void setApiClient(ApiClient apiClient) {
        super.setApiClient(apiClient);
        activitiesApi = new ActivitiesApi(this.apiClient);
    }

}
