package com.willbroadbelt.db;

import com.willbroadbelt.strava.model.AthleteRecord;

public interface AthleteDatabase {

    void insertAthlete(AthleteRecord athlete);

    AthleteRecord getAthleteById(Long athleteId);

    void updateAthlete(Long athleteId, AthleteRecord athlete);

    void deleteAthlete(Long athleteId);

}
