package com.immomio.tidal.music.service;

import com.immomio.tidal.music.dto.ArtistDto;
import com.immomio.tidal.music.dto.ArtistRequest;
import com.immomio.tidal.music.dto.TidalSearchResponse;
import com.immomio.tidal.music.entity.Artist;
import com.immomio.tidal.music.repositories.ArtistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class for managing Artist entities.
 * Handles CRUD operations, search, and synchronization with TIDAL.
 * All operations are transactional and respect manual edit protections.
 */
@Service
@Transactional
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final TidalService tidalService;

    /**
     * Constructor for dependency injection.
     *
     * @param artistRepository the artist repository
     * @param tidalService     the TIDAL service for external API calls
     */
    public ArtistService(ArtistRepository artistRepository,
                         TidalService tidalService) {
        this.artistRepository = artistRepository;
        this.tidalService = tidalService;
    }

    /**
     * Retrieves all artists ordered by name.
     *
     * @return list of artist DTOs
     */
    @Transactional(readOnly = true)
    public List<ArtistDto> getAll() {
        return artistRepository.findAllByOrderByNameAsc().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Retrieves a single artist by ID.
     *
     * @param id the artist ID
     * @return the artist DTO
     * @throws ResponseStatusException if artist not found
     */
    @Transactional(readOnly = true)
    public ArtistDto getById(Long id) {
        return toDto(findArtist(id));
    }

    /**
     * Searches artists by name using full-text search.
     *
     * @param query the search query
     * @return list of matching artist DTOs
     */
    @Transactional(readOnly = true)
    public List<ArtistDto> search(String query) {
        return artistRepository.searchFullText(query).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Creates a new artist and marks it as manually edited.
     *
     * @param request the artist creation request
     * @return the created artist DTO
     */
    public ArtistDto create(ArtistRequest request) {
        Artist artist = new Artist(request.name(), request.externalId());
        artist.markAsEdited();
        return toDto(artistRepository.save(artist));
    }

    /**
     * Updates an existing artist and marks it as manually edited.
     *
     * @param id      the artist ID
     * @param request the update request
     * @return the updated artist DTO
     * @throws ResponseStatusException if artist not found
     */
    public ArtistDto update(Long id, ArtistRequest request) {
        Artist artist = findArtist(id);
        artist.updateName(request.name());
        artist.updateExternalId(request.externalId());
        artist.markAsEdited();
        return toDto(artistRepository.save(artist));
    }

    /**
     * Deletes an artist by ID.
     *
     * @param id the artist ID
     * @throws ResponseStatusException if artist not found
     */
    public void delete(Long id) {
        artistRepository.delete(findArtist(id));
    }

    /**
     * Synchronizes artists from TIDAL.
     * Only updates artists that are not manually edited.
     */
    public void syncArtists() {
        artistRepository.findAll().stream()
                .filter(artist -> artist.getExternalId() != null && !artist.getExternalId().isBlank())
                .forEach(artist -> tidalService.fetchArtistById(artist.getExternalId())
                        .ifPresent(dto -> {
                            if (!artist.isManuallyEdited()) {
                                artist.updateName(dto.name());
                                artistRepository.save(artist);
                            }
                        }));
    }

    /**
     * Searches TIDAL for artists.
     *
     * @param query the search query
     * @param limit the maximum number of results
     * @return TIDAL search response
     */
    @Transactional(readOnly = true)
    public TidalSearchResponse searchTidal(String query, int limit) {
        return tidalService.search(query, limit);
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
     * Converts an Artist entity to a DTO.
     *
     * @param artist the artist entity
     * @return the artist DTO
     */
    private ArtistDto toDto(Artist artist) {
        return new ArtistDto(
                artist.getId(),
                artist.getName(),
                artist.getExternalId(),
                artist.isManuallyEdited()
        );
    }
}
