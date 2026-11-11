package com.bbr.repository;

import com.bbr.entity.UnlockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UnlockRecordRepository extends JpaRepository<UnlockRecord, Long> {
    List<UnlockRecord> findByUserIdOrderByUnlockedAtAsc(Long userId);
}
