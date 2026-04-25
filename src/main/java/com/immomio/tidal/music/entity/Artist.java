package com.immomio.tidal.music.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an Artist in the music database.
 * Artists can have multiple albums and are synchronized from TIDAL.
 * The manuallyEdited flag protects against overwrites during sync.
 */
@Entity
@Table(name = "artist")
@Data
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(name = "manually_edited", nullable = false)
    private boolean manuallyEdited;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Album> albums = new ArrayList<>();

    /**
     * Protected no-args constructor for JPA.
     */
    protected Artist() {
    }

    /**
     * Constructor for creating a new Artist.
     *
     * @param name       the artist's name
     * @param externalId the TIDAL external ID
     */
    public Artist(String name, String externalId) {
        this.name = name;
        this.externalId = externalId;
    }

    /**
     * Updates the artist's name.
     *
     * @param name the new name
     */
    public void updateName(String name) {
        this.name = name;
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
     * Marks the artist as manually edited to prevent sync overwrites.
     */
    public void markAsEdited() {
        this.manuallyEdited = true;
    }

    /**
     * Clears the manually edited flag.
     */
    public void clearEditedFlag() {
        this.manuallyEdited = false;
    }
}
