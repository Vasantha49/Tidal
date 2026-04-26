package com.immomio.tidal.music.controller;

import com.immomio.tidal.music.dto.ArtistDto;
import com.immomio.tidal.music.dto.ArtistRequest;
import com.immomio.tidal.music.dto.TidalArtistResponse;
import com.immomio.tidal.music.dto.TidalSearchResponse;
import com.immomio.tidal.music.service.ArtistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing artists.
 * Provides endpoints for CRUD operations and full-text search.
 */
@RestController
@RequestMapping("/artists")
public class ArtistController {

    private final ArtistService artistService;

    /**
     * Constructor for dependency injection.
     *
     * @param artistService the artist service
     */
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    /**
     * Retrieves all artists ordered by name.
     *
     * @return list of artist DTOs
     */
    @GetMapping
    public List<ArtistDto> getAll() {
        return artistService.getAll();
    }

    /**
     * Retrieves a single artist by ID.
     *
     * @param id the artist ID
     * @return the artist DTO
     */
    @GetMapping("/{id:\\d+}")
    public ArtistDto getById(@PathVariable Long id) {
        return artistService.getById(id);
    }

    /**
     * Searches artists by name using full-text search.
     *
     * @param query the search query
     * @return list of matching artist DTOs
     */
    @GetMapping("/search")
    public List<ArtistDto> search(@RequestParam("q") String query) {
        return artistService.search(query);
    }

    /**
     * Creates a new artist.
     *
     * @param request the artist creation request
     * @return the created artist DTO
     */
    @PostMapping
    public ArtistDto create(@RequestBody ArtistRequest request) {
        return artistService.create(request);
    }

    /**
     * Updates an existing artist.
     *
     * @param id      the artist ID
     * @param request the update request
     * @return the updated artist DTO
     */
    @PutMapping("/{id:\\d+}")
    public ArtistDto update(@PathVariable Long id, @RequestBody ArtistRequest request) {
        return artistService.update(id, request);
    }

    /**
     * Deletes an artist by ID.
     *
     * @param id the artist ID
     * @return no content response
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates an artist with a TIDAL external ID using GET (for browser testing).
     *
     * @param name the artist name
     * @param externalId the TIDAL external ID
     * @return the created artist DTO
     */
    @GetMapping("/tidal")
    public ArtistDto createArtistWithTidalIdGet(
            @RequestParam String name,
            @RequestParam String externalId) {
        return createArtistWithTidalIdInternal(name, externalId);
    }

    /**
     * Creates an artist with a TIDAL external ID using POST (for API clients).
     *
     * @param name the artist name
     * @param externalId the TIDAL external ID
     * @return the created artist DTO
     */
    @PostMapping("/tidal")
    public ArtistDto createArtistWithTidalIdPost(
            @RequestParam String name,
            @RequestParam String externalId) {
        return createArtistWithTidalIdInternal(name, externalId);
    }

    /**
     * Internal method for creating an artist with a TIDAL external ID.
     * Users can manually create artists and then sync their albums.
     *
     * @param name the artist name
     * @param externalId the TIDAL external ID
     * @return the created artist DTO
     */
    private ArtistDto createArtistWithTidalIdInternal(String name, String externalId) {
        ArtistRequest request = new ArtistRequest(name, externalId);
        return artistService.create(request);
    }

    /**
     * Searches TIDAL for artists by name.
     *
     * @param query the search query
     * @return list of matching TIDAL artist responses
     */
    @GetMapping("/tidal/search")
    public List<TidalArtistResponse> searchTidal(@RequestParam("q") String query) {
        TidalSearchResponse searchResponse = artistService.searchTidal(query, 10);
        return searchResponse.artists().data().stream()
                .map(data -> new TidalArtistResponse(data.id(), data.attributes().name()))
                .toList();
    }
}
