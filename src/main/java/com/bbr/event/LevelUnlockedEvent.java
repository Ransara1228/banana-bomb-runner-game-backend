package com.bbr.event;

/** Published when a player really unlocks a new level (not when they replay an old one). */
public record LevelUnlockedEvent(Long userId, int newLevel) {}
