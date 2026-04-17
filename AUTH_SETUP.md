# Authentication System - Supabase Setup

## Current Status

Authentication is now backed by Supabase GoTrue.

- Email/password login and registration use Supabase Auth
- Profile metadata is stored in Supabase user metadata
- Google sign-in uses Supabase auth with Google ID tokens
- No Firebase auth configuration is required for the app runtime

## Required Local Settings

Set these in `local.properties`:

```properties
SUPABASE_URL=https://ocngqeuehrzkwjslrhch.supabase.co
SUPABASE_KEY=<anon-or-publishable-key>
GOOGLE_WEB_CLIENT_ID=<google-web-client-id>.apps.googleusercontent.com
```

For migration scripts only, also set:

```properties
SUPABASE_SERVICE_ROLE_KEY=<service-role-key>
```

## Google Sign-In

Google Sign-In is still used as the identity provider, but the token is handed to Supabase.

Make sure:
- Your Google OAuth client is configured for the Android package name
- `GOOGLE_WEB_CLIENT_ID` in `local.properties` matches your Google OAuth web client ID
- SHA-1 fingerprints are registered with the Google OAuth client if needed by your sign-in flow

## Build and Run

```powershell
.\gradlew clean assembleDebug
```

## Authentication Features

✅ Email/password registration
✅ Email verification
✅ Login with email/password
✅ Google sign-in via Supabase
✅ Session persistence
✅ Logout functionality
✅ Error handling with user-friendly messages

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
- No hardcoded credentials
- Secure token handling via Supabase
