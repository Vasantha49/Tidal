package com.immomio.tidal.music.dto;

/**
 * Data Transfer Object for Artist responses.
 * Contains artist information for API responses.
 *
 * @param id             the artist ID
 * @param name           the artist name
 * @param externalId     the TIDAL external ID
 * @param manuallyEdited whether the artist was manually edited
 */
public record ArtistDto(Long id, String name, String externalId, boolean manuallyEdited) {
}
