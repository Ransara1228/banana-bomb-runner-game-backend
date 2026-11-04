package com.bbr.controller;

import com.bbr.dto.LevelDto;
import com.bbr.service.LevelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LevelController {

    private final LevelService levels;

    public LevelController(LevelService levels) {
        this.levels = levels;
    }

    @GetMapping("/api/levels")
    public List<LevelDto> levels() {
        return levels.all();
    }
}