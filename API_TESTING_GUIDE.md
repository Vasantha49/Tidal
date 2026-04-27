# Music Service - Complete API Testing Guide

## 🚀 Quick Start

### 1. Start the Application

```bash
# Using Maven
./mvnw spring-boot:run

# Or using Docker
docker compose up -d
```

The application will be available at: `http://localhost:8080`

### 2. Set Environment Variables

Before testing, configure TIDAL credentials:

```bash
# Linux/Mac
export TIDAL_CLIENT_ID="your-client-id"
export TIDAL_CLIENT_SECRET="your-client-secret"
export TIDAL_COUNTRY_CODE="DE"

# Windows PowerShell
$env:TIDAL_CLIENT_ID="your-client-id"
$env:TIDAL_CLIENT_SECRET="your-client-secret"
$env:TIDAL_COUNTRY_CODE="DE"
```

---

## ✅ Test All Endpoints

### Phase 1: Health & Configuration Check

#### 1.1 Health Check - TIDAL Status
```bash
curl http://localhost:8080/health/tidal

# Expected Response (200 OK):
{
  "status": "UP",
  "credentialsConfigured": true,
  "message": "TIDAL access token acquired successfully"
}

# If NOT configured:
{
  "status": "DOWN",
  "credentialsConfigured": false,
  "message": "TIDAL client credentials are not configured"
}
```

**What it tests:**
- ✅ OAuth2 authorization working
- ✅ Token acquisition successful
- ✅ Credentials configured correctly
- ✅ TIDAL API connectivity

---

### Phase 2: Data Seeding

#### 2.1 Seed Database with Sample Artists
```bash
# GET request (for browser testing)
curl http://localhost:8080/sync/seed

# POST request (recommended for API testing)
curl -X POST http://localhost:8080/sync/seed

# Expected Response (204 No Content):
# Database is now seeded with ~10 popular artists
```

**What it tests:**
- ✅ OAuth2 token used for API calls
- ✅ TIDAL artist data fetching
- ✅ Database persistence
- ✅ Service layer integration

**Check results:**
```bash
curl http://localhost:8080/artists | jq '.[]' | head -20
```

---

### Phase 3: Artist Operations

#### 3.1 List All Artists
```bash
curl http://localhost:8080/artists

# Expected Response (200 OK):
[
  {
    "id": 1,
    "name": "Radiohead",
    "externalId": "7764",
    "manuallyEdited": false
  },
  {
    "id": 2,
    "name": "The Beatles",
    "externalId": "7763",
    "manuallyEdited": false
  }
]
```

**What it tests:**
- ✅ GET endpoint
- ✅ Database retrieval
- ✅ JSON serialization
- ✅ Response formatting

#### 3.2 Get Artist by ID
```bash
curl http://localhost:8080/artists/1

# Expected Response (200 OK):
{
  "id": 1,
  "name": "Radiohead",
  "externalId": "7764",
  "manuallyEdited": false
}

# If not found (404):
{
  "status": 404,
  "message": "Artist not found",
  "path": "/artists/999"
}
```

**What it tests:**
- ✅ Path parameter handling
- ✅ Entity lookup
- ✅ Error handling (404)

#### 3.3 Search Artists (Local Database)
```bash
curl "http://localhost:8080/artists/search?q=radiohead"

# Expected Response (200 OK):
[
  {
    "id": 1,
    "name": "Radiohead",
    "externalId": "7764",
    "manuallyEdited": false
  }
]
```

**What it tests:**
- ✅ Query parameter handling
- ✅ Full-text search
- ✅ Database search functionality

#### 3.4 Search Artists (TIDAL API)
```bash
curl "http://localhost:8080/artists/tidal/search?q=radiohead"

# Expected Response (200 OK):
[
  {
    "id": "7764",
    "name": "Radiohead"
  }
]
```

**What it tests:**
- ✅ OAuth2 token usage
- ✅ TIDAL API search integration
- ✅ External API communication
- ✅ Authorization working with real API

