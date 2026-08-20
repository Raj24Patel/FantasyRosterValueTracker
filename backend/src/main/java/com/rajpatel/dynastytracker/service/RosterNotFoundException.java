package com.rajpatel.dynastytracker.service;

/** Thrown when a roster ID doesn't exist; translated to a 404 by {@code GlobalExceptionHandler}. */
public class RosterNotFoundException extends RuntimeException {

    /** @param rosterId the roster ID that was requested */
    public RosterNotFoundException(Long rosterId) {
        super("Roster " + rosterId + " not found");
    }
}
