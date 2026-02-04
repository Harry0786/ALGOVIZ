# Authentication System - Setup Instructions

## 1. Firebase Configuration

### Replace google-services.json
The file `app/google-services.json` is a placeholder. Replace it with your actual Firebase configuration:
1. Go to Firebase Console (https://console.firebase.google.com/)
2. Create a new project or select existing
3. Add Android app with package name: `com.algoviz.plus`
4. Download `google-services.json`
5. Replace `app/google-services.json` with downloaded file

### Enable Authentication Methods
In Firebase Console:
1. Navigate to Authentication > Sign-in method
2. Enable "Email/Password"
3. Enable "Google"
4. Add your app's SHA-1 fingerprint

## 2. Google Sign-In Configuration

### Get SHA-1 Fingerprint
Debug:
```powershell
keytool -list -v -keystore C:\Users\YOUR_USER\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Release:
```powershell
keytool -list -v -keystore path\to\your\release.keystore -alias your-key-alias
```

### Update Web Client ID
In `LoginScreen.kt`, replace the web client ID:
```kotlin
val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
```
Get this from Firebase Console > Authentication > Sign-in method > Google > Web SDK configuration

## 3. Build and Run

```powershell
.\gradlew clean assembleDebug
```

## 4. Testing Flow

1. Launch app
2. Register new account with email/password
3. Verify email (check inbox)
4. Login with verified account
5. Try Google Sign-In

## Authentication Features

✅ Email/Password registration
✅ Email verification enforcement
✅ Login with email/password
✅ Google Sign-In
✅ Session persistence
✅ Auto-login on app launch
✅ Email verification flow
✅ Logout functionality
✅ Error handling with user-friendly messages
✅ Loading states
✅ Input validation
✅ Production-ready ProGuard rules

## Architecture

- Clean Architecture with proper layer separation
- MVVM pattern in presentation layer
- Hilt for dependency injection
- Coroutines + Flow for async operations
- Compose for UI
- StateFlow for state management
- Firebase Auth for backend

## Security

- Email validation
- Password strength requirements (minimum 6 characters)
- Email verification enforcement
- ProGuard rules for release builds
- No hardcoded credentials
- Secure token handling
