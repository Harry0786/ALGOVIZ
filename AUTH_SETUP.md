# Authentication System - Supabase Setup

## Current Status

Authentication is backed by Supabase Auth (project `zosawqjebxkjppwtkegx`).

- Email/password login and registration use Supabase Auth
- Profile metadata is stored in Supabase user metadata
- Google sign-in uses Supabase Auth with Google ID tokens
- No additional legacy backend configuration is required for auth runtime

## Required Local Settings

Set these in `local.properties`:

```properties
SUPABASE_URL=https://zosawqjebxkjppwtkegx.supabase.co
SUPABASE_KEY=<publishable-or-anon-key-from-supabase-dashboard>
GOOGLE_WEB_CLIENT_ID=755994556793-ra0a3m34q7etiinrlsum293hiq267ngd.apps.googleusercontent.com
```

For migration scripts only, also set:

```properties
SUPABASE_SERVICE_ROLE_KEY=<service-role-key>
```

## Google Sign-In

Google Sign-In uses the **Web client ID** in the Android app (`requestIdToken`), then sends the ID token to Supabase.

### Google Cloud Console (project: Algoviz)

| Client | Value |
|--------|-------|
| **Web client** (use in app + Supabase) | `755994556793-ra0a3m34q7etiinrlsum293hiq267ngd.apps.googleusercontent.com` |
| **Android client** (release) | `755994556793-m5jnt8tlnhlj4plq53841llscga94ed.apps.googleusercontent.com` |
| **Release package** | `com.algoviz.plus` |
| **Release SHA-1** | `EF:BF:AC:BB:C8:32:9E:5E:98:6D:4A:93:D4:85:86:67:12:9C:21:25` |

### Supabase Dashboard → Authentication → Google

- **Enable Sign in with Google:** ON
- **Client IDs:** include both the Web and Android client IDs (comma-separated):
  ```
  755994556793-ra0a3m34q7etiinrlsum293hiq267ngd.apps.googleusercontent.com,755994556793-m5jnt8tlnhlj4plq53841llscga94ed.apps.googleusercontent.com
  ```
- **Client Secret:** from Google Cloud Console (Web client secret)
- **Callback URL:** `https://zosawqjebxkjppwtkegx.supabase.co/auth/v1/callback`

### Debug builds (`com.algoviz.plus.debug`)

Debug APKs use a different package name. Create a **second Android OAuth client** in Google Cloud Console:

1. Package name: `com.algoviz.plus.debug`
2. SHA-1: from your debug keystore:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
3. Add the new Android client ID to Supabase **Client IDs** (comma-separated list)

Without a debug Android OAuth client, Google Sign-In will fail on debug builds even if release is configured correctly.

## Build and Run

```powershell
.\gradlew clean assembleDebug
```

## Authentication Features

- Email/password registration
- Email verification
- Login with email/password
- Google sign-in via Supabase
- Session persistence
- Logout functionality
- Error handling with user-friendly messages

## Architecture

- Clean Architecture with proper layer separation
- MVVM pattern in presentation layer
- Hilt for dependency injection
- Coroutines + Flow for async operations
- Compose for UI
- StateFlow for state management

## Security

- Email validation
- Password strength requirements
- Email verification enforcement
- No hardcoded credentials in source (keys live in `local.properties` only)
- Secure token handling via Supabase
