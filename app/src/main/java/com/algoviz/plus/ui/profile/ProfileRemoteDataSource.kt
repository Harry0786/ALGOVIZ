package com.algoviz.plus.ui.profile

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRemoteDataSource @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val supabaseClient: SupabaseClient
) {

    private companion object {
        const val PROFILE_IMAGES_BUCKET = "profile-images"
        const val PROFILE_IMAGES_PATH = "profile_images"
    }

    fun getCurrentUserId(): String? = supabaseClient.auth.currentUserOrNull()?.id

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        return try {
            val uid = getCurrentUserId() ?: return Result.failure(Exception("No authenticated user"))
            val objectPath = "$PROFILE_IMAGES_PATH/$uid.jpg"
            val bytes = appContext.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return Result.failure(Exception("Unable to read selected image"))

            supabaseClient.storage
                .from(PROFILE_IMAGES_BUCKET)
                .upload(objectPath, bytes, upsert = true)

            val publicUrl = supabaseClient.storage
                .from(PROFILE_IMAGES_BUCKET)
                .publicRenderUrl(objectPath)

            Result.success(publicUrl)
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload profile image")
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            if (supabaseClient.auth.currentSessionOrNull() == null) {
                return Result.failure(Exception("No authenticated user"))
            }

            supabaseClient.auth.modifyUser {
                data {
                    put("name", profile.name)
                    put("email", profile.email)
                    put("bio", profile.bio)
                    put("avatarUrl", profile.avatarUrl ?: "")
                    put("studyGoal", profile.studyGoal)
                    put("skillLevel", profile.skillLevel)
                    put("avatarColorIndex", profile.avatarColorIndex)
                    put("updatedAt", System.currentTimeMillis())
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save user profile")
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val user = supabaseClient.auth.currentUserOrNull() ?: return Result.success(null)
            val metadata = user.userMetadata
            if (metadata == null || metadata.isEmpty()) {
                return Result.success(null)
            }

            fun JsonObject.string(key: String, fallback: String): String {
                return this[key]?.jsonPrimitive?.content?.ifBlank { fallback } ?: fallback
            }

            val profile = UserProfile(
                name = metadata.string("name", "AlgoViz User"),
                email = metadata.string("email", user.email ?: "user@algoviz.com"),
                bio = metadata.string("bio", "Learning algorithms and data structures"),
                avatarUrl = metadata["avatarUrl"]?.jsonPrimitive?.content,
                studyGoal = metadata.string("studyGoal", "Master algorithms"),
                skillLevel = metadata.string("skillLevel", "Beginner"),
                avatarColorIndex = metadata["avatarColorIndex"]?.jsonPrimitive?.intOrNull ?: 0
            )

            Result.success(profile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch user profile")
            Result.failure(e)
        }
    }
}