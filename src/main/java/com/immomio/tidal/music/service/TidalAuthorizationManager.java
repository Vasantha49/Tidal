package com.immomio.tidal.music.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Utility component for managing TIDAL OAuth2 authorization.
 * Provides helper methods for token management, validation, and credential verification.
 * Implements best practices from TIDAL's official OAuth2 documentation.
 *
 * Related documentation: https://developer.tidal.com/documentation/api-sdk/api-sdk-authorization
 */
@Component
public class TidalAuthorizationManager {

    private static final Logger log = LoggerFactory.getLogger(TidalAuthorizationManager.class);

    /**
     * TIDAL OAuth2 endpoints
     */
    public static final String TIDAL_AUTH_ENDPOINT = "https://auth.tidal.com/v1/oauth2/token";
    public static final String TIDAL_API_BASE_URL = "https://api.tidal.com/v1";
    public static final String TIDAL_MEDIA_TYPE = "application/vnd.api+json";

    /**
     * Token refresh buffer in seconds.
     * We refresh tokens 60 seconds before they expire to avoid edge cases.
     */
    private static final int TOKEN_REFRESH_BUFFER_SECONDS = 60;

    /**
     * Rate limit retry backoff in seconds.
     * When rate limited, wait this long before retrying.
     */
    private static final int RATE_LIMIT_RETRY_SECONDS = 60;

    /**
     * Validates that client credentials are properly formatted.
     *
     * @param clientId     the client ID
     * @param clientSecret the client secret
     * @return true if both credentials are non-null and non-empty
     */
    public boolean validateCredentials(String clientId, String clientSecret) {
        if (clientId == null || clientId.isBlank()) {
            log.error("TIDAL Client ID is not configured");
            return false;
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            log.error("TIDAL Client Secret is not configured");
            return false;
        }
        log.info("TIDAL credentials validated successfully");
        return true;
    }

    /**
     * Creates a Base64-encoded Basic Authorization header from client credentials.
     * Follows RFC 7617 standards.
     *
     * @param clientId     the client ID
     * @param clientSecret the client secret
     * @return the Basic Authorization header value (e.g., "Basic abcd1234...")
     */
    public String createBasicAuthHeader(String clientId, String clientSecret) {
        try {
            String credentials = clientId + ":" + clientSecret;
            String encoded = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            log.debug("Created Basic Authorization header for TIDAL OAuth2");
            return "Basic " + encoded;
        } catch (Exception ex) {
            log.error("Failed to create Basic Authorization header", ex);
            throw new RuntimeException("Failed to encode credentials", ex);
        }
    }

