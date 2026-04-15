param(
    [Parameter(Mandatory = $true)]
    [string]$VersionName,

    [Parameter(Mandatory = $false)]
    [int]$VersionCode = 0,

    [Parameter(Mandatory = $false)]
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

function Run-Git {
    param([string]$GitArgs)
    $output = & cmd /c "git $GitArgs" 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "git $GitArgs failed: $output"
    }
    return $output
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $repoRoot

try {
    if ($VersionName -notmatch '^\d+\.\d+\.\d+$') {
        throw "VersionName must be in format X.Y.Z (example: 1.2.0)"
    }

    $currentBranch = (Run-Git "branch --show-current").Trim()
    if ($currentBranch -ne "master") {
        throw "Current branch is '$currentBranch'. Switch to 'master' before releasing."
    }

    $dirty = Run-Git "status --porcelain"
    if (-not [string]::IsNullOrWhiteSpace($dirty)) {
        throw "Working tree is not clean. Commit or stash changes before release."
    }

    $tagName = "v$VersionName"
    $localTag = & git tag -l $tagName
    if ($localTag -eq $tagName) {
        throw "Tag $tagName already exists locally."
    }

    $remoteTag = & git ls-remote --tags origin $tagName
    if (-not [string]::IsNullOrWhiteSpace($remoteTag)) {
        throw "Tag $tagName already exists on origin."
    }

    $gradleFile = "app/build.gradle.kts"
    $gradleRaw = Get-Content $gradleFile -Raw

    $currentCodeMatch = [regex]::Match($gradleRaw, 'versionCode\s*=\s*(\d+)')
    $currentNameMatch = [regex]::Match($gradleRaw, 'versionName\s*=\s*"([^"]+)"')

    if (-not $currentCodeMatch.Success -or -not $currentNameMatch.Success) {
        throw "Could not parse versionCode/versionName in $gradleFile"
    }

    $currentVersionCode = [int]$currentCodeMatch.Groups[1].Value
    $targetVersionCode = if ($VersionCode -gt 0) { $VersionCode } else { $currentVersionCode + 1 }

    if ($targetVersionCode -le $currentVersionCode) {
        throw "New versionCode ($targetVersionCode) must be greater than current ($currentVersionCode)."
    }

    $updated = [regex]::Replace($gradleRaw, 'versionCode\s*=\s*\d+', "versionCode = $targetVersionCode", 1)
    $updated = [regex]::Replace($updated, 'versionName\s*=\s*"[^"]+"', "versionName = `"$VersionName`"", 1)
    Set-Content -Path $gradleFile -Value $updated -Encoding UTF8

    if (-not $SkipBuild) {
        & .\gradlew.bat :app:assembleRelease --no-daemon
        if ($LASTEXITCODE -ne 0) {
            throw "Release build failed. Aborting release."
        }
    }

    Run-Git "add $gradleFile"
    Run-Git "commit -m \"Release $tagName (code $targetVersionCode)\""
    Run-Git "push origin master"
    Run-Git "tag $tagName"
    Run-Git "push origin $tagName"

    Write-Host "Release completed successfully." -ForegroundColor Green
    Write-Host "Version name: $VersionName" -ForegroundColor Green
    Write-Host "Version code: $targetVersionCode" -ForegroundColor Green
    Write-Host "Tag pushed: $tagName" -ForegroundColor Green
}
finally {
    Pop-Location
}
