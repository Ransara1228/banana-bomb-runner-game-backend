package com.bbr.service;

import com.bbr.dto.LeaderboardEntry;
import com.bbr.dto.ScoreRequest;
import com.bbr.dto.UserResponse;
import com.bbr.entity.Score;
import com.bbr.entity.User;
import com.bbr.repository.ScoreRepository;
import com.bbr.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ScoreService {

    private final ScoreRepository scores;
    private final UserRepository users;

    public ScoreService(ScoreRepository scores, UserRepository users) {
        this.scores = scores;
        this.users = users;
    }

    /** Saves every finished level (history) and keeps the player's best score up to date. */
    public UserResponse save(Long userId, ScoreRequest req) {
        User u = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in"));
        Score s = new Score();
        s.setUserId(u.getId());
        s.setUsername(u.getUsername());
        s.setLevel(req.level());
        s.setPoints(req.points());
        scores.save(s);
        if (req.points() > u.getBestScore()) {
            u.setBestScore(req.points());
            users.save(u);
        }
        return UserResponse.from(u);
    }

    /** Top 10 players, each player once, by best score. */
    public List<LeaderboardEntry> leaderboard() {
        return users.findTop10ByBestScoreGreaterThanOrderByBestScoreDesc(0).stream()
                .map(u -> new LeaderboardEntry(u.getId(), u.getUsername(), u.getHighestLevel(), u.getBestScore()))
                .toList();
    }
}
