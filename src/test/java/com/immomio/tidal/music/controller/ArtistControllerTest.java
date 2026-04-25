package com.immomio.tidal.music.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.immomio.tidal.music.dto.ArtistDto;
import com.immomio.tidal.music.dto.ArtistRequest;
import com.immomio.tidal.music.service.ArtistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ArtistControllerTest {

    @Mock
    private ArtistService artistService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ArtistController(artistService)).build();
    }

    @Test
    void getByIdReturnsArtist() throws Exception {
        when(artistService.getById(7L)).thenReturn(new ArtistDto(7L, "Massive Attack", "tidal-7", false));

        mockMvc.perform(get("/artists/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Massive Attack"));
    }

    @Test
    void searchReturnsMatches() throws Exception {
        when(artistService.search("massive")).thenReturn(List.of(
                new ArtistDto(7L, "Massive Attack", "tidal-7", false)
        ));

        mockMvc.perform(get("/artists/search").param("q", "massive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Massive Attack"));
    }

    @Test
    void createDelegatesToService() throws Exception {
        ArtistRequest request = new ArtistRequest("Radiohead", "tidal-9");
        when(artistService.create(any(ArtistRequest.class)))
                .thenReturn(new ArtistDto(9L, "Radiohead", "tidal-9", true));

        mockMvc.perform(post("/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manuallyEdited").value(true));

        verify(artistService).create(eq(request));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(artistService).delete(3L);

        mockMvc.perform(delete("/artists/3"))
                .andExpect(status().isNoContent());

        verify(artistService).delete(3L);
    }
}
