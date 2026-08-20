package com.rajpatel.dynastytracker.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for {@code POST /api/leagues}. */
public record AddLeagueRequest(@NotBlank String sleeperLeagueId) {
}
