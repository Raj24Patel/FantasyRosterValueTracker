package com.rajpatel.dynastytracker.sleeper;

public class SleeperApiException extends RuntimeException {

    private final int status; // 0 when Sleeper was unreachable (timeout / connection refused)

    public SleeperApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public SleeperApiException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public boolean isNotFound() {
        return status == 404;
    }
}
