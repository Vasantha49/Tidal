# Music Service - API Endpoints Reference

## Base URL
```
http://localhost:8080
```

## Sync Endpoints

### Seed Artists (Create Sample Data)
```bash
# GET (for browser testing)
GET /sync/seed

# POST (for API clients)
POST /sync/seed

# curl example:
curl -X GET http://localhost:8080/sync/seed
curl -X POST http://localhost:8080/sync/seed
```

### Full Sync (Artists + Albums)
```bash
POST /sync

# curl example:
curl -X POST http://localhost:8080/sync
```

### Sync Artists Only
```bash
POST /sync/artists

# curl example:
curl -X POST http://localhost:8080/sync/artists
```

### Sync Albums Only
```bash
POST /sync/albums

# curl example:
curl -X POST http://localhost:8080/sync/albums
```

---

## Artist Endpoints

### Get All Artists
```bash
GET /artists

# curl example:
curl http://localhost:8080/artists
```

### Get Single Artist
```bash
GET /artists/{id}

# curl example:
curl http://localhost:8080/artists/1
```

### Search Artists (Local Database)
```bash
GET /artists/search?q={query}

# curl example:
curl "http://localhost:8080/artists/search?q=radiohead"
```

### Search Artists (TIDAL Live)
```bash
GET /artists/tidal/search?q={query}

# curl example:
curl "http://localhost:8080/artists/tidal/search?q=radiohead"
```

### Create Artist (with TIDAL ID)
```bash
GET /artists/tidal?name={name}&externalId={tidalId}
POST /artists/tidal?name={name}&externalId={tidalId}

# curl examples:
curl "http://localhost:8080/artists/tidal?name=Radiohead&externalId=7764"
curl -X POST "http://localhost:8080/artists/tidal?name=Radiohead&externalId=7764"
```

### Create Artist (Standard)
```bash
POST /artists
Content-Type: application/json

{
  "name": "Massive Attack",
  "externalId": "7654321"
}

# curl example:
curl -X POST http://localhost:8080/artists \
  -H "Content-Type: application/json" \
  -d '{"name":"Massive Attack","externalId":"7654321"}'
```

### Update Artist
```bash
PUT /artists/{id}
Content-Type: application/json

{
  "name": "Updated Name",
  "externalId": "new-id"
}

# curl example:
curl -X PUT http://localhost:8080/artists/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Name","externalId":"new-id"}'
```

### Delete Artist
```bash
DELETE /artists/{id}

# curl example:
curl -X DELETE http://localhost:8080/artists/1
```

---

## Album Endpoints

### Get All Albums
```bash
GET /albums

# curl example:
curl http://localhost:8080/albums
```

### Get Single Album
```bash
GET /albums/{id}

# curl example:
curl http://localhost:8080/albums/1
```

### Search Albums (Local Database)
```bash
GET /albums/search?q={query}

# curl example:
curl "http://localhost:8080/albums/search?q=mezzanine"
```

### Create Album
```bash
POST /albums
Content-Type: application/json

{
  "title": "Mezzanine",
  "externalId": "tidal-album-id",
  "artistId": 1
}

# curl example:
curl -X POST http://localhost:8080/albums \
  -H "Content-Type: application/json" \
  -d '{"title":"Mezzanine","externalId":"album123","artistId":1}'
```

### Update Album
```bash
PUT /albums/{id}
Content-Type: application/json

{
  "title": "Updated Title",
  "externalId": "new-external-id",
  "artistId": 1
}

# curl example:
curl -X PUT http://localhost:8080/albums/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated","externalId":"new-id","artistId":1}'
```

### Delete Album
```bash
DELETE /albums/{id}

# curl example:
curl -X DELETE http://localhost:8080/albums/1
```

---

## Health Check

### Check TIDAL Service Health
```bash
GET /health/tidal

# curl example:
curl http://localhost:8080/health/tidal

# Response (UP):
{
  "status": "UP",
  "credentialsConfigured": true,
  "message": "TIDAL access token acquired successfully"
}

# Response (DOWN):
{
  "status": "DOWN",
  "credentialsConfigured": false,
  "message": "TIDAL client credentials are not configured"
}
```

---

## Common Issues

### 404 Not Found - /seed
**Error:** `localhost:8080/seed` → 404 Not Found

**Solution:** Use `/sync/seed` instead
```bash
# ❌ WRONG
curl http://localhost:8080/seed

# ✅ CORRECT
curl http://localhost:8080/sync/seed
```

### 404 Not Found - Unknown Endpoint
**Error:** Accessing non-existent endpoint → 404 Not Found

**Solution:** Check this reference guide for correct endpoint paths

### Query Parameter Not Working
**Example:** GET request with parameters must be URL-encoded
```bash
# ✅ CORRECT - Use encoded spaces
curl "http://localhost:8080/artists/search?q=query%20text"

# ✅ ALSO CORRECT - Use quotes
curl 'http://localhost:8080/artists/search?q=query text'
```

---

## Request/Response Examples

### Success Response (200 OK)
```json
{
  "id": 1,
  "name": "Radiohead",
  "externalId": "7764",
  "manuallyEdited": false
}
```

### Not Found Response (404)
```html
Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.

Sun Apr 26 11:44:00 CEST 2026
There was an unexpected error (type=Not Found, status=404).
```

### Bad Request Response (400)
```json
{
  "timestamp": "2026-04-26T11:44:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request parameters"
}
```

---

## Testing with Browser

For quick testing, try these endpoints directly in your browser:

```
http://localhost:8080/artists
http://localhost:8080/albums
http://localhost:8080/health/tidal
http://localhost:8080/sync/seed
http://localhost:8080/artists/search?q=radiohead
http://localhost:8080/artists/tidal/search?q=radiohead
```

---

## Testing with Postman/Insomnia

1. Import this collection or manually create requests
2. Set base URL to `http://localhost:8080`
3. Use the endpoint paths listed above
4. For POST/PUT, set `Content-Type: application/json` header
5. Include request body for POST/PUT operations

---

## API Rate Limits

- No rate limiting configured in development
- Production deployments may implement rate limiting
- TIDAL API limits: 100 requests per second per token

---

## Authentication

- Currently using TIDAL OAuth2 client credentials
- Set `TIDAL_CLIENT_ID` and `TIDAL_CLIENT_SECRET` environment variables
- No authentication required for local endpoints
- TIDAL endpoints require valid credentials for data fetching

