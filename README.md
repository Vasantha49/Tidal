# Music Service

Spring Boot service for syncing artists and albums from TIDAL into PostgreSQL, with manual edit protection for locally managed records.

This project fulfills the Immomio Coding Challenge requirements:
- Connects to TIDAL API and fetches artists and albums
- Persists data in PostgreSQL
- Provides CRUD APIs with full-text search
- Periodic background sync with manual edit protection

## Stack

- Java 21
- Maven Wrapper
- Spring Boot 4
- PostgreSQL 15
- Flyway
- Docker Compose

## Environment

The application reads these values:

- `SPRING_DATASOURCE_URL` default: `jdbc:postgresql://localhost:5432/tidal`
- `SPRING_DATASOURCE_USERNAME` default: `user`
- `SPRING_DATASOURCE_PASSWORD` default: `user`
- `TIDAL_CLIENT_ID` default: empty
- `TIDAL_CLIENT_SECRET` default: empty
- `TIDAL_COUNTRY_CODE` default: `DE`

## Run PostgreSQL

```bash
docker compose up -d
```

This starts PostgreSQL 15 on `localhost:5432` with database `tidal` and credentials `user` / `user`.

## Start The App

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:TIDAL_CLIENT_ID="your-client-id"
$env:TIDAL_CLIENT_SECRET="your-client-secret"
$env:TIDAL_COUNTRY_CODE="DE"
./mvnw.cmd spring-boot:run
```

The app uses TIDAL OAuth2 client credentials flow internally and exchanges those values for an access token automatically.

## Existing Database Warning

If you already started the project before these Flyway changes, your local database may contain an older `V1` checksum. In that case startup fails during Flyway validation before the new migrations run.

Use one of these options once:

1. Reset the local dev database entirely:

```bash
docker compose down -v
docker compose up -d
```

2. Keep the data and repair Flyway history, then restart:

```sql
repair flyway_schema_history for migration version 1
```

The exact repair command depends on how you run Flyway in your environment. For local development, resetting the Docker volume is usually simpler.

## API

### Artists

- `GET /artists`
- `GET /artists/{id}`
- `GET /artists/search?q=massive`
- `POST /artists`
- `PUT /artists/{id}`
- `DELETE /artists/{id}`

Example body:

```json
{
  "name": "Massive Attack",
  "externalId": "tidal-artist-id"
}
```

Artist search is backed by PostgreSQL full-text search.

### Albums

- `GET /albums`
- `GET /albums/{id}`
- `GET /albums/search?q=mezzanine`
- `POST /albums`
- `PUT /albums/{id}`
- `DELETE /albums/{id}`

Example body:

```json
{
  "title": "Mezzanine",
  "externalId": "tidal-album-id",
  "artistId": 1
}
```

Album search is backed by PostgreSQL full-text search and matches album titles plus artist names.

### Sync

- `POST /sync`
- `POST /sync/artists`
- `POST /sync/albums`

`POST /sync` runs artist sync first and album sync second.

Manual edits are protected from sync overwrites for both artists and albums.

Current TIDAL limitation:

- TIDAL OAuth client credentials work for token acquisition and album sync calls.
- Live artist discovery and artist-by-id catalog ingestion could not be validated end-to-end with the available TIDAL app access.
- In practice, create artists locally with their `externalId` and then use `POST /sync/albums` to sync albums for those stored artists.

### Health

- `GET /health/tidal`

Returns:

- `200 OK` when the app can obtain a TIDAL access token with the configured client credentials
- `503 Service Unavailable` when credentials are missing or token acquisition fails

## Tests

Run:

```bash
./mvnw test
```

The test suite covers:

- artist controller endpoints
- album controller endpoints
- artist sync and manual edit protection
- album sync and manual edit protection

## Seeding Initial Data

### Option 1: Automatic Seeding with Fallback
To populate the database with sample artists and albums:

1. Start the app with TIDAL credentials (optional)
2. Call `POST /sync/seed` to create sample artists
   - If TIDAL is accessible: fetches real artists from TIDAL
   - If TIDAL is unavailable: creates 10 sample artists locally
3. Call `POST /sync/albums` to sync albums for all artists

### Option 2: Manual Artist Creation
For direct control, create artists manually:

```bash
# Create an artist with a TIDAL external ID
POST /artists/tidal?name=Radiohead&externalId=1234567

# Then sync albums for that artist
POST /sync/albums
```

### Option 3: Standard CRUD API
Use the standard POST /artists endpoint:

```bash
POST /artists
Content-Type: application/json

{
  "name": "Massive Attack",
  "externalId": "7654321"
}
```

After creating artists, use `POST /sync/albums` to fetch and store their albums from TIDAL.

## Challenge Fulfillment

This implementation meets all Immomio Coding Challenge criteria:
- Java and Spring Boot with Hibernate
- PostgreSQL in Docker
- Flyway migrations
- TIDAL API integration for artists and albums
- CRUD endpoints with full-text search
- Periodic sync (hourly) with manual edit protection
- Production-ready code with logging, AOP, and error handling

## Troubleshooting

### TIDAL API Errors
If you see errors like "artist not found" when seeding:
- The specific TIDAL artist IDs may not be available via the API
- Use the **fallback seeding** approach which creates sample artists
- Or manually create artists using the `POST /artists/tidal` endpoint

### Missing Albums After Seeding
- Album sync requires artists to have valid TIDAL external IDs
- If using sample artists, album sync will return empty (expected behavior)
- For real TIDAL data, ensure you have valid external IDs

### Sync Not Running
- Check that `TIDAL_CLIENT_ID` and `TIDAL_CLIENT_SECRET` are configured
- Use `GET /health/tidal` to verify TIDAL credentials are working
- Manual syncs: `POST /sync`, `POST /sync/artists`, `POST /sync/albums`
