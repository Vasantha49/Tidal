package com.immomio.tidal.music.scheduler;

import com.immomio.tidal.music.service.SyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for automatic synchronization with TIDAL.
 * Runs full sync every hour if credentials are configured.
 */
@Component
public class SyncScheduler {

    private final SyncService syncService;

    /**
     * Constructor for dependency injection.
     *
     * @param syncService the sync service
     */
    public SyncScheduler(SyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Scheduled method to perform full synchronization.
     * Executes every 3600000 milliseconds (1 hour).
     */
    @Scheduled(fixedRate = 3600000)
    public void sync() {
        syncService.syncAll();
    }
}
