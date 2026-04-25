package com.immomio.tidal.music.service;

import com.immomio.tidal.music.dto.AlbumRequest;
import com.immomio.tidal.music.dto.TidalAlbumResponse;
import com.immomio.tidal.music.entity.Album;
import com.immomio.tidal.music.entity.Artist;
import com.immomio.tidal.music.repositories.AlbumRepository;
import com.immomio.tidal.music.repositories.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private TidalService tidalService;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void createMarksAlbumAsEdited() {
        Artist artist = new Artist("Nils Frahm", "tidal-4");
        when(artistRepository.findById(4L)).thenReturn(Optional.of(artist));
        when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = albumService.create(new AlbumRequest("All Melody", "tidal-40", 4L));

        assertThat(result.manuallyEdited()).isTrue();
        assertThat(result.artistName()).isEqualTo("Nils Frahm");
    }

    @Test
    void syncSkipsManuallyEditedAlbum() {
        Artist artist = new Artist("Massive Attack", "tidal-7");
        Album album = new Album("Original", "album-1", artist);
        album.markAsEdited();

        when(artistRepository.findAll()).thenReturn(List.of(artist));
        when(tidalService.fetchAlbumsForArtist("tidal-7")).thenReturn(List.of(new TidalAlbumResponse("album-1", "Updated")));
        when(albumRepository.findByExternalId("album-1")).thenReturn(Optional.of(album));

        albumService.syncAlbums();

        assertThat(album.getTitle()).isEqualTo("Original");
    }

    @Test
    void syncCreatesMissingAlbum() {
        Artist artist = new Artist("Bonobo", "tidal-8");

        when(artistRepository.findAll()).thenReturn(List.of(artist));
        when(tidalService.fetchAlbumsForArtist("tidal-8")).thenReturn(List.of(new TidalAlbumResponse("album-8", "Migration")));
        when(albumRepository.findByExternalId("album-8")).thenReturn(Optional.empty());
        when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> invocation.getArgument(0));

        albumService.syncAlbums();

        ArgumentCaptor<Album> captor = ArgumentCaptor.forClass(Album.class);
        verify(albumRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Migration");
        assertThat(captor.getValue().getArtist()).isSameAs(artist);
    }
}
