package com.bbr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ScoreRequest(@Min(1) @Max(5) int level, @Min(0) int points) {}
