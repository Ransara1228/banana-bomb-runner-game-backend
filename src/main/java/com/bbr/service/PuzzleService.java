package com.bbr.service;

import com.bbr.client.BananaClient;
import com.bbr.dto.AnswerRequest;
import com.bbr.dto.AnswerResponse;
import com.bbr.dto.LevelDto;
import com.bbr.dto.PuzzleResponse;
import com.bbr.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** The level gate: a Banana API puzzle at the finish line. */
@Service
public class PuzzleService {

    /** A puzzle waiting for an answer. The solution never leaves the server. */
    private record Pending(Long userId, int solution, int level, int seconds, Instant deadline) {}

    static final int EXTRA_SECONDS = 2;   // for the image to load and the network

    private final BananaClient banana;
    private final LevelService levels;
    private final ProgressService progress;
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();   // puzzleId -> puzzle

    public PuzzleService(BananaClient banana, LevelService levels, ProgressService progress) {
        this.banana = banana;
        this.levels = levels;
        this.progress = progress;
    }

    public PuzzleResponse newPuzzle(Long userId, int level) {
        LevelDto lv = levels.get(level);
        progress.checkUnlocked(userId, level);
        pending.values().removeIf(p -> p.deadline().isBefore(Instant.now().minusSeconds(60)));   // old, unanswered
        BananaClient.BananaPuzzle b = banana.fetch();
        String id = UUID.randomUUID().toString();
        Instant deadline = Instant.now().plusSeconds(lv.puzzleTime() + EXTRA_SECONDS);
        pending.put(id, new Pending(userId, b.solution(), level, lv.puzzleTime(), deadline));
        return new PuzzleResponse(id, b.question(), lv.puzzleTime());
    }

    public AnswerResponse answer(Long userId, AnswerRequest req) {
        Pending p = pending.remove(req.puzzleId());          // each puzzle can be answered once
        if (p == null || !p.userId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle not found");
        }
        long left = Duration.between(Instant.now(), p.deadline()).toSeconds();   // includes the extra seconds
        boolean timeUp = left < 0;
        boolean correct = !timeUp && p.solution() == req.answer();
        if (!correct) {
            return new AnswerResponse(false, timeUp, 0, progress.get(userId).highestLevel());
        }
        int secondsLeft = (int) Math.min(Math.max(left, 0), p.seconds());
        int bonus = 200 * p.level() + 20 * secondsLeft;
        UserResponse u = progress.unlockNext(userId, p.level());   // the server unlocks, not the browser
        return new AnswerResponse(true, false, bonus, u.highestLevel());
    }
}
