package com.willbroadbelt.strava.model;

import java.util.List;

/**
 * Record of Athlete auth details to store and use for requests
 * @param athleteId
 * @param scopes
 * @param accessToken
 * @param refreshToken
 * @param expiresAt
 */
public record AthleteRecord (
        Long athleteId,
        List<Scope> scopes,
        String accessToken,
        String refreshToken,
        Long expiresAt
) {
     public enum Scope {
         READ,
         READ_ALL,
         PROFILE_READ_ALL,
         PROFILE_WRITE,
         ACTIVITY_READ,
         ACTIVITY_READ_ALL,
         ACTIVITY_WRITE;

         public static Scope from(final String scope) {
             if (scope.equalsIgnoreCase("read")) {
                 return READ;
             } else if (scope.equalsIgnoreCase("read_all")) {
                 return READ_ALL;
             } else if (scope.equalsIgnoreCase("profile:read_all")) {
                 return PROFILE_READ_ALL;
             } else if (scope.equalsIgnoreCase("profile:write")) {
                 return PROFILE_WRITE;
             } else if(scope.equalsIgnoreCase("activity:read")) {
                 return ACTIVITY_READ;
             } else if (scope.equalsIgnoreCase("activity:read_all")) {
                 return ACTIVITY_READ_ALL;
             } else if (scope.equalsIgnoreCase("activity:write")) {
                 return ACTIVITY_WRITE;
             }

             return null;
         }
     }
}
