#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Comprehensive API Testing Script for Music Service

.DESCRIPTION
    Tests all endpoints in the Music Service API

.NOTES
    Prerequisites:
    - Application running on http://localhost:8080
    - PostgreSQL running
    - curl installed
#>

$BASE_URL = "http://localhost:8080"
$PASSED = 0
$FAILED = 0

# Color codes
$Green = "`e[32m"
$Red = "`e[31m"
$Yellow = "`e[33m"
$Blue = "`e[34m"
$Reset = "`e[0m"

function Print-Header {
    param([string]$Header)
    Write-Host "`n$Blue========== $Header ==========$Reset"
}

function Print-Test {
    param([string]$TestName, [bool]$Success, [string]$Response = "")
    if ($Success) {
        Write-Host "$Green✓ PASS$Reset: $TestName"
        $script:PASSED++
    } else {
        Write-Host "$Red✗ FAIL$Reset: $TestName"
        if ($Response) {
            Write-Host "  Response: $Red$Response$Reset"
        }
        $script:FAILED++
    }
}

function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$TestName,
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )

    try {
        $Uri = "$BASE_URL$Endpoint"
        $Headers = @{"Content-Type" = "application/json"}

        $Params = @{
            Uri         = $Uri
            Method      = $Method
            Headers     = $Headers
            ErrorAction = 'Stop'
        }

        if ($Body) {
            $Params["Body"] = $Body
        }

        $Response = Invoke-WebRequest @Params
        $Success = $Response.StatusCode -eq $ExpectedStatus
        Print-Test $TestName $Success

        return $Response.Content | ConvertFrom-Json
    }
    catch {
        $ErrorMsg = $_.Exception.Response.StatusCode.Value__
        Print-Test $TestName $false "HTTP $ErrorMsg"
        return $null
    }
}

# ===== START TESTS =====

Write-Host "`n$Blue╔════════════════════════════════════════════════╗$Reset"
Write-Host "$Blue║    MUSIC SERVICE API - COMPREHENSIVE TEST      ║$Reset"
Write-Host "$Blue╚════════════════════════════════════════════════╝$Reset"

# 1. HEALTH CHECK
Print-Header "1. Health & Status Endpoints"

Test-Endpoint "GET" "/health/tidal" "Check TIDAL Health"

# 2. SYNC ENDPOINTS
Print-Header "2. Sync Endpoints"

Test-Endpoint "GET" "/sync/seed" "Seed Database (GET)"
Start-Sleep -Seconds 2

Test-Endpoint "POST" "/sync/albums" "Sync Albums from TIDAL"

# 3. ARTIST ENDPOINTS
Print-Header "3. Artist Endpoints - Read"

$AllArtists = Test-Endpoint "GET" "/artists" "Get All Artists"
if ($AllArtists) {
    Write-Host "  Found $($AllArtists.Count) artists"
    if ($AllArtists.Count -gt 0) {
        $FirstArtistId = $AllArtists[0].id
        Write-Host "  Using Artist ID: $FirstArtistId"

        Test-Endpoint "GET" "/artists/$FirstArtistId" "Get Single Artist by ID"
    }
}

Test-Endpoint "GET" "/artists/search?q=radiohead" "Search Artists (Local)"
Test-Endpoint "GET" "/artists/tidal/search?q=radiohead" "Search Artists (TIDAL)"

# 4. ARTIST ENDPOINTS - CREATE
Print-Header "4. Artist Endpoints - Create"

$ArtistBody = @{
    name = "Test Artist $([DateTime]::Now.Ticks)"
    externalId = "test-artist-$([DateTime]::Now.Ticks)"
} | ConvertTo-Json

