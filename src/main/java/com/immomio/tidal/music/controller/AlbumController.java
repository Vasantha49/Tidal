package com.immomio.tidal.music.controller;

import com.immomio.tidal.music.dto.AlbumDto;
import com.immomio.tidal.music.dto.AlbumRequest;
import com.immomio.tidal.music.service.AlbumService;
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
 * REST controller for managing albums.
 * Provides endpoints for CRUD operations and full-text search.
 */
@RestController
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;

    /**
     * Constructor for dependency injection.
     *
     * @param albumService the album service
     */
    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    /**
     * Retrieves all albums ordered by title.
     *
     * @return list of album DTOs
     */
    @GetMapping
    public List<AlbumDto> getAll() {
        return albumService.getAll();
    }

    /**
     * Retrieves a single album by ID.
     *
     * @param id the album ID
     * @return the album DTO
     */
    @GetMapping("/{id:\\d+}")
    public AlbumDto getById(@PathVariable Long id) {
        return albumService.getById(id);
    }

    /**
     * Searches albums by title and artist name using full-text search.
     *
     * @param query the search query
     * @return list of matching album DTOs
     */
    @GetMapping("/search")
    public List<AlbumDto> search(@RequestParam("q") String query) {
        return albumService.search(query);
    }

    /**
     * Creates a new album.
     *
     * @param request the album creation request
     * @return the created album DTO
     */
    @PostMapping
    public AlbumDto create(@RequestBody AlbumRequest request) {
        return albumService.create(request);
    }

    /**
     * Updates an existing album.
     *
     * @param id      the album ID
     * @param request the update request
     * @return the updated album DTO
     */
    @PutMapping("/{id:\\d+}")
    public AlbumDto update(@PathVariable Long id, @RequestBody AlbumRequest request) {
        return albumService.update(id, request);
    }

    /**
     * Deletes an album by ID.
     *
     * @param id the album ID
     * @return no content response
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
