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

@RestController
@RequestMapping("/oauth2")
public class TidalOAuth2Controller {

    private static final Logger log = LoggerFactory.getLogger(TidalOAuth2Controller.class);

    private final TidalAuthorizationCodeService authorizationCodeService;
    private final String clientId;
    private final String redirectUri;

    public TidalOAuth2Controller(
            TidalAuthorizationCodeService authorizationCodeService,
            @org.springframework.beans.factory.annotation.Value("${tidal.client-id}") String clientId,
            @org.springframework.beans.factory.annotation.Value("${tidal.oauth2.redirect-uri}") String redirectUri
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

        return new RedirectView(url);
    }

    // ---------------- CALLBACK ----------------

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

        authorizationCodeService.removeAuthorizationSession(state);

        return ResponseEntity.ok(Map.of(
                "message", "Authorization code received",
                "code", code,
                "state", state,
                "next", "/oauth2/token"
        ));
    }

    // ---------------- TOKEN EXCHANGE ----------------

    @PostMapping("/token")
    public ResponseEntity<?> exchangeToken(
            @RequestParam String code,
            @RequestParam String state
    ) {
        var session = authorizationCodeService.getAuthorizationSession(state);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "invalid_state"
            ));
        }

        authorizationCodeService.removeAuthorizationSession(state);

        var tokens = authorizationCodeService.exchangeAuthorizationCodeForTokens(
                session.clientId,
                code,
                session.redirectUri,
                session.pkceChallenge.verifier
        );

        return ResponseEntity.ok(tokens);
    }

    // ---------------- HEALTH ----------------

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "TIDAL OAuth2"
        ));
    }
}
