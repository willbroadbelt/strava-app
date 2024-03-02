package com.willbroadbelt.server.actions;

import org.openapitools.client.ApiClient;
import org.openapitools.client.model.DetailedActivity;
import com.willbroadbelt.strava.model.AthleteRecord;
import com.willbroadbelt.strava.model.Event;

import java.util.List;
import java.util.Set;

/**
 * Action to do based on an incoming event received from Strava
 */
public abstract class EventAction {

    protected ApiClient apiClient;

    /**
     * Event Aspect types required for this action
     */
    protected List<Event.AspectType> requiredAspectTypes;

    /**
     * Athlete scopes required for this action
     */
    protected Set<AthleteRecord.Scope> requiredScopes;

//    /**
//     * Event object type that this action will act on
//     */
//    protected Event.ObjectType objectType;

    public abstract void doAction(Event event, DetailedActivity detailedUpdated);

//    public record ActionParams(Event event, Object detailedUpdated) {}

    public void setApiClient(ApiClient client) {
        apiClient = client;
    }

    public List<Event.AspectType> getRequiredAspectTypes() {
        return requiredAspectTypes;
    }

    public Set<AthleteRecord.Scope> getRequiredScopes() {
        return requiredScopes;
    }

}
