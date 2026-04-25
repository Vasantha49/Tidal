package com.immomio.tidal.music.dto;

/**
 * Data Transfer Object for Album creation/update requests.
 * Contains the required fields for album operations.
 *
 * @param title      the album title
 * @param externalId the TIDAL external ID
 * @param artistId   the associated artist ID
 */
public record AlbumRequest(String title, String externalId, Long artistId) {
}
