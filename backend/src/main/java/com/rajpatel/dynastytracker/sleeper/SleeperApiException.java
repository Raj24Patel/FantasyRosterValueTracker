package com.rajpatel.dynastytracker.sleeper;

/** Thrown when a Sleeper API call fails after retries; carries the HTTP status for the web layer to translate. */
public class SleeperApiException extends RuntimeException {

    private final int status; // 0 when Sleeper was unreachable (timeout / connection refused)

    /**
     * @param message human-readable description of what failed
     * @param status the HTTP status Sleeper returned, or 0 if it never responded
     */
    public SleeperApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    /**
     * @param message human-readable description of what failed
     * @param status the HTTP status Sleeper returned, or 0 if it never responded
     * @param cause the underlying exception (e.g. a network timeout)
     */
    public SleeperApiException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /** @return the HTTP status Sleeper returned, or 0 if unreachable */
    public int getStatus() {
        return status;
    }

    /** @return true if Sleeper reported the resource didn't exist (e.g. unknown league id) */
    public boolean isNotFound() {
        return status == 404;
    }
}
