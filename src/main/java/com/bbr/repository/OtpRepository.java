package com.bbr.repository;

import com.bbr.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByEmailOrderByCreatedAtDesc(String email);

    @Transactional
    void deleteByEmail(String email);
}
