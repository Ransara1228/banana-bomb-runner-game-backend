package com.bbr.controller;

import com.bbr.dto.LeaderboardEntry;
import com.bbr.dto.ScoreRequest;
import com.bbr.dto.UserResponse;
import com.bbr.service.ScoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ScoreController {

    private final ScoreService scores;

    public ScoreController(ScoreService scores) {
        this.scores = scores;
    }

    @PostMapping("/api/scores")
    public UserResponse save(@RequestAttribute("userId") Long userId, @Valid @RequestBody ScoreRequest req) {
        return scores.save(userId, req);
    }

    @GetMapping("/api/leaderboard")
    public List<LeaderboardEntry> leaderboard() {
        return scores.leaderboard();
    }
}
