package com.immomio.tidal.music.controller;

import com.immomio.tidal.music.dto.TidalHealthResponse;
import com.immomio.tidal.music.service.TidalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private TidalService tidalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HealthController(tidalService)).build();
    }

    @Test
    void tidalHealthReturnsOkWhenUp() throws Exception {
        when(tidalService.checkHealth()).thenReturn(new TidalHealthResponse(
                "UP",
                true,
                "TIDAL access token acquired successfully"
        ));

        mockMvc.perform(get("/health/tidal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.credentialsConfigured").value(true));
    }

    @Test
    void tidalHealthReturnsServiceUnavailableWhenDown() throws Exception {
        when(tidalService.checkHealth()).thenReturn(new TidalHealthResponse(
                "DOWN",
                false,
                "TIDAL client credentials are not configured"
        ));

        mockMvc.perform(get("/health/tidal"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.credentialsConfigured").value(false));
    }
}
