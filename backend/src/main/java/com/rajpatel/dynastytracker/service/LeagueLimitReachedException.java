package com.rajpatel.dynastytracker.service;

/**
 * Thrown when adding a league would exceed the configured cap. There are no
 * accounts, so anyone with the URL can add leagues — the cap keeps a public
 * deployment from being filled up. Translated to a 409 by
 * {@code GlobalExceptionHandler}.
 */
public class LeagueLimitReachedException extends RuntimeException {

    /** @param limit the configured maximum number of tracked leagues */
    public LeagueLimitReachedException(int limit) {
        super("This tracker is already following its maximum of " + limit
                + (limit == 1 ? " league." : " leagues.") + " Remove one before adding another.");
    }
}
