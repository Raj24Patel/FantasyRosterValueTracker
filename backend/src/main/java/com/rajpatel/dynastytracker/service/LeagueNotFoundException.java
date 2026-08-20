package com.rajpatel.dynastytracker.service;

public class LeagueNotFoundException extends RuntimeException {

    public LeagueNotFoundException(String leagueId) {
        super("League " + leagueId + " is not tracked");
    }
}
