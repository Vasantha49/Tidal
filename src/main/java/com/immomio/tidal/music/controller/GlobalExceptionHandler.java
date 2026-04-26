package com.immomio.tidal.music.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API.
 * Provides consistent error responses with helpful messages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles 404 Not Found errors with helpful message.
     *
     * @param ex the NoHandlerFoundException
     * @return error response with helpful message
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex) {
        log.warn("Endpoint not found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", "The requested endpoint does not exist");
        errorResponse.put("path", ex.getRequestURL());
        errorResponse.put("hint", getHelpfulHint(ex.getRequestURL()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Provides helpful suggestions based on the requested path.
     *
     * @param requestUrl the requested URL
     * @return hint message
     */
    private String getHelpfulHint(String requestUrl) {
        if (requestUrl.contains("/seed")) {
            return "Did you mean /sync/seed? (All sync endpoints start with /sync)";
        } else if (requestUrl.contains("/artists")) {
            return "Available artist endpoints: /artists, /artists/{id}, /artists/search?q=query, /artists/tidal/search?q=query";
        } else if (requestUrl.contains("/albums")) {
            return "Available album endpoints: /albums, /albums/{id}, /albums/search?q=query";
        } else if (requestUrl.contains("/sync")) {
            return "Available sync endpoints: /sync, /sync/artists, /sync/albums, /sync/seed";
        } else if (requestUrl.contains("/health")) {
            return "Available health endpoints: /health/tidal";
        }
        return "Check API_ENDPOINTS.md or README.md for available endpoints";
    }
}


