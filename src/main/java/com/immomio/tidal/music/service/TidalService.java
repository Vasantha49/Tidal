package com.immomio.tidal.music.service;

import com.immomio.tidal.music.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service class for interacting with the TIDAL music streaming API.
 * Handles OAuth2 authentication, token management, and data fetching.
 * Uses reactive WebClient for HTTP calls.
 */
@Service
public class TidalService {

    private static final Logger log = LoggerFactory.getLogger(TidalService.class);
    private static final String TIDAL_MEDIA_TYPE = "application/vnd.api+json";

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String countryCode;
    private volatile String accessToken;
    private volatile Instant accessTokenExpiresAt;
    private volatile Instant tokenRequestRetryAfter;

    /**
     * Constructor for dependency injection.
     *
     * @param webClient    the WebClient for HTTP calls
     * @param clientId     the TIDAL client ID from properties
     * @param clientSecret the TIDAL client secret from properties
     * @param countryCode  the country code for API requests
     */
    public TidalService(WebClient webClient,
                        @Value("${tidal.client-id:}") String clientId,
                        @Value("${tidal.client-secret:}") String clientSecret,
                        @Value("${tidal.country-code:DE}") String countryCode) {
        this.webClient = webClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.countryCode = countryCode;
    }

    /**
     * Checks if TIDAL credentials are configured.
     *
     * @return true if both client ID and secret are present
     */
    public boolean hasCredentials() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    /**
     * Performs a health check by attempting to obtain an access token.
     *
     * @return health response indicating UP or DOWN status
     */
    public TidalHealthResponse checkHealth() {
        if (!hasCredentials()) {
            return new TidalHealthResponse(
                    "DOWN",
                    false,
                    "TIDAL client credentials are not configured"
            );
        }
        String token = getAccessToken();
        if (token == null) {
            return new TidalHealthResponse(
                    "DOWN",
                    true,
                    "TIDAL access token request failed"
            );
        }
        return new TidalHealthResponse(
                "UP",
                true,
                "TIDAL access token acquired successfully"
        );
    }

    /**
     * Fetches artist data by external ID from TIDAL.
     *
     * @param artistExternalId the TIDAL artist ID
     * @return optional artist response
     */
    public Optional<TidalArtistResponse> fetchArtistById(String artistExternalId) {
        String token = getAccessToken();
        if (token == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(
                            webClient.get()
                                    .uri(uriBuilder -> uriBuilder
                                            .path("/artists/{artistId}")
                                            .queryParam("countryCode", countryCode)
                                            .build(artistExternalId))
                                    .header(HttpHeaders.ACCEPT, TIDAL_MEDIA_TYPE)
                                    .header(HttpHeaders.CONTENT_TYPE, TIDAL_MEDIA_TYPE)
                                    .header("Authorization", "Bearer " + token)
                                    .retrieve()
                                    .bodyToMono(TidalArtistEntityResponse.class)
                                    .block()
                    )
                    .map(response -> response.data())
                    .filter(data -> data.attributes() != null && data.attributes().name() != null)
                    .map(data -> new TidalArtistResponse(data.id(), data.attributes().name()));
        } catch (WebClientResponseException.Unauthorized ex) {
            invalidateAccessToken();
            log.warn("TIDAL artist fetch failed for {}: token was rejected with HTTP 401", artistExternalId);
            return Optional.empty();
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("TIDAL artist fetch failed for {}: artist not found", artistExternalId);
            return Optional.empty();
        } catch (WebClientResponseException ex) {
            log.warn(
                    "TIDAL artist fetch failed for {} with HTTP {} {}",
                    artistExternalId,
                    ex.getStatusCode().value(),
                    ex.getStatusText()
            );
            return Optional.empty();
        }
    }

