package com.bbr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@Email @NotBlank String email,
                              @NotBlank @Size(max = 20) String username,
                              @Size(min = 6) String password) {}
