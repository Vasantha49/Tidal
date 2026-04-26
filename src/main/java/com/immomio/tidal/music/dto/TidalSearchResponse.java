package com.immomio.tidal.music.dto;

/**
 * Response DTO for TIDAL search API.
 * Contains search results organized by type (artists, albums, etc.).
 */
public record TidalSearchResponse(TidalSearchArtists artists) {
}
