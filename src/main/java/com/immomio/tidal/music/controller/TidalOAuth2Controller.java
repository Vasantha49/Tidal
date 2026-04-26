package com.immomio.tidal.music.controller;

import com.immomio.tidal.music.service.TidalAuthorizationCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for TIDAL OAuth2 Authorization Code Flow.
 * Handles user login/authorization with PKCE for enhanced security.
 *
 * This enables end-user authentication and user-specific data access.
 */
@RestController
@RequestMapping("/oauth2")
public class TidalOAuth2Controller {

    private static final Logger log = LoggerFactory.getLogger(TidalOAuth2Controller.class);

    private final TidalAuthorizationCodeService authorizationCodeService;

    // Injected from properties
    private final String clientId;
    private final String redirectUri;

    /**
     * Constructor with dependency injection.
     *
     * @param authorizationCodeService the authorization code service
     * @param clientId the TIDAL client ID from configuration
     * @param redirectUri the redirect URI from configuration
     */
    public TidalOAuth2Controller(TidalAuthorizationCodeService authorizationCodeService,
                                @org.springframework.beans.factory.annotation.Value("${tidal.client-id:}") String clientId,
                                @org.springframework.beans.factory.annotation.Value("${tidal.oauth2.redirect-uri:http://localhost:8080/oauth2/callback}") String redirectUri) {
        this.authorizationCodeService = authorizationCodeService;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    /**
     * Initiates TIDAL user login flow.
     * Returns the authorization URL that should be used for browser redirect.
     *
     * @param scopes comma-separated requested scopes (e.g., "r_dst,w_dst")
     * @return authorization URL and session state
     */
    @GetMapping("/authorize")
    public RedirectView  authorize(@RequestParam(defaultValue = "r_usr") String scopes) {
        try {
            // Generate PKCE challenge pair
            TidalAuthorizationCodeService.PKCEChallenge pkceChallenge =
                authorizationCodeService.generatePKCEChallenge();

            // Generate state for CSRF protection
            String state = authorizationCodeService.generateState();

            // Save authorization session for callback validation
            TidalAuthorizationCodeService.AuthorizationSession session =
                new TidalAuthorizationCodeService.AuthorizationSession(
                    state, pkceChallenge, clientId, redirectUri
                );
            authorizationCodeService.saveAuthorizationSession(session);

            // Build authorization URL for user browser redirect
            String authorizationUrl = authorizationCodeService.buildAuthorizationUrl(
                clientId, redirectUri, scopes, pkceChallenge, state
            );

            Map<String, String> response = new HashMap<>();
            response.put("authorization_url", authorizationUrl);
            response.put("state", state);
            response.put("message", "Redirect user to the authorization_url to authorize access");
            System.out.println(response); // Log response for debugging
            log.info("Generated authorization URL for user login");
            log.info("Redirecting user to TIDAL login");
            return new RedirectView(authorizationUrl); // HTTP 302 redirect

        } catch (Exception ex) {
            log.error("Failed to redirect to TIDAL login", ex);
            return new RedirectView("/error?message=" + ex.getMessage());
        }
    }

    /**
     * OAuth2 callback endpoint that receives the authorization code.
     * User is redirected here after authorizing in TIDAL.
     *
     * This endpoint receives:
     * - code: authorization code (valid for 5 minutes)
     * - state: CSRF protection token
     * - error: if user denied authorization
     *
     * @param code the authorization code from TIDAL
     * @param state the state parameter for CSRF validation
     * @param error error from TIDAL if user denied
     * @return token exchange instructions or error
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {

        Map<String, Object> response = new HashMap<>();

        // Handle user denial
        if (error != null) {
            log.warn("User denied authorization. Error: {}", error);
            response.put("error", error);
            response.put("message", "User denied authorization request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Validate required parameters
        if (code == null || state == null) {
            log.warn("Missing required callback parameters");
            response.put("error", "missing_parameters");
            response.put("message", "Missing 'code' or 'state' parameter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Retrieve and validate saved authorization session
        TidalAuthorizationCodeService.AuthorizationSession session =
            authorizationCodeService.getAuthorizationSession(state);

        if (session == null) {
            log.warn("Invalid or expired authorization state: {}", state);
            response.put("error", "invalid_state");
            response.put("message", "Authorization session expired or invalid state parameter");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Remove session after validation
        authorizationCodeService.removeAuthorizationSession(state);

        // Build token exchange request information
        Map<String, String> tokenRequest = authorizationCodeService.buildTokenRequestBody(
            clientId, code, redirectUri, session.pkceChallenge.verifier
        );

        response.put("message", "Exchange authorization code for tokens");
        response.put("token_endpoint", authorizationCodeService.getTokenEndpoint());
        response.put("token_request_body", tokenRequest);
        response.put("instructions",
            "POST the token_request_body to the token_endpoint to exchange the code for access_token and refresh_token");

        log.info("Authorization code received and validated. Ready for token exchange.");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint information for performing token exchange.
     * Frontend or backend server should complete this step.
     *
     * @return detailed token exchange instructions
     */
    @GetMapping("/token-exchange-info")
    public ResponseEntity<Map<String, Object>> tokenExchangeInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("description", "Exchange authorization code for access and refresh tokens");
        info.put("endpoint", authorizationCodeService.getTokenEndpoint());
        info.put("method", "POST");
        info.put("content_type", "application/x-www-form-urlencoded");

        Map<String, String> bodyExample = new HashMap<>();
        bodyExample.put("grant_type", "authorization_code");
        bodyExample.put("client_id", "your-client-id");
        bodyExample.put("code", "authorization-code-from-callback");
        bodyExample.put("redirect_uri", "http://localhost:8080/oauth2/callback");
        bodyExample.put("code_verifier", "pkce-code-verifier-from-authorization");

        info.put("body_parameters", bodyExample);

        Map<String, String> responseExample = new HashMap<>();
        responseExample.put("access_token", "Bearer token for API calls");
        responseExample.put("refresh_token", "Token for refreshing access when expired");
        responseExample.put("token_type", "Bearer");
        responseExample.put("expires_in", "86400 (24 hours in seconds)");

        info.put("response_parameters", responseExample);

        Map<String, String> curlExample = new HashMap<>();
        curlExample.put("command",
            "curl -X POST https://auth.tidal.com/v1/oauth2/token " +
            "-H 'Content-Type: application/x-www-form-urlencoded' " +
            "-d 'grant_type=authorization_code&client_id=YOUR_ID&code=AUTH_CODE&redirect_uri=YOUR_URI&code_verifier=VERIFIER'");

        info.put("curl_example", curlExample);

        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint information for refreshing expired access tokens.
     *
     * @return refresh token flow instructions
     */
    @GetMapping("/refresh-token-info")
    public ResponseEntity<Map<String, Object>> refreshTokenInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("description", "Refresh an expired access token using refresh token");
        info.put("endpoint", authorizationCodeService.getTokenEndpoint());
        info.put("method", "POST");
        info.put("content_type", "application/x-www-form-urlencoded");

        Map<String, String> bodyExample = new HashMap<>();
        bodyExample.put("grant_type", "refresh_token");
        bodyExample.put("client_id", "your-client-id");
        bodyExample.put("refresh_token", "refresh-token-from-token-response");

        info.put("body_parameters", bodyExample);

        Map<String, String> responseExample = new HashMap<>();
        responseExample.put("access_token", "New Bearer token");
        responseExample.put("refresh_token", "New refresh token (or same if not expired)");
        responseExample.put("token_type", "Bearer");
        responseExample.put("expires_in", "86400");

        info.put("response_parameters", responseExample);

        Map<String, String> curlExample = new HashMap<>();
        curlExample.put("command",
            "curl -X POST https://auth.tidal.com/v1/oauth2/token " +
            "-H 'Content-Type: application/x-www-form-urlencoded' " +
            "-d 'grant_type=refresh_token&client_id=YOUR_ID&refresh_token=REFRESH_TOKEN'");

        info.put("curl_example", curlExample);

        return ResponseEntity.ok(info);
    }

    /**
     * Health check for OAuth2 endpoints.
     *
     * @return service status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "TIDAL OAuth2 Authorization Code Flow");
        status.put("description", "User authentication and authorization service");
        status.put("endpoints", "/oauth2/authorize, /oauth2/callback, /oauth2/token-exchange-info, /oauth2/refresh-token-info");
        return ResponseEntity.ok(status);
    }
}