$CreatedArtist = Test-Endpoint "POST" "/artists" "Create Artist (POST /artists)" $ArtistBody
if ($CreatedArtist) {
    $CreatedArtistId = $CreatedArtist.id
    Write-Host "  Created Artist ID: $CreatedArtistId"

    # 5. ARTIST ENDPOINTS - UPDATE
    Print-Header "5. Artist Endpoints - Update & Delete"

    $UpdateBody = @{
        name = "Updated Artist Name"
        externalId = "updated-external-id"
    } | ConvertTo-Json

    Test-Endpoint "PUT" "/artists/$CreatedArtistId" "Update Artist (PUT)" $UpdateBody

    # 6. ALBUM ENDPOINTS - READ
    Print-Header "6. Album Endpoints - Read"

    $AllAlbums = Test-Endpoint "GET" "/albums" "Get All Albums"
    if ($AllAlbums) {
        Write-Host "  Found $($AllAlbums.Count) albums"
        if ($AllAlbums.Count -gt 0) {
            $FirstAlbumId = $AllAlbums[0].id
            Test-Endpoint "GET" "/albums/$FirstAlbumId" "Get Single Album by ID"
        }
    }

    Test-Endpoint "GET" "/albums/search?q=ok" "Search Albums (Local)"

    # 7. ALBUM ENDPOINTS - CREATE
    Print-Header "7. Album Endpoints - Create"

    $AlbumBody = @{
        title = "Test Album $([DateTime]::Now.Ticks)"
        externalId = "test-album-$([DateTime]::Now.Ticks)"
        artistId = $CreatedArtistId
    } | ConvertTo-Json

    $CreatedAlbum = Test-Endpoint "POST" "/albums" "Create Album (POST /albums)" $AlbumBody
    if ($CreatedAlbum) {
        $CreatedAlbumId = $CreatedAlbum.id
        Write-Host "  Created Album ID: $CreatedAlbumId"

        # 8. ALBUM ENDPOINTS - UPDATE
        Print-Header "8. Album Endpoints - Update & Delete"

        $UpdateAlbumBody = @{
            title = "Updated Album Title"
            externalId = "updated-album-id"
            artistId = $CreatedArtistId
        } | ConvertTo-Json

        Test-Endpoint "PUT" "/albums/$CreatedAlbumId" "Update Album (PUT)" $UpdateAlbumBody

        # 9. DELETE OPERATIONS
        Print-Header "9. Delete Operations"

        Test-Endpoint "DELETE" "/albums/$CreatedAlbumId" "Delete Album" "" 204
        Test-Endpoint "DELETE" "/artists/$CreatedArtistId" "Delete Artist" "" 204
    }
}

# 10. TIDAL CREATE ARTIST ENDPOINT
Print-Header "10. Special Endpoints - TIDAL"

$TidalArtistName = "Test TIDAL Artist $([DateTime]::Now.Ticks)"
$TidalArtistId = "tidal-test-$([DateTime]::Now.Ticks)"

Test-Endpoint "GET" "/artists/tidal?name=$([Uri]::EscapeDataString($TidalArtistName))&externalId=$TidalArtistId" `
    "Create Artist with TIDAL ID (GET)"

# ===== SUMMARY =====

Print-Header "TEST SUMMARY"

$Total = $PASSED + $FAILED
$PassRate = if ($Total -gt 0) { [math]::Round(($PASSED / $Total) * 100, 1) } else { 0 }

Write-Host "
$Green✓ Passed: $PASSED$Reset
$Red✗ Failed: $FAILED$Reset
Total:  $Total
Pass Rate: $PassRate%
"

if ($FAILED -eq 0) {
    Write-Host "$Green╔════════════════════════════════════╗$Reset"
    Write-Host "$Green║  ALL TESTS PASSED! 🎉             ║$Reset"
    Write-Host "$Green╚════════════════════════════════════╝$Reset"
    exit 0
} else {
    Write-Host "$Red╔════════════════════════════════════╗$Reset"
    Write-Host "$Red║   SOME TESTS FAILED               ║$Reset"
    Write-Host "$Red╚════════════════════════════════════╝$Reset"
    exit 1
}

