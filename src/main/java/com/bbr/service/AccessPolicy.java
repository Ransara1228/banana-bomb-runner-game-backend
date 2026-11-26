/*
 * AI-assisted: Claude (Anthropic), 2026-11-21.
 * Used for: first version of this file. Change it, test it and make sure you understand it before submitting.
 */
package com.bbr.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Authorization: who may play which level.
 * Authentication (the JWT cookie) says WHO the player is; this class decides WHAT they are allowed to do.
 *   guest (no login)  -> levels 1..FREE_LEVELS only
 *   logged-in player  -> the free levels + every level they have unlocked
 */
@Service
public class AccessPolicy {

    /** Levels 1 and 2 can be played without logging in. */
    public static final int FREE_LEVELS = 2;

    private final ProgressService progress;

    public AccessPolicy(ProgressService progress) {
        this.progress = progress;
    }

    public static boolean isFree(int level) {
        return level <= FREE_LEVELS;
    }

    /** userId is null for a guest. Throws 401 (log in first) or 403 (level locked). */
    public void checkCanPlay(Long userId, int level) {
        if (userId == null) {
            if (!isFree(level)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in to play level " + level);
            }
            return;
        }
        progress.checkUnlocked(userId, level);
    }
}
