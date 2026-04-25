package com.immomio.tidal.music.dto;

/**
 * Data Transfer Object for Album responses.
 * Contains album information including associated artist details for API responses.
 *
 * @param id             the album ID
 * @param title          the album title
 * @param externalId     the TIDAL external ID
 * @param manuallyEdited whether the album was manually edited
 * @param artistId       the associated artist ID
 * @param artistName     the associated artist name
 */
public record AlbumDto(
        Long id,
        String title,
        String externalId,
        boolean manuallyEdited,
        Long artistId,
        String artistName
) {
}
