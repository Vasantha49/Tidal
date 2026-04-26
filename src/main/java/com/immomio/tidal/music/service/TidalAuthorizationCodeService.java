package com.immomio.tidal.music.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing TIDAL OAuth2 Authorization Code Flow with PKCE.
 * Implements RFC 7636 Proof Key for Public Clients for enhanced security.
 *
 * This enables end-user authentication and access to user-specific TIDAL data.
 *
 * Related documentation: https://developer.tidal.com/documentation/api-sdk/api-sdk-authorization
 */
@Service
public class TidalAuthorizationCodeService {

    private static final Logger log = LoggerFactory.getLogger(TidalAuthorizationCodeService.class);

    private static final String TIDAL_AUTH_ENDPOINT = "https://login.tidal.com/authorize";
    private static final String TIDAL_TOKEN_ENDPOINT = "https://auth.tidal.com/v1/oauth2/token";
    private static final int CODE_VERIFIER_LENGTH = 128;

    /**
     * Represents a PKCE challenge for secure authorization code flow.
     * Stores both the verifier (kept on client) and challenge (sent to server).
     */
    public static class PKCEChallenge {
        public final String verifier;
        public final String challenge;
        public final String method;

        public PKCEChallenge(String verifier, String challenge, String method) {
            this.verifier = verifier;
            this.challenge = challenge;
            this.method = method;
        }
    }

    /**
     * Represents an authorization session with saved state for security.
     */
    public static class AuthorizationSession {
        public final String state;
        public final PKCEChallenge pkceChallenge;
        public final String clientId;
        public final String redirectUri;
        public final long createdAt;

        public AuthorizationSession(String state, PKCEChallenge pkceChallenge, 
                                   String clientId, String redirectUri) {
            this.state = state;
            this.pkceChallenge = pkceChallenge;
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.createdAt = System.currentTimeMillis();
        }

        /**
         * Check if session is still valid (not expired after 10 minutes).
         */
        public boolean isValid() {
            long ageMillis = System.currentTimeMillis() - createdAt;
            long maxAgeMillis = 10 * 60 * 1000; // 10 minutes
            return ageMillis < maxAgeMillis;
        }
    }

    /**
     * In-memory store for authorization sessions.
     * In production, use Redis or database.
     */
    private final Map<String, AuthorizationSession> authorizationSessions = new HashMap<>();

    /**
     * Generates a PKCE challenge pair.
     * Implements SHA256 method as recommended.
     *
     * @return PKCEChallenge with verifier and challenge
     */
    public PKCEChallenge generatePKCEChallenge() {
        try {
            // Generate random code verifier (128 characters)
            String verifier = generateRandomString(CODE_VERIFIER_LENGTH);

            // Create SHA256 challenge from verifier
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(verifier.getBytes());
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            log.debug("Generated PKCE challenge pair");
            return new PKCEChallenge(verifier, challenge, "S256");
        } catch (Exception ex) {
            log.error("Failed to generate PKCE challenge", ex);
            throw new RuntimeException("Failed to generate PKCE challenge", ex);
        }
    }

    /**
     * Generates a random state parameter for CSRF protection.
     *
     * @return random state string
     */
    public String generateState() {
        return generateRandomString(32);
    }

    /**
     * Builds the authorization URL for user browser redirect.
     *
     * @param clientId the TIDAL client ID
     * @param redirectUri the callback URI after user authorizes
     * @param scopes requested scopes (space-separated)
     * @param pkceChallenge the PKCE challenge
     * @param state CSRF protection state
     * @return complete authorization URL
     */
    public String buildAuthorizationUrl(String clientId, String redirectUri, 
                                       String scopes, PKCEChallenge pkceChallenge, String state) {
        StringBuilder url = new StringBuilder(TIDAL_AUTH_ENDPOINT);
        url.append("?response_type=code");
        url.append("&client_id=").append(urlEncode(clientId));
        url.append("&redirect_uri=").append(urlEncode(redirectUri));
        url.append("&scope=").append(urlEncode(scopes));
        url.append("&code_challenge=").append(urlEncode(pkceChallenge.challenge));
        url.append("&code_challenge_method=").append(pkceChallenge.method);
        url.append("&state=").append(urlEncode(state));

        log.info("Built authorization URL for user login");
        return url.toString();
    }

    /**
     * Saves an authorization session for state validation during callback.
     *
     * @param session the authorization session
     */
    public void saveAuthorizationSession(AuthorizationSession session) {
        authorizationSessions.put(session.state, session);
        log.debug("Saved authorization session for state: {}", session.state);
    }

    /**
     * Retrieves and validates a saved authorization session.
     *
     * @param state the state parameter
     * @return the session if valid and found
     */
    public AuthorizationSession getAuthorizationSession(String state) {
        AuthorizationSession session = authorizationSessions.get(state);
        if (session == null) {
            log.warn("Authorization session not found for state: {}", state);
            return null;
        }
        if (!session.isValid()) {
            log.warn("Authorization session expired for state: {}", state);
            authorizationSessions.remove(state);
            return null;
        }
        return session;
    }

    /**
     * Removes a session after successful use.
     *
     * @param state the state parameter
     */
    public void removeAuthorizationSession(String state) {
        authorizationSessions.remove(state);
        log.debug("Removed authorization session for state: {}", state);
    }

    /**
     * Builds the token request body for exchanging code for tokens.
     *
     * @param clientId the client ID
     * @param code the authorization code from redirect
     * @param redirectUri the redirect URI (must match original)
     * @param codeVerifier the PKCE code verifier
     * @return request body as map
     */
    public Map<String, String> buildTokenRequestBody(String clientId, String code, 
                                                     String redirectUri, String codeVerifier) {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("client_id", clientId);
        body.put("code", code);
        body.put("redirect_uri", redirectUri);
        body.put("code_verifier", codeVerifier);
        return body;
    }

    /**
     * Builds the refresh token request body.
     *
     * @param clientId the client ID
     * @param refreshToken the refresh token
     * @return request body as map
     */
    public Map<String, String> buildRefreshTokenRequestBody(String clientId, String refreshToken) {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "refresh_token");
        body.put("client_id", clientId);
        body.put("refresh_token", refreshToken);
        return body;
    }

    /**
     * Generates a cryptographically secure random string.
     *
     * @param length the desired length
     * @return random string
     */
    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                .substring(0, length);
    }

    /**
     * URL encodes a string for use in query parameters.
     *
     * @param value the value to encode
     * @return encoded value
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception ex) {
            log.error("Failed to URL encode value", ex);
            return value;
        }
    }

    /**
     * Returns the token endpoint URL.
     *
     * @return TIDAL token endpoint
     */
    public String getTokenEndpoint() {
        return TIDAL_TOKEN_ENDPOINT;
    }
}

