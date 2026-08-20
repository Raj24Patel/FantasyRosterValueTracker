package com.rajpatel.dynastytracker.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddLeagueRequest(@NotBlank String sleeperLeagueId) {
}
