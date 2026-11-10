package com.bbr.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(@NotBlank String puzzleId, int answer) {}