    /**
     * Creates a Bearer token header value.
     *
     * @param accessToken the access token from TIDAL
     * @return the Bearer token header value (e.g., "Bearer token123...")
     */
    public String createBearerTokenHeader(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            log.error("Access token is null or blank");
            return null;
        }
        return "Bearer " + accessToken;
    }

    /**
     * Validates an access token structure.
     * Checks if token is non-null and looks like a valid JWT or opaque token.
     *
     * @param accessToken the access token to validate
     * @return true if token appears valid
     */
    public boolean isTokenValid(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("Access token is null or empty");
            return false;
        }
        // JWT tokens contain dots (header.payload.signature)
        // TIDAL may return either JWT or opaque tokens
        if (accessToken.length() < 20) {
            log.warn("Access token appears too short to be valid");
            return false;
        }
        return true;
    }

    /**
     * Calculates token expiry time with a refresh buffer.
     * This ensures we refresh tokens before they actually expire.
     *
     * @param expiresInSeconds the token expiry time from TIDAL response
     * @return the calculated expiry instant as Instant
     */
    public Instant calculateTokenExpiry(long expiresInSeconds) {
        // Use buffer to refresh before actual expiry
        long refreshSeconds = Math.max(TOKEN_REFRESH_BUFFER_SECONDS, expiresInSeconds - TOKEN_REFRESH_BUFFER_SECONDS);
        Instant expiry = Instant.now().plusSeconds(refreshSeconds);
        log.info("Token will be refreshed at {}, actual expiry in {} seconds", expiry, expiresInSeconds);
        return expiry;
    }

    /**
     * Checks if a token has expired or is about to expire.
     *
     * @param expiryTime the token expiry time
     * @return true if token is expired or about to expire
     */
    public boolean isTokenExpired(Instant expiryTime) {
        if (expiryTime == null) {
            return true;
        }
        boolean expired = Instant.now().isAfter(expiryTime);
        if (expired) {
            log.debug("Token has expired");
        }
        return expired;
    }

    /**
     * Calculates the rate limit retry delay.
     *
     * @return the instant when retry should be attempted
     */
    public Instant calculateRateLimitRetry() {
        Instant retryAt = Instant.now().plusSeconds(RATE_LIMIT_RETRY_SECONDS);
        log.warn("Rate limited by TIDAL API. Will retry at {}", retryAt);
        return retryAt;
    }

    /**
     * Validates a country code format.
     *
     * @param countryCode the ISO 3166-1 alpha-2 country code
     * @return true if country code is valid format
     */
    public boolean validateCountryCode(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) {
            log.warn("Invalid country code: {}. Expected ISO 3166-1 alpha-2 format", countryCode);
            return false;
        }
        if (!countryCode.matches("[A-Z]{2}")) {
            log.warn("Country code must be uppercase letters only: {}", countryCode);
            return false;
        }
        return true;
    }

    /**
     * Generates a log message for authentication errors.
     *
     * @param statusCode the HTTP status code
     * @param statusText the HTTP status text
     * @param responseBody the response body (if available)
     * @return formatted error message
     */
    public String formatAuthError(int statusCode, String statusText, String responseBody) {
        StringBuilder message = new StringBuilder();
        message.append("TIDAL authentication failed with HTTP ").append(statusCode).append(" ").append(statusText);

        if (statusCode == 401) {
            message.append(". Check tidal.client-id and tidal.client-secret configuration");
        } else if (statusCode == 400) {
            message.append(". Check request format and parameters");
        } else if (statusCode == 429) {
            message.append(". Rate limited by TIDAL API");
        }

        if (responseBody != null && !responseBody.isBlank()) {
            message.append(". Response: ").append(responseBody);
        }

        return message.toString();
    }

    /**
     * Creates the OAuth2 grant type for client credentials flow.
     *
     * @return the grant type string
     */
    public String getOAuth2GrantType() {
        return "client_credentials";
    }

    /**
     * Returns the authorization endpoint URL for token requests.
     *
     * @return the TIDAL OAuth2 token endpoint
     */
    public String getTokenEndpoint() {
        return TIDAL_AUTH_ENDPOINT;
    }

    /**
     * Returns the API base URL for TIDAL requests.
     *
     * @return the TIDAL API base URL
     */
    public String getApiBaseUrl() {
        return TIDAL_API_BASE_URL;
    }

    /**
     * Returns the required media type for TIDAL API requests.
     *
     * @return the TIDAL JSON:API media type
     */
    public String getMediaType() {
        return TIDAL_MEDIA_TYPE;
    }

    /**
     * Logs token requests for debugging purposes.
     * Does NOT log sensitive credentials.
     *
     * @param reason the reason for the token request
     */
    public void logTokenRequest(String reason) {
        log.info("Requesting TIDAL access token: {}", reason);
    }

    /**
     * Logs successful token acquisition.
     *
     * @param expiryTime the time when token will expire
     */
    public void logTokenSuccess(Instant expiryTime) {
        log.info("Successfully obtained TIDAL access token, expires at {}", expiryTime);
    }

    /**
     * Logs token invalidation (typically on 401 errors).
     *
     * @param reason the reason for invalidation
     */
    public void logTokenInvalidation(String reason) {
        log.warn("TIDAL access token invalidated: {}", reason);
    }
}

