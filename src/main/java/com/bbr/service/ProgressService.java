package com.bbr.service;

import com.bbr.dto.UserResponse;
import com.bbr.entity.User;
import com.bbr.event.LevelUnlockedEvent;
import com.bbr.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProgressService {

    private final UserRepository users;
    private final LevelService levels;
    private final ApplicationEventPublisher events;

    public ProgressService(UserRepository users, LevelService levels, ApplicationEventPublisher events) {
        this.users = users;
        this.levels = levels;
        this.events = events;
    }

    public UserResponse get(Long userId) {
        return UserResponse.from(find(userId));
    }

    /** Authorization: a logged-in user can only play levels they have unlocked. */
    public void checkUnlocked(Long userId, int level) {
        if (level > find(userId).getHighestLevel()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Level locked");
        }
    }

    /** Only the gate puzzle calls this, after a correct answer. The browser cannot unlock levels. */
    public UserResponse unlockNext(Long userId, int level) {
        User u = find(userId);
        if (level > u.getHighestLevel()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Level locked");
        }
        if (level == u.getHighestLevel() && level < levels.count()) {
            u.setHighestLevel(level + 1);
            users.save(u);
            events.publishEvent(new LevelUnlockedEvent(userId, level + 1));   // listeners react (history)
        }
        return UserResponse.from(u);
    }

    private User find(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in"));
    }
}
