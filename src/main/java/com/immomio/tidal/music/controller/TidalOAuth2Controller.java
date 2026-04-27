package com.immomio.tidal.music.controller;

import com.immomio.tidal.music.service.TidalAuthorizationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class TidalOAuth2Controller {

    private static final Logger log = LoggerFactory.getLogger(TidalOAuth2Controller.class);

    private final TidalAuthorizationCodeService authorizationCodeService;
    private final String clientId;
    private final String redirectUri;

    public TidalOAuth2Controller(
            TidalAuthorizationCodeService authorizationCodeService,
            @Value("${tidal.client-id}") String clientId,
            @Value("${tidal.oauth2.redirect-uri}") String redirectUri
    ) {
        this.authorizationCodeService = authorizationCodeService;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    // ---------------- AUTHORIZE ----------------

    @GetMapping("/authorize")
    public RedirectView authorize(
            @RequestParam(defaultValue = "user.read collection.read search.read playlists.write") String scopes
    ) {


        var pkce = authorizationCodeService.generatePKCEChallenge();
        var state = authorizationCodeService.generateState();

        var session = new TidalAuthorizationCodeService.AuthorizationSession(
                state, pkce, clientId, redirectUri
        );
        authorizationCodeService.saveAuthorizationSession(session);

        String url = authorizationCodeService.buildAuthorizationUrl(
                clientId, redirectUri, scopes, pkce, state
        );

        log.info("Redirecting to TIDAL authorize URL with state={}", state);
        return new RedirectView(url);
    }

    // ---------------- CALLBACK (AUTO TOKEN EXCHANGE) ----------------

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", error,
                    "message", "User denied authorization"
            ));
        }

        if (code == null || state == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "missing_parameters"
            ));
        }

        var session = authorizationCodeService.getAuthorizationSession(state);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "invalid_state"
            ));
        }

        log.info("Authorization code received, auto‑exchanging for tokens…");

        try {
            var tokens = authorizationCodeService.exchangeAuthorizationCodeForTokens(
                    session.clientId,
                    code,
                    session.redirectUri,
                    session.pkceChallenge.verifier
            );

            authorizationCodeService.removeAuthorizationSession(state);

            return ResponseEntity.ok(Map.of(
                    "message", "Token exchange successful",
                    "tokens", tokens
            ));

        } catch (Exception ex) {
            log.error("Token exchange failed", ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "error", "token_exchange_failed",
                    "details", ex.getMessage()
            ));
        }
    }

    // ---------------- HEALTH ----------------

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "tidal-oauth2"
        );
    }
}
