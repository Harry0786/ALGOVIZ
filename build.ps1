# Build Configuration Script
# Run this after initial setup

param(
    [ValidateSet("debug", "staging", "release")]
    [string]$buildType = "debug"
)

Write-Host "AlgoViz+ Build Configuration" -ForegroundColor Cyan
Write-Host "============================" -ForegroundColor Cyan

# Check Java version
Write-Host "`nChecking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host $javaVersion
    
    # Verify it's Java 17
    if ($javaVersion -notmatch "17\.") {
        Write-Host "WARNING: JDK 17 is required" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "ERROR: Java not found" -ForegroundColor Red
    exit 1
}

# Check Android SDK
Write-Host "`nChecking Android SDK..." -ForegroundColor Yellow
if ($env:ANDROID_HOME) {
    Write-Host "Android SDK: $env:ANDROID_HOME" -ForegroundColor Green
} else {
    Write-Host "WARNING: ANDROID_HOME not set" -ForegroundColor Red
}

# Check local.properties
Write-Host "`nChecking local.properties..." -ForegroundColor Yellow
if (Test-Path "local.properties") {
    Write-Host "local.properties exists" -ForegroundColor Green
    
    # Verify required properties exist
    $content = Get-Content "local.properties"
    if ($content -notmatch "sdk.dir=") {
        Write-Host "WARNING: sdk.dir not configured in local.properties" -ForegroundColor Red
        Write-Host "Please add your Android SDK path to local.properties" -ForegroundColor Yellow
    }
} else {
    Write-Host "ERROR: local.properties not found" -ForegroundColor Red
    Write-Host "Copy from local.properties.template and configure SDK path" -ForegroundColor Yellow
    exit 1
}

# Gradle sync
Write-Host "`nSyncing Gradle..." -ForegroundColor Yellow
.\gradlew.bat tasks --quiet

Write-Host "`nBuild Type: $buildType" -ForegroundColor Cyan

switch ($buildType) {
    "debug" {
        Write-Host "Building debug variant..." -ForegroundColor Yellow
        .\gradlew.bat assembleDebug
    }
    "staging" {
        Write-Host "Building staging variant..." -ForegroundColor Yellow
        .\gradlew.bat assembleStaging
    }
    "release" {
        Write-Host "Building release variant..." -ForegroundColor Yellow
        .\gradlew.bat assembleRelease
    }
    default {
        Write-Host "Invalid build type. Use: debug, staging, or release" -ForegroundColor Red
    }
}

Write-Host "`nBuild complete!" -ForegroundColor Green
