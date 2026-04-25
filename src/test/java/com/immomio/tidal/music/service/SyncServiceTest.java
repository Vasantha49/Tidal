package com.immomio.tidal.music.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private ArtistService artistService;

    @Mock
    private AlbumService albumService;

    @Mock
    private TidalService tidalService;

    @InjectMocks
    private SyncService syncService;

    @Test
    void syncAllSkipsWhenCredentialsMissing() {
        when(tidalService.hasCredentials()).thenReturn(false);

        syncService.syncAll();

        verify(artistService, never()).syncArtists();
        verify(albumService, never()).syncAlbums();
    }

    @Test
    void syncAllRunsWhenCredentialsPresent() {
        when(tidalService.hasCredentials()).thenReturn(true);

        syncService.syncAll();

        verify(artistService).syncArtists();
        verify(albumService).syncAlbums();
    }
}
