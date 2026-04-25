package com.immomio.tidal.music.dto;

/**
 * Data Transfer Object for Artist creation/update requests.
 * Contains the required fields for artist operations.
 *
 * @param name       the artist name
 * @param externalId the TIDAL external ID
 */
public record ArtistRequest(String name, String externalId) {
}
