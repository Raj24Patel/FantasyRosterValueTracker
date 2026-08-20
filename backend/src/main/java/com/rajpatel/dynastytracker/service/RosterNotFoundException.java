package com.rajpatel.dynastytracker.service;

public class RosterNotFoundException extends RuntimeException {

    public RosterNotFoundException(Long rosterId) {
        super("Roster " + rosterId + " not found");
    }
}
