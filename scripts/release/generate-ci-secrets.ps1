param(
    [Parameter(Mandatory = $true)]
    [string]$KeystorePath,

    [Parameter(Mandatory = $false)]
    [string]$SupabaseUrl = "",

    [Parameter(Mandatory = $false)]
    [string]$SupabaseServiceRoleKey = ""
)

if (-not (Test-Path $KeystorePath)) {
    throw "Keystore file not found: $KeystorePath"
}

$keystoreBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $KeystorePath))
$keystoreBase64 = [System.Convert]::ToBase64String($keystoreBytes)

Write-Output ""
Write-Output "Copy these into GitHub Actions Secrets:"
Write-Output ""
Write-Output "ANDROID_KEYSTORE_BASE64:"
Write-Output $keystoreBase64
Write-Output ""
Write-Output "Also add manually:"
Write-Output "ANDROID_STORE_PASSWORD"
Write-Output "ANDROID_KEY_ALIAS"
Write-Output "ANDROID_KEY_PASSWORD"
Write-Output "SUPABASE_URL"
Write-Output "SUPABASE_SERVICE_ROLE_KEY"
