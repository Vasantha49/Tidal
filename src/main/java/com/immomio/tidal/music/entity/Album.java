package com.immomio.tidal.music.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity representing an Album in the music database.
 * Albums belong to artists and are synchronized from TIDAL.
 * The manuallyEdited flag protects against overwrites during sync.
 */
@Entity
@Table(name = "album")
@Data
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(name = "manually_edited", nullable = false)
    private boolean manuallyEdited;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    /**
     * Protected no-args constructor for JPA.
     */
    protected Album() {
    }

    /**
     * Constructor for creating a new Album.
     *
     * @param title      the album title
     * @param externalId the TIDAL external ID
     * @param artist     the associated artist
     */
    public Album(String title, String externalId, Artist artist) {
        this.title = title;
        this.externalId = externalId;
        this.artist = artist;
    }

    /**
     * Updates the album's title.
     *
     * @param title the new title
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * Updates the TIDAL external ID.
     *
     * @param externalId the new external ID
     */
    public void updateExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Updates the associated artist.
     *
     * @param artist the new artist
     */
    public void updateArtist(Artist artist) {
        this.artist = artist;
    }

    /**
     * Marks the album as manually edited to prevent sync overwrites.
     */
    public void markAsEdited() {
        this.manuallyEdited = true;
    }
}
