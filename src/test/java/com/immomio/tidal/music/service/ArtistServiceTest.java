package com.immomio.tidal.music.service;

import com.immomio.tidal.music.dto.ArtistRequest;
import com.immomio.tidal.music.dto.ArtistDto;
import com.immomio.tidal.music.dto.TidalArtistResponse;
import com.immomio.tidal.music.entity.Artist;
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
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private TidalService tidalService;

    @InjectMocks
    private ArtistService artistService;

    @Test
    void createMarksArtistAsEdited() {
        when(artistRepository.save(any(Artist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = artistService.create(new ArtistRequest("Portishead", "tidal-30"));

        assertThat(result.manuallyEdited()).isTrue();
    }

    @Test
    void syncSkipsManuallyEditedArtist() {
        Artist existing = new Artist("Old Name", "tidal-1");
        existing.markAsEdited();

        when(artistRepository.findAll()).thenReturn(List.of(existing));
        when(tidalService.fetchArtistById("tidal-1")).thenReturn(Optional.of(new TidalArtistResponse("tidal-1", "New Name")));

        artistService.syncArtists();

        assertThat(existing.getName()).isEqualTo("Old Name");
    }

    @Test
    void syncUpdatesExistingArtist() {
        Artist existing = new Artist("Old Burial", "tidal-2");
        when(artistRepository.findAll()).thenReturn(List.of(existing));
        when(tidalService.fetchArtistById("tidal-2")).thenReturn(Optional.of(new TidalArtistResponse("tidal-2", "Burial")));
        when(artistRepository.save(any(Artist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        artistService.syncArtists();

        ArgumentCaptor<Artist> captor = ArgumentCaptor.forClass(Artist.class);
        verify(artistRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Burial");
        assertThat(captor.getValue().getExternalId()).isEqualTo("tidal-2");
    }
}
