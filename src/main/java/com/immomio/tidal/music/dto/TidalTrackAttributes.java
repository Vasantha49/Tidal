package com.immomio.tidal.music.dto;

/**
 * Attributes for a TIDAL track in search results.
 */
public record TidalTrackAttributes(
    String title,
    TidalArtistReference artist,
    TidalAlbumReference album
) {
}
