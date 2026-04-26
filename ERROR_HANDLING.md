# 404 Error Resolution - Documentation

## Problem

You received a **404 Not Found** error when accessing `/seed`:

```
Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.

Sun Apr 26 11:44:00 CEST 2026
There was an unexpected error (type=Not Found, status=404).
for localhost:8080/seed
```

## Root Cause

The endpoint path was incorrect. The SyncController maps all endpoints under the `/sync` path:

```java
@RestController
@RequestMapping("/sync")  // ← All endpoints are prefixed with /sync
public class SyncController {
    
    @GetMapping("/seed")  // ← This creates endpoint: /sync/seed
    public void seedArtistsGet() { }
}
```

**What you tried:** `http://localhost:8080/seed`  
**What you should use:** `http://localhost:8080/sync/seed`

## Solutions Implemented

### 1. Created Comprehensive API Reference

**File:** `API_ENDPOINTS.md`

Contains:
- All available endpoints with correct paths
- HTTP method (GET/POST/PUT/DELETE)
- curl examples for each endpoint
- Request/response examples
- Common troubleshooting tips
- Browser-testable endpoints

### 2. Added Global Exception Handler

**File:** `GlobalExceptionHandler.java`

Features:
- Catches 404 Not Found errors
- Provides helpful error messages
- Suggests correct endpoint paths based on what was requested
- Returns JSON error responses (not HTML Whitelabel error)

**Example Response (with better error message):**
```json
{
  "timestamp": "2026-04-26T11:44:00",
  "status": 404,
  "error": "Not Found",
  "message": "The requested endpoint does not exist",
  "path": "/seed",
  "hint": "Did you mean /sync/seed? (All sync endpoints start with /sync)"
}
```

### 3. Enabled No Handler Found Exception

**File:** `application.yaml`

Added configuration:
```yaml
spring:
  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      add-mappings: false
```

This enables:
- Spring to throw `NoHandlerFoundException` instead of sending Whitelabel error page
- Our global exception handler to catch and format the error
- Better error messages with helpful hints

## Sync Endpoints - Complete Reference

| Purpose | Method | Path | Example |
|---------|--------|------|---------|
| Seed database | GET | `/sync/seed` | `curl http://localhost:8080/sync/seed` |
| Seed database | POST | `/sync/seed` | `curl -X POST http://localhost:8080/sync/seed` |
| Full sync | POST | `/sync` | `curl -X POST http://localhost:8080/sync` |
| Sync artists | POST | `/sync/artists` | `curl -X POST http://localhost:8080/sync/artists` |
| Sync albums | POST | `/sync/albums` | `curl -X POST http://localhost:8080/sync/albums` |

## Testing the Fix

### Before (Old Error Message):
```html
Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.
Sun Apr 26 11:44:00 CEST 2026
There was an unexpected error (type=Not Found, status=404).
```

### After (New Error Message):
```json
{
  "timestamp": "2026-04-26T11:44:00",
  "status": 404,
  "error": "Not Found",
  "message": "The requested endpoint does not exist",
  "path": "/seed",
  "hint": "Did you mean /sync/seed? (All sync endpoints start with /sync)"
}
```

## How to Use

### 1. Correct Endpoint Access

```bash
# ❌ WRONG (will show 404)
curl http://localhost:8080/seed

# ✅ CORRECT
curl http://localhost:8080/sync/seed
```

### 2. Browser Testing

Visit these URLs directly in your browser:
- `http://localhost:8080/sync/seed` - Seed database
- `http://localhost:8080/artists` - Get all artists
- `http://localhost:8080/albums` - Get all albums
- `http://localhost:8080/health/tidal` - Check TIDAL health

### 3. API Client Testing (Postman/Insomnia)

Use base URL: `http://localhost:8080`

Available endpoints:
- GET `/sync/seed`
- POST `/sync/seed`
- POST `/sync`
- POST `/sync/artists`
- POST `/sync/albums`
- GET `/artists`
- GET `/albums`
- And many more... (see `API_ENDPOINTS.md`)

## Common Mistakes & Fixes

| Mistake | Fix |
|---------|-----|
| `/seed` → 404 | Use `/sync/seed` |
| `/artist` → 404 | Use `/artists` |
| `/album` → 404 | Use `/albums` |
| `/sync` (GET) → 405 Method Not Allowed | Use POST instead: `POST /sync` |
| Query params not working | URL-encode spaces: `?q=query%20text` |

## Files Modified/Created

1. **GlobalExceptionHandler.java** (NEW)
   - Global exception handling for REST API
   - Provides helpful error messages
   - Catches 404 and other exceptions

2. **application.yaml** (MODIFIED)
   - Added `spring.mvc.throw-exception-if-no-handler-found: true`
   - Added `spring.web.resources.add-mappings: false`
   - Enables error handler integration

3. **API_ENDPOINTS.md** (NEW)
   - Complete API reference guide
   - All endpoints with examples
   - Common issues and solutions
   - curl/Postman examples

## Benefits

✅ **Better Error Messages** - Users see helpful hints instead of generic Whitelabel error
✅ **Clear Documentation** - Complete API reference available
✅ **Easier Debugging** - Error responses include hints for typos
✅ **Professional API** - JSON error responses following REST conventions
✅ **Reduced Support Burden** - Self-documenting API with clever error hints

## Quick Start

1. Access the correct endpoint: `http://localhost:8080/sync/seed`
2. Refer to `API_ENDPOINTS.md` for complete endpoint list
3. If you get 404, read the error message hint for correct path
4. Use the provided curl examples for testing

## Additional Notes

- The global exception handler catches all `NoHandlerFoundException` instances
- Error responses return JSON format, not HTML
- Status codes follow REST conventions (404 for Not Found, 400 for Bad Request, etc.)
- Hints are contextual based on the requested path
- All changes are backward compatible - existing endpoints work unchanged

