package com.immomio.tidal.music.dto;

public record TidalHealthResponse(
        String status,
        boolean credentialsConfigured,
        String message
) {
}
