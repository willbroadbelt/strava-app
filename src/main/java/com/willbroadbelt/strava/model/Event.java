package com.willbroadbelt.strava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

public record Event (
        @JsonProperty("object_type") ObjectType objectType,
        @JsonProperty("object_id") Long objectId,
        @JsonProperty("aspect_type") AspectType aspectType,
        Map<String, String> updates,
        @JsonProperty("owner_id")Long ownerId,
        @JsonProperty("subscription_id")int subscriptionId,
        @JsonProperty("event_time")long eventTime
) {

    public enum ObjectType {
        ACTIVITY,
        ATHLETE;

        @JsonValue
        public String toLowerCase() {
            return toString().toLowerCase();
        }
    }

    public enum AspectType {
        CREATE,
        UPDATE,
        DELETE;

        @JsonValue
        public String toLowerCase() {
            return toString().toLowerCase();
        }
    }
}