#### 3.5 Create Artist
```bash
curl -X POST http://localhost:8080/artists \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pink Floyd",
    "externalId": "7765"
  }'

# Expected Response (200 OK):
{
  "id": 11,
  "name": "Pink Floyd",
  "externalId": "7765",
  "manuallyEdited": true
}
```

**What it tests:**
- ✅ POST endpoint
- ✅ JSON request parsing
- ✅ Database insertion
- ✅ Manual edit flag handling

#### 3.6 Update Artist
```bash
curl -X PUT http://localhost:8080/artists/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Radiohead Updated",
    "externalId": "7764"
  }'

# Expected Response (200 OK):
{
  "id": 1,
  "name": "Radiohead Updated",
  "externalId": "7764",
  "manuallyEdited": true
}
```

**What it tests:**
- ✅ PUT endpoint
- ✅ Database update
- ✅ Manual edit tracking

#### 3.7 Delete Artist
```bash
curl -X DELETE http://localhost:8080/artists/11

# Expected Response (204 No Content):
# Artist deleted successfully
```

**What it tests:**
- ✅ DELETE endpoint
- ✅ Database deletion
- ✅ Cascading deletes (if albums exist)

---

### Phase 4: Album Operations

#### 4.1 Sync Albums from TIDAL
```bash
# Sync all artists' albums
curl -X POST http://localhost:8080/sync/albums

# Expected Response (204 No Content):
# Albums synced for all artists

# Check results:
curl http://localhost:8080/albums | jq 'length'
# Should return > 0
```

**What it tests:**
- ✅ OAuth2 token for multiple API calls
- ✅ Bulk TIDAL API operations
- ✅ Loop through artists and fetch albums
- ✅ Database bulk inserts

#### 4.2 List All Albums
```bash
curl http://localhost:8080/albums

# Expected Response (200 OK):
[
  {
    "id": 1,
    "title": "OK Computer",
    "externalId": "album123",
    "manuallyEdited": false,
    "artistId": 1,
    "artistName": "Radiohead"
  },
  {
    "id": 2,
    "title": "The Bends",
    "externalId": "album124",
    "manuallyEdited": false,
    "artistId": 1,
    "artistName": "Radiohead"
  }
]
```

**What it tests:**
- ✅ GET albums endpoint
- ✅ Artist relationship loading
- ✅ JSON response formatting

#### 4.3 Get Album by ID
```bash
curl http://localhost:8080/albums/1

# Expected Response (200 OK):
{
  "id": 1,
  "title": "OK Computer",
  "externalId": "album123",
  "manuallyEdited": false,
  "artistId": 1,
  "artistName": "Radiohead"
}
```

**What it tests:**
- ✅ Album retrieval
- ✅ Artist data population

#### 4.4 Search Albums
```bash
curl "http://localhost:8080/albums/search?q=computer"

# Expected Response (200 OK):
[
  {
    "id": 1,
    "title": "OK Computer",
    "externalId": "album123",
    "manuallyEdited": false,
    "artistId": 1,
    "artistName": "Radiohead"
  }
]
```

**What it tests:**
- ✅ Album search functionality

#### 4.5 Create Album
```bash
curl -X POST http://localhost:8080/albums \
  -H "Content-Type: application/json" \
  -d '{
    "title": "A Moon Shaped Pool",
    "externalId": "album999",
    "artistId": 1
  }'

# Expected Response (200 OK):
{
  "id": 50,
  "title": "A Moon Shaped Pool",
  "externalId": "album999",
  "manuallyEdited": true,
  "artistId": 1,
  "artistName": "Radiohead"
}
```

**What it tests:**
- ✅ Album creation
- ✅ Foreign key validation
- ✅ Manual edit tracking

#### 4.6 Update Album
```bash
curl -X PUT http://localhost:8080/albums/50 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "A Moon Shaped Pool - Updated",
    "externalId": "album999",
    "artistId": 1
  }'

# Expected Response (200 OK):
{
  "id": 50,
  "title": "A Moon Shaped Pool - Updated",
  "externalId": "album999",
  "manuallyEdited": true,
  "artistId": 1,
  "artistName": "Radiohead"
}
```

**What it tests:**
- ✅ Album update functionality

