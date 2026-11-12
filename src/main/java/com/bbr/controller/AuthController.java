/*
 * AI-assisted: Claude (Anthropic), 2026-11-07.
 */
package com.bbr.controller;

import com.bbr.dto.*;
import com.bbr.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final boolean secureCookie;

    public AuthController(AuthService auth, @Value("${app.cookie.secure:false}") boolean secureCookie) {
        this.auth = auth;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest req) {
        auth.register(req);
        return new MessageResponse("Registered. Now log in.");
    }

    @PostMapping("/login")
    public MessageResponse login(@RequestBody LoginRequest req) {
        auth.login(req);
        return new MessageResponse("Code sent to your email");
    }

    @PostMapping("/verify-otp")
    public UserResponse verify(@RequestBody VerifyOtpRequest req, HttpServletResponse res) {
        AuthService.LoginResult result = auth.verifyOtp(req);
        ResponseCookie cookie = ResponseCookie.from("token", result.token())
                .httpOnly(true)          // JavaScript cannot read it (XSS safe)
                .secure(secureCookie)    // true when the site runs on HTTPS
                .sameSite("Strict")      // not sent from other sites (CSRF)
                .path("/")
                .maxAge(Duration.ofHours(2))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return result.user();
    }

    @PostMapping("/logout")
    public MessageResponse logout(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true).secure(secureCookie).sameSite("Strict").path("/").maxAge(0).build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new MessageResponse("Logged out");
    }
}
