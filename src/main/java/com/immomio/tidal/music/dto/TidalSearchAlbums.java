package com.immomio.tidal.music.dto;

import java.util.List;

/**
 * Container for album search results from TIDAL search API.
 */
public record TidalSearchAlbums(List<TidalSearchAlbumData> data) {
}
