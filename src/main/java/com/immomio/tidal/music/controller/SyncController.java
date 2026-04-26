package com.immomio.tidal.music.controller;

import com.immomio.tidal.music.service.SyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for triggering synchronization with TIDAL.
 * Provides endpoints to manually sync artists and albums.
 */
@RestController
@RequestMapping("/sync")
public class SyncController {

    private final SyncService syncService;

    /**
     * Constructor for dependency injection.
     *
     * @param syncService the sync service
     */
    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Triggers a full synchronization: artists and albums.
     */
    @PostMapping
    public void sync() {
        syncService.syncAll();
    }

    /**
     * Triggers synchronization of artists from TIDAL.
     */
    @PostMapping("/artists")
    public void syncArtists() {
        syncService.syncArtists();
    }

    /**
     * Triggers synchronization of albums from TIDAL.
     */
    @PostMapping("/albums")
    public void syncAlbums() {
        syncService.syncAlbums();
    }

    /**
     * Seeds the database with top artists from TIDAL using GET (for browser testing).
     */
    @GetMapping("/seed")
    public void seedArtistsGet() {
        seedArtistsInternal();
    }

    /**
     * Seeds the database with top artists from TIDAL using POST (for API clients).
     */
    @PostMapping("/seed")
    public void seedArtistsPost() {
        seedArtistsInternal();
    }

    /**
     * Internal method to seed the database with top artists from TIDAL.
     */
    private void seedArtistsInternal() {
        syncService.seedArtists();
    }
}
