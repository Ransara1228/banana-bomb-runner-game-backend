/*
 * AI-assisted: Claude (Anthropic), 2026-11-09.
 */
package com.bbr.security;

import com.bbr.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Signed token that says "this is user X" for 2 hours. */
    public String create(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("name", user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    /** Returns the user id, or throws if the token is missing, fake or expired. */
    public Long userId(String token) {
        String subject = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
        return Long.valueOf(subject);
    }
}
