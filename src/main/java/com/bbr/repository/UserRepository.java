package com.bbr.repository;

import com.bbr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /** Leaderboard: the 10 players with the best score (each player once). */
    List<User> findTop10ByBestScoreGreaterThanOrderByBestScoreDesc(int minScore);
}
