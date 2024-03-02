package com.willbroadbelt.db;

import com.willbroadbelt.strava.auth.AuthClient;
import com.willbroadbelt.strava.model.AthleteRecord;

import java.util.HashMap;
import java.util.logging.Logger;


/**
 * Simple local collection implementation of AthleteDatabase
 * To be used for testing only, records are lost on restart
 */
public class AthleteDatabaseLocal implements AthleteDatabase {

    private final Logger logger = Logger.getLogger(AthleteDatabaseLocal.class.getName());

    private static final HashMap<Long, AthleteRecord> records = new HashMap<>();

    private final AuthClient authClient;

    public AthleteDatabaseLocal(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public void insertAthlete(AthleteRecord athlete) {
        logger.fine("Inserting athlete " + athlete.athleteId());
        records.put(athlete.athleteId(), athlete);
    }

    @Override
    public AthleteRecord getAthleteById(Long athleteId) {
        logger.fine("Getting athlete " + athleteId);
        var athleteRecord = records.get(athleteId);

        if (athleteRecord.expiresAt() > (System.currentTimeMillis()/1000)) {
            logger.fine("Not refreshing token");
            return athleteRecord;
        }

        logger.fine("Refreshing athlete token");

        var refreshedAthlete = authClient.refreshToken(athleteRecord.refreshToken());

        if (refreshedAthlete == null) {
            logger.fine("Refreshing athlete token failed");
            return athleteRecord;
        }

        var updatedAthleteRecord = new AthleteRecord(athleteId,
                athleteRecord.scopes(),
                refreshedAthlete.accessToken(),
                refreshedAthlete.refreshToken(),
                refreshedAthlete.expiresAt());
        updateAthlete(updatedAthleteRecord.athleteId(), updatedAthleteRecord);

        return updatedAthleteRecord;
    }

    @Override
    public void updateAthlete(Long athleteId, AthleteRecord athlete) {
        logger.fine("Updating athlete: " + athlete.athleteId());
        records.put(athleteId, athlete);
    }

    @Override
    public void deleteAthlete(Long athleteId) {
        logger.fine("Removing athlete: " + athleteId);
        records.remove(athleteId);
    }
}
