package com.immomio.tidal.music.service;

import com.immomio.tidal.music.dto.TidalArtistResponse;
import com.immomio.tidal.music.entity.Artist;
import com.immomio.tidal.music.repositories.ArtistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for orchestrating synchronization operations with TIDAL.
 * Provides methods to sync artists and albums, checking for valid credentials.
 */
@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final ArtistService artistService;
    private final AlbumService albumService;
    private final TidalService tidalService;
    private final ArtistRepository artistRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param artistService the artist service
     * @param albumService  the album service
     * @param tidalService  the TIDAL service
     * @param artistRepository the artist repository
     */
    public SyncService(ArtistService artistService, AlbumService albumService, TidalService tidalService, ArtistRepository artistRepository) {
        this.artistService = artistService;
        this.albumService = albumService;
        this.tidalService = tidalService;
        this.artistRepository = artistRepository;
    }

    /**
     * Performs a full synchronization: artists first, then albums.
     * Skips if TIDAL credentials are not configured.
     */
    public void syncAll() {
        if (!tidalService.hasCredentials()) {
            log.info("Skipping scheduled TIDAL sync because tidal.client-id or tidal.client-secret is not configured");
            return;
        }
        artistService.syncArtists();
        albumService.syncAlbums();
    }

    /**
     * Synchronizes artists from TIDAL.
     * Skips if TIDAL credentials are not configured.
     */
    public void syncArtists() {
        if (!tidalService.hasCredentials()) {
            log.info("Skipping artist sync because tidal.client-id or tidal.client-secret is not configured");
            return;
        }
        artistService.syncArtists();
    }

    /**
     * Synchronizes albums from TIDAL.
     * Skips if TIDAL credentials are not configured.
     */
    public void syncAlbums() {
        if (!tidalService.hasCredentials()) {
            log.info("Skipping album sync because tidal.client-id or tidal.client-secret is not configured");
            return;
        }
        albumService.syncAlbums();
    }

    /**
     * Seeds the database with sample artists and albums for testing.
     * If TIDAL API access is limited, creates local sample data.
     * For production, artists should be fetched from TIDAL API.
     */
    public void seedArtists() {
        log.info("Starting artist seeding process");
        
        // Try to fetch from TIDAL first
        if (tidalService.hasCredentials()) {
            List<TidalArtistResponse> topArtists = tidalService.fetchTopArtists(10);
            if (!topArtists.isEmpty()) {
                for (TidalArtistResponse artist : topArtists) {
                    if (artistRepository.findByExternalId(artist.id()).isEmpty()) {
                        Artist newArtist = new Artist(artist.name(), artist.id());
                        artistRepository.save(newArtist);
                        log.info("Seeded artist from TIDAL: {}", artist.name());
                    }
                }
                return;
            }
        }

        // Fallback: Create sample artists locally for testing
        log.info("Creating sample artists for testing (fallback)");
        String[] sampleArtists = {
                "Radiohead",
                "Massive Attack",
                "Burial",
                "Portishead",
                "Thom Yorke",
                "Tricky",
                "Kode9",
                "DJ Shadow",
                "Björk",
                "Goldie"
        };

        for (int i = 0; i < sampleArtists.length; i++) {
            String artistName = sampleArtists[i];
            String externalId = "sample-artist-" + (i + 1);
            if (artistRepository.findByExternalId(externalId).isEmpty()) {
                Artist newArtist = new Artist(artistName, externalId);
                artistRepository.save(newArtist);
                log.info("Seeded sample artist: {}", artistName);
            }
        }
    }
}
