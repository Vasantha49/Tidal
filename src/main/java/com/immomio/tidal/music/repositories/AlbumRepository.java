package com.immomio.tidal.music.repositories;

import com.immomio.tidal.music.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Album entity operations.
 * Provides CRUD methods and custom queries for album management and full-text search.
 */
public interface AlbumRepository extends JpaRepository<Album, Long> {

    /**
     * Finds an album by its TIDAL external ID.
     *
     * @param externalId the TIDAL external ID
     * @return an Optional containing the album if found
     */
    Optional<Album> findByExternalId(String externalId);

    /**
     * Retrieves all albums ordered by title in ascending order.
     *
     * @return list of albums sorted by title
     */
    List<Album> findAllByOrderByTitleAsc();

    /**
     * Performs full-text search on album titles and artist names using PostgreSQL's tsvector.
     * Combines album title and artist name for comprehensive search.
     * Results are ranked by relevance and then by title.
     *
     * @param query the search query string
     * @return list of matching albums ordered by relevance
     */
    @Query(value = """
            SELECT album.*
            FROM album
            JOIN artist ON artist.id = album.artist_id
            WHERE to_tsvector('simple', coalesce(album.title, '') || ' ' || coalesce(artist.name, ''))
                  @@ websearch_to_tsquery('simple', :query)
            ORDER BY ts_rank_cd(
                to_tsvector('simple', coalesce(album.title, '') || ' ' || coalesce(artist.name, '')),
                websearch_to_tsquery('simple', :query)
            ) DESC, album.title ASC
            """, nativeQuery = true)
    List<Album> searchFullText(@Param("query") String query);
}
