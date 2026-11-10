package com.bbr.controller;

import com.bbr.dto.AnswerRequest;
import com.bbr.dto.AnswerResponse;
import com.bbr.dto.PuzzleResponse;
import com.bbr.service.PuzzleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/puzzle")
public class PuzzleController {

    private final PuzzleService puzzles;

    public PuzzleController(PuzzleService puzzles) {
        this.puzzles = puzzles;
    }

    @GetMapping
    public PuzzleResponse newPuzzle(@RequestAttribute("userId") Long userId, @RequestParam("level") int level) {
        return puzzles.newPuzzle(userId, level);
    }

    @PostMapping("/answer")
    public AnswerResponse answer(@RequestAttribute("userId") Long userId, @Valid @RequestBody AnswerRequest req) {
        return puzzles.answer(userId, req);
    }
}