#### 4.7 Delete Album
```bash
curl -X DELETE http://localhost:8080/albums/50

# Expected Response (204 No Content):
# Album deleted successfully
```

**What it tests:**
- ✅ Album deletion

---

### Phase 5: Synchronization Operations

#### 5.1 Full Synchronization
```bash
# Sync both artists and albums
curl -X POST http://localhost:8080/sync

# Expected Response (204 No Content):
# Full sync completed
```

**What it tests:**
- ✅ Orchestration between multiple sync operations
- ✅ OAuth2 token reuse across multiple calls

#### 5.2 Sync Artists Only
```bash
curl -X POST http://localhost:8080/sync/artists

# Expected Response (204 No Content):
```

**What it tests:**
- ✅ Artist-specific sync

#### 5.3 Sync Albums Only
```bash
curl -X POST http://localhost:8080/sync/albums

# Expected Response (204 No Content):
```

**What it tests:**
- ✅ Album-specific sync
- ✅ Token management across many API calls

---

## 🧪 Automated Testing Script

### PowerShell Test Script

Create `test-all-endpoints.ps1`:

```powershell
# Music Service API Testing Script

$baseUrl = "http://localhost:8080"
$testsRun = 0
$testsPassed = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method = "GET",
        [string]$Endpoint,
        [object]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    $testsRun++
    
    $url = "$baseUrl$Endpoint"
    
    try {
        if ($Body) {
            $response = Invoke-WebRequest -Uri $url -Method $Method -Body ($Body | ConvertTo-Json) -ContentType "application/json" -SkipHttpErrorCheck
        } else {
            $response = Invoke-WebRequest -Uri $url -Method $Method -SkipHttpErrorCheck
        }
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host "✅ PASS: $Name" -ForegroundColor Green
            $testsPassed++
        } else {
            Write-Host "❌ FAIL: $Name (Expected $ExpectedStatus, got $($response.StatusCode))" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ ERROR: $Name - $_" -ForegroundColor Red
    }
}

Write-Host "🚀 Starting Music Service API Tests" -ForegroundColor Cyan
Write-Host "Base URL: $baseUrl`n" -ForegroundColor Cyan

# Phase 1: Health Check
Write-Host "`n📋 Phase 1: Health Check" -ForegroundColor Yellow
Test-Endpoint "Health Check" "GET" "/health/tidal" $null 200

# Phase 2: Seed Data
Write-Host "`n📋 Phase 2: Seed Data" -ForegroundColor Yellow
Test-Endpoint "Seed Artists" "POST" "/sync/seed" $null 204

# Phase 3: Artists
Write-Host "`n📋 Phase 3: Artist Operations" -ForegroundColor Yellow
Test-Endpoint "List Artists" "GET" "/artists" $null 200
Test-Endpoint "Get Artist" "GET" "/artists/1" $null 200
Test-Endpoint "Search Artists" "GET" "/artists/search?q=radiohead" $null 200
Test-Endpoint "Create Artist" "POST" "/artists" @{name="Test Artist"; externalId="test123"} 200
Test-Endpoint "Update Artist" "PUT" "/artists/1" @{name="Radiohead"; externalId="7764"} 200

# Phase 4: Albums
Write-Host "`n📋 Phase 4: Album Operations" -ForegroundColor Yellow
Test-Endpoint "Sync Albums" "POST" "/sync/albums" $null 204
Test-Endpoint "List Albums" "GET" "/albums" $null 200
Test-Endpoint "Search Albums" "GET" "/albums/search?q=ok" $null 200

# Phase 5: TIDAL Search
Write-Host "`n📋 Phase 5: TIDAL Integration" -ForegroundColor Yellow
Test-Endpoint "TIDAL Artist Search" "GET" "/artists/tidal/search?q=radiohead" $null 200

Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "Test Results: $testsPassed / $testsRun Passed" -ForegroundColor $(if ($testsPassed -eq $testsRun) { "Green" } else { "Yellow" })
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
```

Run it:
```bash
./test-all-endpoints.ps1
```

---

## 📊 Expected Test Results

### All Tests Passing
```
✅ PASS: Health Check
✅ PASS: Seed Artists
✅ PASS: List Artists
✅ PASS: Get Artist
✅ PASS: Search Artists
✅ PASS: Create Artist
✅ PASS: Update Artist
✅ PASS: Sync Albums
✅ PASS: List Albums
✅ PASS: Search Albums
✅ PASS: TIDAL Artist Search

