package com.rajpatel.dynastytracker.web;

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

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LeagueNotFoundException.class)
    ProblemDetail handleLeagueNotFound(LeagueNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("League not found");
        return problem;
    }

    @ExceptionHandler(RosterNotFoundException.class)
    ProblemDetail handleRosterNotFound(RosterNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Roster not found");
        return problem;
    }

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Request body failed validation");
        problem.setTitle("Invalid request");
        return problem;
    }
}
