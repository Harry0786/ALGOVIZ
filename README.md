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