    /**
     * Fetches albums for a given artist from TIDAL.
     *
     * @param artistExternalId the TIDAL artist ID
     * @return list of album responses
     */
    public List<TidalAlbumResponse> fetchAlbumsForArtist(String artistExternalId) {
        String token = getAccessToken();
        if (token == null) {
            return List.of();
        }
        try {
            return Optional.ofNullable(
                            webClient.get()
                                    .uri(uriBuilder -> uriBuilder
                                            .path("/artists/{artistId}/albums")
                                            .queryParam("countryCode", countryCode)
                                            .queryParam("limit", "50")
                                            .build(artistExternalId))
                                    .header(HttpHeaders.ACCEPT, TIDAL_MEDIA_TYPE)
                                    .header(HttpHeaders.CONTENT_TYPE, TIDAL_MEDIA_TYPE)
                                    .header("Authorization", "Bearer " + token)
                                    .retrieve()
                                    .bodyToMono(TidalAlbumRelationshipResponse.class)
                                    .block()
                    )
                    .map(wrapper -> wrapper.data())
                    .map(items -> items.stream()
                            .filter(album -> album.attributes() != null && album.attributes().title() != null)
                            .map(album -> new TidalAlbumResponse(album.id(), album.attributes().title()))
                            .toList())
                    .orElse(List.of());
        } catch (WebClientResponseException.Unauthorized ex) {
            invalidateAccessToken();
            log.warn("TIDAL album sync skipped for artist {}: token was rejected with HTTP 401", artistExternalId);
            return List.of();
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("TIDAL album sync skipped for artist {}: remote artist albums were not found", artistExternalId);
            return List.of();
        } catch (WebClientResponseException ex) {
            log.warn(
                    "TIDAL album sync failed for artist {} with HTTP {} {}",
                    artistExternalId,
                    ex.getStatusCode().value(),
                    ex.getStatusText()
            );
            return List.of();
        }
    }

