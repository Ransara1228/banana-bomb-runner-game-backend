package com.bbr.dto;

/** Result of a gate puzzle. The bonus and the unlock are worked out on the server. */
public record AnswerResponse(boolean correct, boolean timeUp, int bonus, int highestLevel) {}
