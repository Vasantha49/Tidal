package com.immomio.tidal.music.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for WebClient setup.
 * Provides a configured WebClient bean for making HTTP requests to TIDAL API.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates and configures a WebClient bean.
     * Sets the base URL for TIDAL's OpenAPI.
     *
     * @return configured WebClient instance
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://openapi.tidal.com") // may vary depending on API version
                .build();
    }
}
