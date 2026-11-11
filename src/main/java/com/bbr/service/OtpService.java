package com.bbr.service;

import com.bbr.entity.OtpCode;
import com.bbr.event.OtpRequestedEvent;
import com.bbr.repository.OtpRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class OtpService {

    private final OtpRepository otpRepo;
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher events;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpRepository otpRepo, PasswordEncoder encoder, ApplicationEventPublisher events) {
        this.otpRepo = otpRepo;
        this.encoder = encoder;
        this.events = events;
    }

    public void sendCode(String email) {
        otpRepo.findTopByEmailOrderByCreatedAtDesc(email).ifPresent(last -> {
            if (last.getCreatedAt().isAfter(Instant.now().minusSeconds(60))) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Wait 60 seconds before asking for a new code");
            }
        });
        String code = String.format("%06d", random.nextInt(1_000_000));   // 6 digits
        otpRepo.deleteByEmail(email);

        OtpCode otp = new OtpCode();
        otp.setEmail(email);
        otp.setCodeHash(encoder.encode(code));
        otp.setCreatedAt(Instant.now());
        otp.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
        otpRepo.save(otp);

        // Event-driven: we only announce it, EmailListener sends the email
        events.publishEvent(new OtpRequestedEvent(email, code));
    }

    public boolean verify(String email, String code) {
        OtpCode otp = otpRepo.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ask for a new code"));
        if (otp.getExpiresAt().isBefore(Instant.now()) || otp.getAttempts() >= 5) {
            otpRepo.deleteByEmail(email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expired, log in again");
        }
        if (code == null || !encoder.matches(code, otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepo.save(otp);
            return false;
        }
        otpRepo.deleteByEmail(email);   // a code works only once
        return true;
    }
}
