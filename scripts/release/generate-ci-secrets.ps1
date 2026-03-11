param(
    [Parameter(Mandatory = $true)]
    [string]$KeystorePath,

    [Parameter(Mandatory = $true)]
    [string]$ServiceAccountJsonPath,

    [Parameter(Mandatory = $false)]
    [string]$GoogleServicesJsonPath = ".\app\google-services.json"
)

if (-not (Test-Path $KeystorePath)) {
    throw "Keystore file not found: $KeystorePath"
}

if (-not (Test-Path $ServiceAccountJsonPath)) {
    throw "Service account json file not found: $ServiceAccountJsonPath"
}

if (-not (Test-Path $GoogleServicesJsonPath)) {
    throw "google-services.json file not found: $GoogleServicesJsonPath"
}

$keystoreBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $KeystorePath))
$keystoreBase64 = [System.Convert]::ToBase64String($keystoreBytes)

$googleServicesBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $GoogleServicesJsonPath))
$googleServicesBase64 = [System.Convert]::ToBase64String($googleServicesBytes)

$jsonRaw = Get-Content $ServiceAccountJsonPath -Raw
$jsonOneLine = ($jsonRaw | ConvertFrom-Json | ConvertTo-Json -Compress)

Write-Output ""
Write-Output "Copy these into GitHub Actions Secrets:"
Write-Output ""
Write-Output "ANDROID_KEYSTORE_BASE64:"
Write-Output $keystoreBase64
Write-Output ""
Write-Output "FIREBASE_SERVICE_ACCOUNT_JSON:"
Write-Output $jsonOneLine
Write-Output ""
Write-Output "GOOGLE_SERVICES_JSON_BASE64:"
Write-Output $googleServicesBase64
Write-Output ""
Write-Output "Also add manually:"
Write-Output "ANDROID_STORE_PASSWORD"
Write-Output "ANDROID_KEY_ALIAS"
Write-Output "ANDROID_KEY_PASSWORD"
Write-Output "FIREBASE_PROJECT_ID"
Write-Output "FIREBASE_STORAGE_BUCKET"
