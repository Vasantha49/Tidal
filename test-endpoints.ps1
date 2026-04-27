#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Music Service API Testing Script
.DESCRIPTION
    Tests all endpoints in the Music Service API
#>

$BASE_URL = "http://localhost:8080"
$PASSED = 0
$FAILED = 0

Write-Host "`n========== MUSIC SERVICE API TEST SUITE ==========" -ForegroundColor Cyan
Write-Host "Testing Base URL: $BASE_URL`n" -ForegroundColor Cyan

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method = "GET",
        [string]$Endpoint,
        [hashtable]$Body = $null,
        [int]$ExpectedStatus = 200
    )

    $url = "$BASE_URL$Endpoint"

    try {
        $ProgressPreference = 'SilentlyContinue'

        if ($Body) {
            $bodyJson = $Body | ConvertTo-Json
            $response = Invoke-WebRequest -Uri $url -Method $Method `
                -Body $bodyJson -ContentType "application/json" `
                -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
        } else {
            $response = Invoke-WebRequest -Uri $url -Method $Method `
                -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
        }

        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host "[PASS] $Name" -ForegroundColor Green
            $script:PASSED++
            return $true
        } else {
            Write-Host "[FAIL] $Name - Expected $ExpectedStatus, got $($response.StatusCode)" -ForegroundColor Red
            $script:FAILED++
            return $false
        }
    } catch {
        if ($ExpectedStatus -eq 204) {
            Write-Host "[PASS] $Name" -ForegroundColor Green
            $script:PASSED++
            return $true
        } else {
            Write-Host "[FAIL] $Name - Error: $($_.Exception.Message)" -ForegroundColor Red
            $script:FAILED++
            return $false
        }
    }
}

# Phase 1: Health Check
Write-Host "`n[Phase 1] Health & Configuration Check" -ForegroundColor Yellow
Test-Endpoint "Health Check - TIDAL Status" "GET" "/health/tidal" $null 200

# Phase 2: Seed Data
Write-Host "`n[Phase 2] Data Seeding" -ForegroundColor Yellow
Test-Endpoint "Seed Artists (POST)" "POST" "/sync/seed" $null 204

# Phase 3: Artist Operations
Write-Host "`n[Phase 3] Artist Operations" -ForegroundColor Yellow
Test-Endpoint "List All Artists" "GET" "/artists" $null 200
Test-Endpoint "Get Single Artist" "GET" "/artists/1" $null 200
Test-Endpoint "Search Artists (Local)" "GET" "/artists/search?q=radiohead" $null 200
Test-Endpoint "Create Artist" "POST" "/artists" @{"name"="Test Artist";"externalId"="test123"} 200
Test-Endpoint "Update Artist" "PUT" "/artists/1" @{"name"="Radiohead Updated";"externalId"="7764"} 200

# Phase 4: Album Operations
Write-Host "`n[Phase 4] Album Operations" -ForegroundColor Yellow
Test-Endpoint "Sync Albums" "POST" "/sync/albums" $null 204
Test-Endpoint "List All Albums" "GET" "/albums" $null 200
Test-Endpoint "Search Albums" "GET" "/albums/search?q=ok" $null 200

# Phase 5: TIDAL Integration
Write-Host "`n[Phase 5] TIDAL Integration" -ForegroundColor Yellow
Test-Endpoint "Search TIDAL Artists" "GET" "/artists/tidal/search?q=radiohead" $null 200

# Phase 6: Sync Operations
Write-Host "`n[Phase 6] Sync Operations" -ForegroundColor Yellow
Test-Endpoint "Full Sync" "POST" "/sync" $null 204
Test-Endpoint "Sync Artists Only" "POST" "/sync/artists" $null 204

# Summary
Write-Host "`n========== TEST RESULTS ==========" -ForegroundColor Cyan
Write-Host "PASSED: $PASSED" -ForegroundColor Green
Write-Host "FAILED: $FAILED" -ForegroundColor $(if ($FAILED -gt 0) { "Red" } else { "Green" })
Write-Host "TOTAL:  $($PASSED + $FAILED)" -ForegroundColor Cyan

if ($FAILED -eq 0) {
    Write-Host "`nAll tests PASSED! [OK]" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`nSome tests FAILED! [ERROR]" -ForegroundColor Red
    exit 1
}

