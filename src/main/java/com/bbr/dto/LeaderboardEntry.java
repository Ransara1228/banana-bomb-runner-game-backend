package com.bbr.dto;

/** One player on the leaderboard: their highest unlocked level and best score. */
public record LeaderboardEntry(Long id, String username, int level, int points) {}
