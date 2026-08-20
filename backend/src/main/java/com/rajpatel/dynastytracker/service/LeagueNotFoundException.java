package com.rajpatel.dynastytracker.service;

/** Thrown when a league ID isn't tracked; translated to a 404 by {@code GlobalExceptionHandler}. */
public class LeagueNotFoundException extends RuntimeException {

    /** @param leagueId the untracked league ID that was requested */
    public LeagueNotFoundException(String leagueId) {
        super("League " + leagueId + " is not tracked");
    }
}
