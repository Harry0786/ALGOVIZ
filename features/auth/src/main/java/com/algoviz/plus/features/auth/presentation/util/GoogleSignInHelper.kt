package com.algoviz.plus.features.auth.presentation.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Tasks
import timber.log.Timber
import java.util.concurrent.TimeUnit

object GoogleSignInHelper {

    fun getGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context.applicationContext, gso)
    }

    /**
     * Clears any cached Google account so the picker always returns a fresh ID token.
     */
    fun launchSignIn(
        googleSignInClient: GoogleSignInClient,
        launchIntent: (Intent) -> Unit
    ) {
        googleSignInClient.signOut().addOnCompleteListener {
            launchIntent(googleSignInClient.signInIntent)
        }
    }

    fun handleSignInResult(result: ActivityResult, packageName: String = ""): GoogleSignInResult {
        val data = result.data
        if (data == null) {
            return if (result.resultCode == Activity.RESULT_CANCELED) {
                GoogleSignInResult.Cancelled
            } else {
                GoogleSignInResult.Error(
                    "Google Sign-In failed before an account was selected. Please try again."
                )
            }
        }

        // Google Play Services may return RESULT_CANCELED even when the intent contains
        // a usable account or a concrete ApiException — always parse the intent first.
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                GoogleSignInResult.Error(
                    "Google Sign-In did not return an ID token. Verify GOOGLE_WEB_CLIENT_ID and OAuth setup."
                )
            } else {
                GoogleSignInResult.Success(idToken)
            }
        } catch (e: ApiException) {
            Timber.e(e, "Google sign-in failed with status ${e.statusCode}")
            GoogleSignInResult.Error(mapApiException(e, packageName))
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in failed")
            GoogleSignInResult.Error(e.message ?: "Google Sign-In failed")
        }
    }

    fun awaitSignOut(googleSignInClient: GoogleSignInClient) {
        runCatching {
            Tasks.await(googleSignInClient.signOut(), 5, TimeUnit.SECONDS)
        }.onFailure { error ->
            Timber.w(error, "Google sign-out before sign-in did not complete; continuing anyway")
        }
    }

    private fun mapApiException(exception: ApiException, packageName: String): String {
        val debugHint = debugPackageHint(packageName)
        return when (exception.statusCode) {
            CommonStatusCodes.DEVELOPER_ERROR ->
                "Google Sign-In is misconfigured for this build. Check GOOGLE_WEB_CLIENT_ID and register an Android OAuth client for package $packageName with the correct SHA-1 in Google Cloud Console.$debugHint"
            CommonStatusCodes.NETWORK_ERROR ->
                "Network error during Google Sign-In."
            CommonStatusCodes.CANCELED ->
                "Google Sign-In cancelled.${if (packageName.endsWith(".debug")) debugPackageHint(packageName) else ""}"
            CommonStatusCodes.INVALID_ACCOUNT ->
                "The selected Google account is not valid for this app."
            CommonStatusCodes.SIGN_IN_REQUIRED ->
                "Google Sign-In required. Please try again."
            else ->
                "Google Sign-In failed (${exception.statusCode}).$debugHint"
        }
    }

    private fun debugPackageHint(packageName: String): String {
        if (!packageName.endsWith(".debug")) return ""
        return " Debug builds use $packageName — create a separate Android OAuth client for that package plus your debug keystore SHA-1."
    }
}

sealed class GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
}
