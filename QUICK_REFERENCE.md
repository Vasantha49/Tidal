# Quick Reference - Music Service API

> **✅ All TIDAL API endpoints verified against official reference - April 27, 2026**
> See [ENDPOINT_VERIFICATION_APRIL_27.md](ENDPOINT_VERIFICATION_APRIL_27.md) for detailed verification report

## ⚡ System Requirements

Before starting, ensure you have:
- **Java 21+** - `java -version`
- **Docker** - `docker --version`
- **Docker Compose 2.0+** - `docker compose --version`

**Installation:** See SUBMISSION_GUIDE.md or GITHUB_README.md for detailed setup

---

##  Quick Start

```bash
# Start the app
./mvnw spring-boot:run

# Seed database with sample data
curl http://localhost:8080/sync/seed

# Get all artists
curl http://localhost:8080/artists

# Get all albums
curl http://localhost:8080/albums

# Check TIDAL health
curl http://localhost:8080/health/tidal
```

## All Sync Endpoints

```
GET  /sync/seed              ← Seed sample artists
POST /sync/seed              ← Seed sample artists
POST /sync                   ← Full sync (artists + albums)
POST /sync/artists           ← Sync artists only
POST /sync/albums            ← Sync albums only
```

## 👥 Artist Endpoints

```
GET    /artists                        ← List all
GET    /artists/{id}                   ← Get one
GET    /artists/search?q={query}       ← Search locally
GET    /artists/tidal/search?q={query} ← Search TIDAL
GET    /artists/tidal?name=X&externalId=Y     ← Create with TIDAL ID
POST   /artists/tidal?name=X&externalId=Y     ← Create with TIDAL ID
POST   /artists                        ← Create
PUT    /artists/{id}                   ← Update
DELETE /artists/{id}                   ← Delete
```

## 💿 Album Endpoints

```
GET    /albums                  ← List all
GET    /albums/{id}             ← Get one
GET    /albums/search?q={query} ← Search
POST   /albums                  ← Create
PUT    /albums/{id}             ← Update
DELETE /albums/{id}             ← Delete
```

## ❤️ Health Endpoint

```
GET /health/tidal              ← Check TIDAL API status
```

## ⚠️ Common 404 Errors & Fixes

| You tried | Use instead |
|-----------|------------|
| `/seed` | `/sync/seed` |
| `/artist` | `/artists` |
| `/album` | `/albums` |

## 📝 Example Requests

### Seed Database
```bash
curl -X GET http://localhost:8080/sync/seed
```

### Create Artist
```bash
curl -X POST http://localhost:8080/artists \
  -H "Content-Type: application/json" \
  -d '{"name":"Radiohead","externalId":"7764"}'
```

### Search Artists Locally
```bash
curl "http://localhost:8080/artists/search?q=radiohead"
```

### Search Artists on TIDAL
```bash
curl "http://localhost:8080/artists/tidal/search?q=radiohead"
```

### Create Album for Artist ID 1
```bash
curl -X POST http://localhost:8080/albums \
  -H "Content-Type: application/json" \
  -d '{"title":"OK Computer","externalId":"album123","artistId":1}'
```

### Sync Albums
```bash
curl -X POST http://localhost:8080/sync/albums
```

## 📖 Documentation Files

| File | Purpose |
|------|---------|
| `API_ENDPOINTS.md` | Complete API reference |
| `ERROR_HANDLING.md` | Error handling & troubleshooting |
| `ProjectFilesExplanation.md` | Project structure overview |
| `README.md` | Getting started guide |

## 🔧 Configuration

### Environment Variables
```bash
export TIDAL_CLIENT_ID="your-client-id"
export TIDAL_CLIENT_SECRET="your-client-secret"
export TIDAL_COUNTRY_CODE="DE"
```

### Application Config
```yaml
# application.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tidal
    username: user
    password: user
```

## ✅ Verification Checklist

- [ ] App runs without warnings
- [ ] `/sync/seed` endpoint works
- [ ] Artists are created in database
- [ ] Albums can be synced
- [ ] Search endpoints work
- [ ] Error messages are helpful

## 🐛 Troubleshooting

**Getting 404 errors?**
- Check endpoint path starts with `/sync` for sync operations
- See `ERROR_HANDLING.md` for helpful error messages

**Getting 405 Method Not Allowed?**
- Use POST for sync endpoints: `/sync`, `/sync/artists`, `/sync/albums`
- Use GET for read operations: `/artists`, `/albums`, `/health/tidal`

**Music Service not starting?**
- Check PostgreSQL is running: `docker compose up -d`
- Check logs for error details
- Verify environment variables are set

**No data after seeding?**
- Run `POST /sync/albums` to fetch albums for seeded artists
- Check `GET /health/tidal` to verify TIDAL connection

---

**Need more details?** See `API_ENDPOINTS.md` for complete documentation.

