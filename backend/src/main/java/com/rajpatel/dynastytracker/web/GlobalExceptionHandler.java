package com.rajpatel.dynastytracker.web;

import com.rajpatel.dynastytracker.service.LeagueLimitReachedException;
import com.rajpatel.dynastytracker.service.LeagueNotFoundException;
import com.rajpatel.dynastytracker.service.RosterNotFoundException;
import com.rajpatel.dynastytracker.sleeper.SleeperApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates service-layer exceptions into RFC 7807 {@link ProblemDetail}
 * responses, so clients get a 404/503/400 with a human-readable message
 * instead of a raw stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * @param e the untracked-league error
     * @return a 404 problem detail describing which league wasn't found
     */
    @ExceptionHandler(LeagueNotFoundException.class)
    ProblemDetail handleLeagueNotFound(LeagueNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("League not found");
        return problem;
    }

    /**
     * @param e the missing-roster error
     * @return a 404 problem detail describing which roster wasn't found
     */
    @ExceptionHandler(RosterNotFoundException.class)
    ProblemDetail handleRosterNotFound(RosterNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Roster not found");
        return problem;
    }

    /**
     * @param e the league-cap error
     * @return a 409 problem detail telling the user to remove a league first
     */
    @ExceptionHandler(LeagueLimitReachedException.class)
    ProblemDetail handleLeagueLimit(LeagueLimitReachedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("League limit reached");
        return problem;
    }

    /**
     * @param e the Sleeper call failure (unknown league vs. genuine outage are handled differently)
     * @return a 404 if Sleeper said the league doesn't exist, otherwise a 503 (logged server-side)
     */
    @ExceptionHandler(SleeperApiException.class)
    ProblemDetail handleSleeperFailure(SleeperApiException e) {
        if (e.isNotFound()) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                    "Sleeper doesn't know that league id. Double-check it and try again.");
            problem.setTitle("Unknown Sleeper league");
            return problem;
        }
        log.error("Sleeper API failure", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Sleeper is unavailable right now, so league data can't be fetched. Try again in a bit.");
        problem.setTitle("Sleeper unavailable");
        return problem;
    }

    /**
     * @param e the bean-validation failure (e.g. blank sleeperLeagueId)
     * @return a 400 problem detail
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Request body failed validation");
        problem.setTitle("Invalid request");
        return problem;
    }
}