Test Results: 11 / 11 Passed ✅
```

### If Authorization Fails
```
❌ FAIL: Health Check
❌ FAIL: TIDAL Artist Search
```

**Solution:** Check TIDAL credentials configuration
```bash
# Verify credentials are set
echo $TIDAL_CLIENT_ID
echo $TIDAL_CLIENT_SECRET

# Check application logs
# Look for: "TIDAL client credentials are not configured"
```

---

## 🔍 Debugging & Diagnostics

### Enable Debug Logging

Edit `application.yaml`:
```yaml
logging:
  level:
    com.immomio.tidal.music: DEBUG
    org.springframework.web.reactive.function.client: DEBUG
```

### Watch Application Logs

```bash
# Using Docker
docker compose logs -f app

# Using Maven
./mvnw spring-boot:run | grep -i "tidal\|error"
```

### Check Database State

```bash
# List all artists
curl http://localhost:8080/artists | jq 'length'

# List all albums
curl http://localhost:8080/albums | jq 'length'

# Check specific artist
curl http://localhost:8080/artists/1 | jq '.'
```

---

## ⚠️ Common Issues & Solutions

### Issue: "TIDAL client credentials are not configured"

**Cause:** Environment variables not set

**Solution:**
```bash
# Set credentials
export TIDAL_CLIENT_ID="your-id"
export TIDAL_CLIENT_SECRET="your-secret"

# Restart app
./mvnw spring-boot:run
```

### Issue: "401 Unauthorized"

**Cause:** Invalid credentials or expired token

**Solution:**
```bash
# 1. Verify credentials
echo "Client ID: $TIDAL_CLIENT_ID"
echo "Client Secret: $TIDAL_CLIENT_SECRET"

# 2. Check health
curl http://localhost:8080/health/tidal

# 3. Enable debug logging
# Edit application.yaml with DEBUG level
```

### Issue: "404 Not Found"

**Cause:** Wrong endpoint path

**Solution:**
- All sync endpoints are under `/sync`
- All artist endpoints are under `/artists`
- All album endpoints are under `/albums`

### Issue: Database Connection Failed

**Cause:** PostgreSQL not running

**Solution:**
```bash
# Start database
docker compose up -d db

# Verify connection
docker compose logs db
```

---

## ✅ Complete Verification Checklist

Before deployment:

- [ ] Health endpoint returns UP status
- [ ] Can create artists
- [ ] Can create albums
- [ ] Can search locally
- [ ] Can search TIDAL API
- [ ] Sync endpoints work
- [ ] Error handling shows helpful messages
- [ ] Token caching working (check logs)
- [ ] Manual edit flag updates correctly
- [ ] Database persists data across restarts

---

## 📈 Performance Benchmarks

Expected response times:

| Operation | Expected Time | Notes |
|-----------|---------------|-------|
| List Artists | < 100ms | Depends on record count |
| Search (local) | < 200ms | Full-text search |
| Get Artist (by ID) | < 50ms | Indexed lookup |
| Create Artist | < 100ms | Database insert |
| Search TIDAL | 500-2000ms | External API call |
| Sync Artists (10) | 5-10 sec | API calls + DB inserts |
| Sync Albums (500) | 30-60 sec | Many API calls |

---

## 🚀 Ready for Testing!

All endpoints are now ready to test. Follow the phases above to verify:

1. ✅ Authorization working with TIDAL OAuth2
2. ✅ Database operations functioning
3. ✅ API Integration operational
4. ✅ Error handling comprehensive
5. ✅ Full workflow complete

**Run the full test cycle:**
```bash
# 1. Start application
./mvnw spring-boot:run

# 2. In another terminal, run tests
./test-all-endpoints.ps1

# 3. Verify all tests pass ✅
```


