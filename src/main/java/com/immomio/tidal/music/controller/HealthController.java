package com.immomio.tidal.music.controller;

import com.immomio.tidal.music.dto.TidalHealthResponse;
import com.immomio.tidal.music.service.TidalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for health checks.
 * Provides endpoints to check the status of external services.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private final TidalService tidalService;

    /**
     * Constructor for dependency injection.
     *
     * @param tidalService the TIDAL service
     */
    public HealthController(TidalService tidalService) {
        this.tidalService = tidalService;
    }

    /**
     * Checks the health of the TIDAL service integration.
     * Returns 200 OK if TIDAL is accessible, 503 Service Unavailable otherwise.
     *
     * @return health response with status details
     */
    @GetMapping("/tidal")
    public ResponseEntity<TidalHealthResponse> tidalHealth() {
        TidalHealthResponse response = tidalService.checkHealth();
        HttpStatus status = "UP".equals(response.status()) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
}
