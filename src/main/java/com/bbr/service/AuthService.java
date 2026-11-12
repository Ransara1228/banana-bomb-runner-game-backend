/*
 * AI-assisted: Claude (Anthropic), 2026-11-07.
 */
package com.bbr.service;

import com.bbr.dto.LoginRequest;
import com.bbr.dto.RegisterRequest;
import com.bbr.dto.UserResponse;
import com.bbr.dto.VerifyOtpRequest;
import com.bbr.entity.User;
import com.bbr.repository.UserRepository;
import com.bbr.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    /** What a finished login gives back: the JWT for the cookie + the user for the screen. */
    public record LoginResult(String token, UserResponse user) {}

    static final int MAX_FAILED_LOGINS = 5;                    // wrong passwords before the lock
    static final Duration LOCK_TIME = Duration.ofMinutes(5);   // how long the lock lasts

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final OtpService otp;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, OtpService otp, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.otp = otp;
        this.jwt = jwt;
    }

    public void register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User u = new User();
        u.setEmail(req.email());
        u.setUsername(req.username());
        u.setPasswordHash(encoder.encode(req.password()));   // BCrypt hash, never the real password
        users.save(u);
    }

    /** Step 1: email + password. If correct, a 6-digit code goes to the email. */
    public void login(LoginRequest req) {
        User u = users.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));

        // Locked after too many wrong passwords: stop here, even if this password is right
        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many wrong passwords. Try again in 5 minutes");
        }

        if (req.password() == null || !encoder.matches(req.password(), u.getPasswordHash())) {
            u.setFailedLogins(u.getFailedLogins() + 1);
            if (u.getFailedLogins() >= MAX_FAILED_LOGINS) {
                u.setLockedUntil(Instant.now().plus(LOCK_TIME));   // start the lock
                u.setFailedLogins(0);
            }
            users.save(u);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }

        // Right password: clear the counter and send the code
        u.setFailedLogins(0);
        u.setLockedUntil(null);
        users.save(u);
        otp.sendCode(u.getEmail());
    }

    /** Step 2: the code. If correct, make the JWT. */
    public LoginResult verifyOtp(VerifyOtpRequest req) {
        if (!otp.verify(req.email(), req.code())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong code");
        }
        User u = users.findByEmail(req.email()).orElseThrow();
        return new LoginResult(jwt.create(u), UserResponse.from(u));
    }
}
