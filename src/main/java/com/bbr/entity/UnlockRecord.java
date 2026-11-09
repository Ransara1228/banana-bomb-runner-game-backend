package com.bbr.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** One row each time a player unlocks a new level (written by UnlockHistoryListener). */
@Entity
@Table(name = "unlock_history")
public class UnlockRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private int level;
    private Instant unlockedAt = Instant.now();

    protected UnlockRecord() {}   // for JPA

    public UnlockRecord(Long userId, int level) {
        this.userId = userId;
        this.level = level;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public int getLevel() { return level; }
    public Instant getUnlockedAt() { return unlockedAt; }
}
