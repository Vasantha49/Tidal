package com.immomio.tidal.music.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class TidalServiceHealthTest {

    @Test
    void healthIsDownWhenCredentialsAreMissing() {
        TidalService tidalService = new TidalService(WebClient.builder().build(), "", "", "DE");

        var response = tidalService.checkHealth();

        assertThat(response.status()).isEqualTo("DOWN");
        assertThat(response.credentialsConfigured()).isFalse();
    }
}
