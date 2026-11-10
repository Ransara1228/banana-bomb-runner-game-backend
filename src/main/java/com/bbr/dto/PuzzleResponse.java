package com.bbr.dto;

/** Sent to the browser. There is no solution field on purpose. */
public record PuzzleResponse(String puzzleId, String image, int timeLimit) {}
