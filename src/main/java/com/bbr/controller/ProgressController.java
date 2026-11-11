package com.bbr.controller;

import com.bbr.dto.UserResponse;
import com.bbr.service.ProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progress;

    public ProgressController(ProgressService progress) {
        this.progress = progress;
    }

    /** Who is logged in and which levels are unlocked. Needs the JWT cookie. There is no POST: only the gate puzzle unlocks. */
    @GetMapping
    public UserResponse get(@RequestAttribute("userId") Long userId) {
        return progress.get(userId);
    }
}
