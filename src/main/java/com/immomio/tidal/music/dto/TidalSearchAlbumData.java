package com.immomio.tidal.music.dto;

/**
 * TIDAL album data for search results with ID and attributes including artist info.
 */
public record TidalSearchAlbumData(String id, TidalSearchAlbumAttributes attributes) {
}
