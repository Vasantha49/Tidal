package com.immomio.tidal.music.dto;

/**
 * Attributes for a TIDAL album in search results.
 */
public record TidalSearchAlbumAttributes(
    String title,
    TidalArtistReference artist
) {
}
