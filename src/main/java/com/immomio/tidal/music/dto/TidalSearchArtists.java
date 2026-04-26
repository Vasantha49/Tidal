package com.immomio.tidal.music.dto;

import java.util.List;

/**
 * Container for artist search results from TIDAL search API.
 */
public record TidalSearchArtists(List<TidalArtistData> data) {
}
