package com.willbroadbelt.db;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.willbroadbelt.strava.auth.AuthClient;
import com.willbroadbelt.strava.model.AthleteRecord;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class AthleteDatabaseCouchbase implements AthleteDatabase{

    private final Cluster cluster;
    private final Bucket bucket;
    private final AuthClient authClient;
    private final Collection recordsCollection;


    public AthleteDatabaseCouchbase(AuthClient authClient, String hostname, String user, String password) {
        this.authClient = authClient;
        cluster = Cluster.connect(hostname, user, password);
        //bucket = cluster.bucket("athletes");
        bucket = cluster.bucket("default");
        bucket.waitUntilReady(Duration.ofSeconds(3));
        recordsCollection = bucket.defaultScope().collection("records");
    }

    @Override
    public void insertAthlete(AthleteRecord athlete) {
        bucket.defaultCollection().insert(String.valueOf(athlete.athleteId()), athlete);
    }

    @Override
    public AthleteRecord getAthleteById(Long athleteId) {
        var result = bucket.defaultCollection().get(String.valueOf(athleteId));
        var athleteRecord = result.contentAs(AthleteRecord.class);

        if (athleteRecord.expiresAt() > (System.currentTimeMillis()/1000)) {
            return athleteRecord;
        }

        var refreshedAthlete = authClient.refreshToken(athleteRecord.refreshToken());

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
        bucket.defaultCollection().upsert(String.valueOf(athlete.athleteId()), athlete);
    }

    @Override
    public void deleteAthlete(Long athleteId) {
        bucket.defaultCollection().remove(String.valueOf(athleteId));
    }


    //TODO: Make the document much richer
    //      - Have All-Time PRs and PRs for each year
    //      - Each Time entry has 3 entries for 1st, 2nd, 3rd best, so can say "2nd best 5s power this year!"
    //      - Each Time entry has the date and activity ID, in addition to the Watts value
    public Map<Integer, Integer> getPowerRecords(Long athleteId) {
        try {
            return recordsCollection.get(athleteId.toString()).contentAs(HashMap.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    public void updatePowerRecords(Long athleteId, Map<Integer, Integer> rideBestPowers) {
        recordsCollection.upsert(athleteId.toString(), rideBestPowers);
    }
}
