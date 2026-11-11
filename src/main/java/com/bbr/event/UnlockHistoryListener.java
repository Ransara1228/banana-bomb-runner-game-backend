package com.bbr.event;

import com.bbr.entity.UnlockRecord;
import com.bbr.repository.UnlockRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Audit log: saves a row each time a level is unlocked. ProgressService does not know this class exists. */
@Component
public class UnlockHistoryListener {

    private static final Logger log = LoggerFactory.getLogger(UnlockHistoryListener.class);
    private final UnlockRecordRepository history;

    public UnlockHistoryListener(UnlockRecordRepository history) {
        this.history = history;
    }

    @EventListener
    public void onLevelUnlocked(LevelUnlockedEvent e) {
        history.save(new UnlockRecord(e.userId(), e.newLevel()));
        log.info("User {} unlocked level {}", e.userId(), e.newLevel());
    }
}
