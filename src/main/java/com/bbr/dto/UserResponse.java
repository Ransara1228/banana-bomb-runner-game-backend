package com.bbr.dto;

import com.bbr.entity.User;

/** What the browser may see about a user (no email, no password hash). */
public record UserResponse(String username, int highestLevel, int bestScore) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getUsername(), u.getHighestLevel(), u.getBestScore());
    }
}
