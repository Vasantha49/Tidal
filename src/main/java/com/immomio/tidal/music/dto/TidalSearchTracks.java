package com.immomio.tidal.music.dto;

import java.util.List;

/**
 * Container for track search results from TIDAL search API.
 */
public record TidalSearchTracks(List<TidalTrackData> data) {
}
