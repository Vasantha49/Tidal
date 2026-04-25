package com.immomio.tidal.music.repositories;

import com.immomio.tidal.music.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Artist entity operations.
 * Provides CRUD methods and custom queries for artist management and full-text search.
 */
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    /**
     * Finds an artist by their TIDAL external ID.
     *
     * @param externalId the TIDAL external ID
     * @return an Optional containing the artist if found
     */
    Optional<Artist> findByExternalId(String externalId);

    /**
     * Retrieves all artists ordered by name in ascending order.
     *
     * @return list of artists sorted by name
     */
    List<Artist> findAllByOrderByNameAsc();

    /**
     * Performs full-text search on artist names using PostgreSQL's tsvector.
     * Results are ranked by relevance and then by name.
     *
     * @param query the search query string
     * @return list of matching artists ordered by relevance
     */
    @Query(value = """
            SELECT *
            FROM artist
            WHERE to_tsvector('simple', coalesce(name, '')) @@ websearch_to_tsquery('simple', :query)
            ORDER BY ts_rank_cd(to_tsvector('simple', coalesce(name, '')), websearch_to_tsquery('simple', :query)) DESC, name ASC
            """, nativeQuery = true)
    List<Artist> searchFullText(@Param("query") String query);
}
