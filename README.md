# AlgoViz+

AlgoViz+ is an Android app for learning and practicing algorithms with real-time, collaborative study-room features.

Current implementation includes:
- Supabase-backed authentication (email/password + Google)
- Study rooms, room member presence, and chat flows
- Modular Clean Architecture foundation for continued feature expansion

## Tech Stack

- Kotlin 1.9.22
- Android Gradle Plugin 8.3.0
- Jetpack Compose + Material 3
- MVVM + Clean Architecture
- Hilt dependency injection
- Coroutines + Flow
- Retrofit + OkHttp
- Room + DataStore
- Supabase Kotlin SDK (GoTrue, PostgREST, Storage, Realtime)
- Firebase Messaging + Analytics

## Project Structure

Main directories:

- app: Android application module and navigation wiring
- core/common: shared utilities, models, and result/error primitives
- core/ui: base UI components and shared presentation helpers
- core/designsystem: theme, typography, and design tokens
- core/network: HTTP client and API/network abstractions
- core/database: Room database layer
- core/datastore: preference storage
- data: repository and datasource implementations
- domain: use cases and repository contracts
- features: feature modules
- scripts: Supabase SQL utilities and helper scripts

## Requirements

- JDK 17
- Android Studio Hedgehog 2023.1.1 or newer
- Android SDK 34
- Gradle wrapper (included)

Android build config (from app module):
- minSdk: 26
- targetSdk: 34
- compileSdk: 34

## Local Setup

1. Copy local.properties.template to local.properties
2. Set your SDK path in local.properties
3. Add required runtime keys in local.properties
4. Sync Gradle and build

Required local.properties entries:

- sdk.dir=... (local Android SDK path)
- SUPABASE_URL=...
- SUPABASE_KEY=... (anon/publishable key)
- GOOGLE_WEB_CLIENT_ID=...

Optional entries:

- SUPABASE_URL_DEBUG, SUPABASE_URL_STAGING, SUPABASE_URL_RELEASE
- RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD
- SUPABASE_SERVICE_ROLE_KEY (scripts/admin-only workflows)

Security notes:

- Never commit local.properties
- Never commit signing keystores or service role secrets
- Use local.properties.template as the only committed template

## Build Variants

- debug: debuggable, applicationId suffix .debug
- staging: debuggable + minified/shrunk, suffix .staging
- release: minified/shrunk, optional signing from local properties

## Common Commands

From repository root on Windows:

- .\gradlew.bat clean
- .\gradlew.bat assembleDebug
- .\gradlew.bat assembleStaging
- .\gradlew.bat assembleRelease
- .\gradlew.bat test
- .\gradlew.bat detekt
- .\gradlew.bat ktlintCheck

## Supabase Workflow

Core SQL assets are under scripts:

- scripts/supabase_studyroom_schema.sql
- scripts/supabase_full_recovery.sql
- scripts/supabase_complete_audit_fix.sql
- scripts/study_rooms_diagnostic.sql
- scripts/fix_study_rooms_member_data.sql

Recommended order for environment setup and recovery:

1. Apply schema/base setup
2. Apply recovery/audit fixes if needed
3. Run diagnostics to validate consistency

Related documentation:

- AUTH_SETUP.md
- SUPABASE_DEPLOYMENT_GUIDE.md
- STUDYROOMS_TESTING_GUIDE.md
- docs/archive (historical completion reports)

## Architecture Notes

The codebase follows Clean Architecture boundaries:

- Domain: pure business use cases and contracts
- Data: concrete repositories and remote/local datasource integration
- Presentation: Compose screens and ViewModels

This structure keeps feature logic testable and infrastructure-swappable.

## Repository Hygiene

The repository is configured to ignore generated and local-only artifacts including:

- local.properties
- keystores (*.jks)
- IDE/cache/build output folders
- JVM error/replay logs

If you see these files locally, that is expected; they should remain untracked.

## Status

The project is actively maintained with Supabase-based runtime flows and cleaned repository structure. The README, setup template, and deployment/testing guides are aligned for onboarding and day-to-day development.
