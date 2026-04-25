package com.immomio.tidal.music.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TidalTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn
) {
}
