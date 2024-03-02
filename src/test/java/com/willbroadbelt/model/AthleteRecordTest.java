package com.willbroadbelt.model;

import org.junit.Test;
import com.willbroadbelt.strava.model.AthleteRecord;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static com.willbroadbelt.strava.model.AthleteRecord.Scope.*;

public class AthleteRecordTest {

    @Test
    public void ScopeParseTest() {
        var scopesString = "read,activity:write,activity:read_all,profile:read_all,read_all,profile:write,activity:read";
        var convertedScopes = Arrays.stream(scopesString.split(","))
                .map(s -> AthleteRecord.Scope.from(s))
                .collect(Collectors.toList());

        var scopeList = List.of(
                READ,
                READ_ALL,
                PROFILE_READ_ALL,
                PROFILE_WRITE,
                ACTIVITY_READ,
                ACTIVITY_READ_ALL,
                ACTIVITY_WRITE);

        assertTrue(scopeList.containsAll(convertedScopes));
    }
}
