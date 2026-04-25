package com.immomio.tidal.music.service;

import com.immomio.tidal.music.dto.AlbumDto;
import com.immomio.tidal.music.dto.AlbumRequest;
import com.immomio.tidal.music.entity.Album;
import com.immomio.tidal.music.entity.Artist;
import com.immomio.tidal.music.repositories.AlbumRepository;
import com.immomio.tidal.music.repositories.ArtistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class for managing Album entities.
 * Handles CRUD operations, search, and synchronization with TIDAL.
 * All operations are transactional and respect manual edit protections.
 */
@Service
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final TidalService tidalService;

    /**
     * Constructor for dependency injection.
     *
     * @param albumRepository  the album repository
     * @param artistRepository the artist repository
     * @param tidalService     the TIDAL service for external API calls
     */
    public AlbumService(AlbumRepository albumRepository,
                        ArtistRepository artistRepository,
                        TidalService tidalService) {
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.tidalService = tidalService;
    }

    /**
     * Retrieves all albums ordered by title.
     *
     * @return list of album DTOs
     */
    @Transactional(readOnly = true)
    public List<AlbumDto> getAll() {
        return albumRepository.findAllByOrderByTitleAsc().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Retrieves a single album by ID.
     *
     * @param id the album ID
     * @return the album DTO
     * @throws ResponseStatusException if album not found
     */
    @Transactional(readOnly = true)
    public AlbumDto getById(Long id) {
        return toDto(findAlbum(id));
    }

    /**
     * Searches albums by title and artist name using full-text search.
     *
     * @param query the search query
     * @return list of matching album DTOs
     */
    @Transactional(readOnly = true)
    public List<AlbumDto> search(String query) {
        return albumRepository.searchFullText(query).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Creates a new album and marks it as manually edited.
     *
     * @param request the album creation request
     * @return the created album DTO
     * @throws ResponseStatusException if artist not found
     */
    public AlbumDto create(AlbumRequest request) {
        Artist artist = findArtist(request.artistId());
        Album album = new Album(request.title(), request.externalId(), artist);
        album.markAsEdited();
        return toDto(albumRepository.save(album));
    }

    /**
     * Updates an existing album and marks it as manually edited.
     *
     * @param id      the album ID
     * @param request the update request
     * @return the updated album DTO
     * @throws ResponseStatusException if album or artist not found
     */
    public AlbumDto update(Long id, AlbumRequest request) {
        Album album = findAlbum(id);
        album.updateTitle(request.title());
        album.updateExternalId(request.externalId());
        album.updateArtist(findArtist(request.artistId()));
        album.markAsEdited();
        return toDto(albumRepository.save(album));
    }

    /**
     * Deletes an album by ID.
     *
     * @param id the album ID
     * @throws ResponseStatusException if album not found
     */
    public void delete(Long id) {
        albumRepository.delete(findAlbum(id));
    }

    /**
     * Synchronizes albums from TIDAL for all artists.
     */
    public void syncAlbums() {
        artistRepository.findAll().stream()
                .filter(artist -> artist.getExternalId() != null && !artist.getExternalId().isBlank())
                .forEach(this::syncAlbumsForArtist);
    }

    /**
     * Synchronizes albums for a specific artist from TIDAL.
     * Updates existing albums if not manually edited, or creates new ones.
     *
     * @param artist the artist to sync albums for
     * @return the number of new albums imported
     */
    public int syncAlbumsForArtist(Artist artist) {
        int[] importedCount = {0};
        tidalService.fetchAlbumsForArtist(artist.getExternalId())
                .forEach(dto -> albumRepository.findByExternalId(dto.id())
                        .ifPresentOrElse(existing -> {
                            if (!existing.isManuallyEdited()) {
                                existing.updateTitle(dto.title());
                                existing.updateArtist(artist);
                                albumRepository.save(existing);
                            }
                        }, () -> {
                            albumRepository.save(new Album(dto.title(), dto.id(), artist));
                            importedCount[0]++;
                        }));
        return importedCount[0];
    }

    /**
     * Finds an album by ID or throws an exception.
     *
     * @param id the album ID
     * @return the album entity
     * @throws ResponseStatusException if not found
     */
    private Album findAlbum(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
    }

    /**
     * Finds an artist by ID or throws an exception.
     *
     * @param id the artist ID
     * @return the artist entity
     * @throws ResponseStatusException if not found
     */
    private Artist findArtist(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));
    }

    /**
     * Converts an Album entity to a DTO.
     *
     * @param album the album entity
     * @return the album DTO
     */
    private AlbumDto toDto(Album album) {
        return new AlbumDto(
                album.getId(),
                album.getTitle(),
                album.getExternalId(),
                album.isManuallyEdited(),
                album.getArtist().getId(),
                album.getArtist().getName()
        );
    }
}