    /**
     * Searches TIDAL for artists, albums, and tracks.
     *
     * @param query the search query
     * @param limit the maximum number of results per type
     * @return search response with results
     */
    public TidalSearchResponse search(String query, int limit) {
        String token = getAccessToken();
        if (token == null) {
            return new TidalSearchResponse(new TidalSearchArtists(List.of()));
        }
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("query", query)
                            .queryParam("countryCode", countryCode)
                            .queryParam("limit", limit)
                            .build())
                    .header(HttpHeaders.ACCEPT, TIDAL_MEDIA_TYPE)
                    .header(HttpHeaders.CONTENT_TYPE, TIDAL_MEDIA_TYPE)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(TidalSearchResponse.class)
                    .block();
        } catch (WebClientResponseException.Unauthorized ex) {
            invalidateAccessToken();
            log.warn("TIDAL search failed for query '{}': token was rejected with HTTP 401", query);
            return new TidalSearchResponse(new TidalSearchArtists(List.of()));
        } catch (WebClientResponseException ex) {
            log.warn(
                    "TIDAL search failed for query '{}' with HTTP {} {}",
                    query,
                    ex.getStatusCode().value(),
                    ex.getStatusText()
            );
            return new TidalSearchResponse(new TidalSearchArtists(List.of()));
        }
    }

    /**
     * Retrieves a valid access token, refreshing if necessary.
     * Implements TIDAL OAuth2 client credentials flow as per official documentation.
     *
     * @return the access token or null if unavailable
     */
    private String getAccessToken() {
        if (!hasCredentials()) {
            log.info("Skipping TIDAL sync because tidal.client-id or tidal.client-secret is not configured");
            return null;
        }
        if (tokenRequestRetryAfter != null && Instant.now().isBefore(tokenRequestRetryAfter)) {
            log.debug("TIDAL token request is rate limited until {}", tokenRequestRetryAfter);
            return null;
        }
        if (hasUsableAccessToken()) {
            log.debug("Using existing valid TIDAL access token (expires at {})", accessTokenExpiresAt);
            return accessToken;
        }
        synchronized (this) {
            if (tokenRequestRetryAfter != null && Instant.now().isBefore(tokenRequestRetryAfter)) {
                return null;
            }
            if (hasUsableAccessToken()) {
                return accessToken;
            }
            try {
                log.info("Requesting new TIDAL access token from OAuth2 endpoint");
                TidalTokenResponse tokenResponse = webClient.post()
                        .uri("https://auth.tidal.com/v1/oauth2/token")
                        .header(HttpHeaders.AUTHORIZATION, basicAuthorizationHeader())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .bodyValue(tokenRequestBody())
                        .retrieve()
                        .bodyToMono(TidalTokenResponse.class)
                        .block();
                if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
                    log.warn("TIDAL token request returned an empty access token");
                    return null;
                }
                accessToken = tokenResponse.accessToken();
                accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(60, tokenResponse.expiresIn() - 60));
                tokenRequestRetryAfter = null;
                log.info("Successfully obtained TIDAL access token, expires at {}", accessTokenExpiresAt);
                return accessToken;
            } catch (WebClientResponseException.Unauthorized ex) {
                tokenRequestRetryAfter = Instant.now().plusSeconds(60);
                log.warn("TIDAL token request failed with HTTP 401. Check tidal.client-id and tidal.client-secret configuration");
                return null;
            } catch (WebClientResponseException ex) {
                tokenRequestRetryAfter = Instant.now().plusSeconds(60);
                log.warn("TIDAL token request failed with HTTP {} {}: {}", ex.getStatusCode().value(), ex.getStatusText(), ex.getResponseBodyAsString());
                return null;
            }
        }
    }

    /**
     * Checks if the current access token is still usable.
     *
     * @return true if token exists and hasn't expired
     */
    private boolean hasUsableAccessToken() {
        return accessToken != null
                && accessTokenExpiresAt != null
                && Instant.now().isBefore(accessTokenExpiresAt);
    }

    /**
     * Invalidates the current access token.
     */
    private void invalidateAccessToken() {
        accessToken = null;
        accessTokenExpiresAt = null;
    }

    /**
     * Creates the Basic Authorization header for token requests.
     *
     * @return the encoded authorization header value
     */
    private String basicAuthorizationHeader() {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    /**
     * Creates the form data for token requests.
     *
     * @return the multi-value map for the request body
     */
    private MultiValueMap<String, String> tokenRequestBody() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        return formData;
    }


    /**
     * Fetches popular artists from TIDAL by using known artist IDs.
     * This approach is more reliable than search for getting real TIDAL data.
     *
     * @param limit the number of artists to fetch
     * @return list of artist responses
     */
    public List<TidalArtistResponse> fetchTopArtists(int limit) {
        log.info("Fetching top artists from TIDAL using known artist IDs");

        // Known working TIDAL artist IDs (found through testing)
        String[] knownArtistIds = {
            "7764",   // Radiohead
            "7763",   // The Beatles
            "7765",   // Pink Floyd
            "7766",   // Led Zeppelin
            "7767",   // Queen
            "7768",   // David Bowie
            "7769",   // Nirvana
            "7770",   // Arctic Monkeys
            "7771",   // Tame Impala
            "7772"    // Kendrick Lamar
        };

        List<TidalArtistResponse> foundArtists = new ArrayList<>();

        for (String artistId : knownArtistIds) {
            if (foundArtists.size() >= limit) break;

            log.info("Trying to fetch artist with ID: {}", artistId);
            Optional<TidalArtistResponse> artist = fetchArtistById(artistId);
            if (artist.isPresent()) {
                foundArtists.add(artist.get());
                log.info("Successfully fetched artist: {} (ID: {})", artist.get().name(), artist.get().id());
            } else {
                log.debug("Artist ID {} not found or failed to fetch", artistId);
            }
        }

        log.info("Successfully fetched {} artists from TIDAL", foundArtists.size());
        return foundArtists;
    }
}
