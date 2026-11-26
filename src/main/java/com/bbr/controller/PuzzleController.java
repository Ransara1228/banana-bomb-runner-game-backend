package com.bbr.controller;

import com.bbr.dto.AnswerRequest;
import com.bbr.dto.AnswerResponse;
import com.bbr.dto.PuzzleResponse;
import com.bbr.service.PuzzleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Gate puzzles. Guests (no userId) may play levels 1-2; AccessPolicy checks the rest. */
@RestController
@RequestMapping("/api/puzzle")
public class PuzzleController {

    private final PuzzleService puzzles;

    public PuzzleController(PuzzleService puzzles) {
        this.puzzles = puzzles;
    }

    @GetMapping
    public PuzzleResponse newPuzzle(@RequestAttribute(value = "userId", required = false) Long userId, @RequestParam("level") int level) {
        return puzzles.newPuzzle(userId, level);
    }

    @PostMapping("/answer")
    public AnswerResponse answer(@RequestAttribute(value = "userId", required = false) Long userId, @Valid @RequestBody AnswerRequest req) {
        return puzzles.answer(userId, req);
    }
}
