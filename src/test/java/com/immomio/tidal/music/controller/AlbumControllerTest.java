package com.immomio.tidal.music.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.immomio.tidal.music.dto.AlbumDto;
import com.immomio.tidal.music.dto.AlbumRequest;
import com.immomio.tidal.music.service.AlbumService;
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
class AlbumControllerTest {

    @Mock
    private AlbumService albumService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AlbumController(albumService)).build();
    }

    @Test
    void getByIdReturnsAlbum() throws Exception {
        when(albumService.getById(11L)).thenReturn(new AlbumDto(11L, "Mezzanine", "tidal-11", false, 7L, "Massive Attack"));

        mockMvc.perform(get("/albums/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Mezzanine"))
                .andExpect(jsonPath("$.artistName").value("Massive Attack"));
    }

    @Test
    void searchReturnsMatches() throws Exception {
        when(albumService.search("mez")).thenReturn(List.of(
                new AlbumDto(11L, "Mezzanine", "tidal-11", false, 7L, "Massive Attack")
        ));

        mockMvc.perform(get("/albums/search").param("q", "mez"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Mezzanine"));
    }

    @Test
    void createDelegatesToService() throws Exception {
        AlbumRequest request = new AlbumRequest("Blue Train", "tidal-21", 5L);
        when(albumService.create(any(AlbumRequest.class)))
                .thenReturn(new AlbumDto(21L, "Blue Train", "tidal-21", true, 5L, "John Coltrane"));

        mockMvc.perform(post("/albums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manuallyEdited").value(true));

        verify(albumService).create(eq(request));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(albumService).delete(4L);

        mockMvc.perform(delete("/albums/4"))
                .andExpect(status().isNoContent());

        verify(albumService).delete(4L);
    }
}
