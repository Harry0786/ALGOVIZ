# AlgoViz+ - Android Application

Production-grade Android application for algorithm learning platform.

## Tech Stack

- Kotlin 1.9.22
- Jetpack Compose with Material 3
- MVVM + Clean Architecture
- Hilt for DI
- Coroutines + Flow
- Retrofit + OkHttp
- Room + DataStore
- Supabase integration ready
- Firebase Cloud Messaging

## Module Structure

```
app/                    - Application module
core/
  ├── common/          - Shared utilities, constants, result types
  ├── ui/              - Base UI components, ViewModel
  ├── designsystem/    - Material 3 theme, colors, typography
  ├── network/         - Retrofit, OkHttp, interceptors
  ├── database/        - Room database
  └── datastore/       - DataStore preferences
data/                  - Repository implementations
domain/                - Use cases, repository interfaces
features/              - Feature modules (to be implemented)
```

## Requirements

- JDK 17
- Android Studio Hedgehog | 2023.1.1+
- Android SDK 34
- Gradle 8.6

## Setup

1. Copy `local.properties.template` to `local.properties`
2. Add SDK path and API keys
3. Sync Gradle
4. Build project

## Build Variants

- **debug** - Development with verbose logging
- **staging** - Pre-production testing
- **release** - Production build

## Gradle Tasks

```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
./gradlew test               # Run unit tests
./gradlew detekt             # Run code analysis
./gradlew ktlintCheck        # Check code style
```

## Architecture

Clean Architecture with MVVM pattern:
- **Domain Layer**: Business logic, use cases
- **Data Layer**: Repository implementations, data sources
- **Presentation Layer**: ViewModels, UI components

## Next Steps

Feature modules ready for implementation:
- Authentication
- Home/Dashboard
- Leaderboards
- Study Rooms
- Analytics
- Profile

---

**Status**: Infrastructure Complete | Week 1 + Week 2 Foundation Ready

## Automatic In-App Update Pipeline (No Play Store)

This repository now includes a CI workflow at `.github/workflows/release-update.yml`.

What it does:
- Builds signed release APK
- Uploads APK to Firebase Storage
- Updates Firestore document `app_config/latest_version`
- Existing app installations detect the new version and prompt users to update

### Required GitHub Secrets

Add these in repository settings: `Settings > Secrets and variables > Actions`.

- `ANDROID_KEYSTORE_BASE64`: Base64-encoded release keystore file
- `ANDROID_STORE_PASSWORD`: Keystore password
- `ANDROID_KEY_ALIAS`: Signing key alias
- `ANDROID_KEY_PASSWORD`: Signing key password
- `FIREBASE_SERVICE_ACCOUNT_JSON`: Full Firebase service account JSON (single-line string)
- `FIREBASE_PROJECT_ID`: Firebase project id (example: `algoviz-plus`)
- `FIREBASE_STORAGE_BUCKET`: Firebase storage bucket (example: `algoviz-plus.firebasestorage.app`)

### Triggering Releases

- Tag push: `git tag v1.2.0 && git push origin v1.2.0`
- Manual run: GitHub `Actions` tab -> `Build and Publish Android Update` -> `Run workflow`

### Important

- Increase `versionCode` in `app/build.gradle.kts` for every release.
- Keep using the same signing key, otherwise update install will fail.

### Fast Setup Helper (Local)

Generate the two long secret values using:

```powershell
pwsh ./scripts/release/generate-ci-secrets.ps1 \
  -KeystorePath "C:\path\to\release.jks" \
  -ServiceAccountJsonPath "C:\path\to\firebase-service-account.json"
```

### Final Activation Checklist

1. Add all required GitHub Secrets.
2. Ensure `versionCode` is incremented in `app/build.gradle.kts`.
3. Push a tag (example: `v1.1.0`) to trigger release automation.
4. Confirm workflow success in GitHub Actions.
5. Open installed app on test device and verify update prompt appears.

### One-Command Release (Master Branch)

Use this script to automate version bump, release build, commit, push, tag, and tag push:

```powershell
pwsh ./scripts/release/release.ps1 -VersionName 1.2.0
```

Optional explicit version code:

```powershell
pwsh ./scripts/release/release.ps1 -VersionName 1.2.0 -VersionCode 3
```

Notes:
- Must be run on a clean `master` branch.
- By default it runs `:app:assembleRelease` before tagging.
