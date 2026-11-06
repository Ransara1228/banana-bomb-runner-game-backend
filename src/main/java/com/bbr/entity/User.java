/*
 * AI-assisted: Claude (Anthropic), 2026-11-04.
 * Used for: first version of this file. Change it, test it and make sure you understand it before submitting.
 */
package com.bbr.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private int highestLevel = 1;   // levels 1..highestLevel are unlocked
    private int bestScore = 0;

    private int failedLogins = 0;   // wrong passwords in a row
    private Instant lockedUntil;    // login is blocked until this time (null = not locked)

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public int getHighestLevel() { return highestLevel; }
    public void setHighestLevel(int highestLevel) { this.highestLevel = highestLevel; }
    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }
    public int getFailedLogins() { return failedLogins; }
    public void setFailedLogins(int failedLogins) { this.failedLogins = failedLogins; }
    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }
}
