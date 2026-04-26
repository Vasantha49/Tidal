package com.immomio.tidal.music.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing TIDAL OAuth2 Authorization Code Flow with PKCE.
 */
@Service
public class TidalAuthorizationCodeService {

    private static final Logger log = LoggerFactory.getLogger(TidalAuthorizationCodeService.class);

    private static final String TIDAL_AUTH_ENDPOINT = "https://login.tidal.com/authorize";
    private static final String TIDAL_TOKEN_ENDPOINT = "https://auth.tidal.com/v1/oauth2/token";
    private static final int CODE_VERIFIER_LENGTH = 128;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * PKCE challenge pair.
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
     * Authorization session stored in memory.
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

        public boolean isValid() {
            return (System.currentTimeMillis() - createdAt) < (10 * 60 * 1000);
        }
    }

    private final Map<String, AuthorizationSession> authorizationSessions = new HashMap<>();

    // ---------------- PKCE + STATE ----------------

    public PKCEChallenge generatePKCEChallenge() {
        try {
            String verifier = generateRandomString(CODE_VERIFIER_LENGTH);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(verifier.getBytes());
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            return new PKCEChallenge(verifier, challenge, "S256");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate PKCE challenge", ex);
        }
    }

    public String generateState() {
        return generateRandomString(32);
    }

    // ---------------- AUTHORIZATION URL ----------------

    public String buildAuthorizationUrl(String clientId, String redirectUri,
                                        String scopes, PKCEChallenge pkceChallenge, String state) {

        return TIDAL_AUTH_ENDPOINT +
                "?response_type=code" +
                "&client_id=" + urlEncode(clientId) +
                "&redirect_uri=" + urlEncode(redirectUri) +
                "&scope=" + urlEncode(scopes) +
                "&code_challenge=" + urlEncode(pkceChallenge.challenge) +
                "&code_challenge_method=" + pkceChallenge.method +
                "&state=" + urlEncode(state);
    }

    // ---------------- SESSION STORE ----------------

    public void saveAuthorizationSession(AuthorizationSession session) {
        authorizationSessions.put(session.state, session);
    }

    public AuthorizationSession getAuthorizationSession(String state) {
        AuthorizationSession session = authorizationSessions.get(state);
        if (session == null || !session.isValid()) {
            authorizationSessions.remove(state);
            return null;
        }
        return session;
    }

    public void removeAuthorizationSession(String state) {
        authorizationSessions.remove(state);
    }

    // ---------------- TOKEN EXCHANGE ----------------

    public Map<String, Object> exchangeAuthorizationCodeForTokens(
            String clientId,
            String code,
            String redirectUri,
            String codeVerifier
    ) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("code_verifier", codeVerifier);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                TIDAL_TOKEN_ENDPOINT,
                request,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Token exchange failed: " + response.getStatusCode());
        }

        return response.getBody();
    }

    public String getTokenEndpoint() {
        return TIDAL_TOKEN_ENDPOINT;
    }

    // ---------------- HELPERS ----------------

    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                .substring(0, length);
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception ex) {
            return value;
        }
    }
}
