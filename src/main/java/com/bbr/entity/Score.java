package com.bbr.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String username;
    private int level;
    private int points;
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public Instant getCreatedAt() { return createdAt; }
}
