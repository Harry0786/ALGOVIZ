package com.algoviz.plus.ui.profile

import android.content.Context
import android.net.Uri
import com.algoviz.plus.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

    @Serializable
    private data class UserProfileRow(
        @SerialName("user_id") val userId: String,
        val name: String,
        val username: String = "",
        val email: String,
        @SerialName("phone_no") val phoneNo: String = "",
        val bio: String,
        @SerialName("avatar_url") val avatarUrl: String? = null,
        @SerialName("study_goal") val studyGoal: String,
        @SerialName("skill_level") val skillLevel: String,
        @SerialName("avatar_color_index") val avatarColorIndex: Int,
        @SerialName("updated_at") val updatedAt: Long
    )

    private companion object {
        const val PROFILE_IMAGES_BUCKET = "Algoviz"
        const val PROFILE_IMAGES_PATH = "profile_images"
    }

    private fun buildPublicAvatarUrl(objectPath: String, version: Long? = null): String {
        val base = BuildConfig.SUPABASE_URL.trimEnd('/')
        val url = "$base/storage/v1/object/public/$PROFILE_IMAGES_BUCKET/$objectPath"
        return if (version != null) "$url?v=$version" else url
    }

    private fun normalizeAvatarUrl(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isBlank()) return null

        return when {
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed.replaceFirst("http://", "https://")
            trimmed.startsWith("/") -> "${BuildConfig.SUPABASE_URL.trimEnd('/')}$trimmed"
            else -> {
                if (trimmed.startsWith("storage/v1/object/public/")) {
                    "${BuildConfig.SUPABASE_URL.trimEnd('/')}/$trimmed"
                } else {
                    trimmed
                }
            }
        }
    }

    private fun JsonObject.valueOrNull(key: String): String? {
        return runCatching { this[key]?.jsonPrimitive?.content?.trim() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private fun resolveDisplayName(
        user: UserInfo,
        preferred: String? = null,
        fallback: String = "AlgoViz User"
    ): String {
        val metadata = user.userMetadata
        return listOf(
            preferred,
            metadata?.valueOrNull("name"),
            metadata?.valueOrNull("full_name"),
            metadata?.valueOrNull("user_name"),
            metadata?.valueOrNull("preferred_username"),
            metadata?.valueOrNull("given_name"),
            user.email?.substringBefore('@')
        ).firstOrNull { !it.isNullOrBlank() } ?: fallback
    }

    private fun extractStorageObjectPath(rawUrl: String?): String? {
        val normalized = normalizeAvatarUrl(rawUrl) ?: return null
        val marker = "/storage/v1/object/public/$PROFILE_IMAGES_BUCKET/"
        val index = normalized.indexOf(marker)
        if (index == -1) return null

        val afterMarker = normalized.substring(index + marker.length)
        return afterMarker.substringBefore('?').trim().ifBlank { null }
    }

    private suspend fun getExistingAvatarObjectPath(uid: String): String? {
        val rowAvatar = runCatching {
            userProfilesTable.select {
                filter {
                    eq("user_id", uid)
                }
                limit(1)
            }.decodeSingleOrNull<UserProfileRow>()?.avatarUrl
        }.getOrNull()

        val rowPath = extractStorageObjectPath(rowAvatar)
        if (rowPath != null) return rowPath

        val metadataAvatar = supabaseClient.auth.currentUserOrNull()
            ?.userMetadata
            ?.get("avatarUrl")
            ?.jsonPrimitive
            ?.content

        return extractStorageObjectPath(metadataAvatar)
    }

    private val userProfilesTable = supabaseClient.postgrest["user_profiles"]

    private suspend fun resolveAuthenticatedUser(): UserInfo? {
        // Try up to 5 times with small delays, in case auth is still loading from storage
        repeat(5) { attempt ->
            val current = supabaseClient.auth.currentUserOrNull()
            Timber.d("Auth Debug [Attempt $attempt] - currentUserOrNull: ${current?.id ?: "null"}")
            if (current != null) return current

            val sessionStatus = supabaseClient.auth.sessionStatus.value
            Timber.d("Auth Debug [Attempt $attempt] - sessionStatus: $sessionStatus")
            
            val statusUser = when (sessionStatus) {
                is SessionStatus.Authenticated -> {
                    Timber.d("Auth Debug [Attempt $attempt] - sessionStatus is Authenticated, user: ${sessionStatus.session.user?.id ?: "null"}")
                    sessionStatus.session.user
                }
                else -> {
                    Timber.d("Auth Debug [Attempt $attempt] - sessionStatus is NOT Authenticated: ${sessionStatus::class.simpleName}")
                    null
                }
            }
            if (statusUser != null) return statusUser

            val session = supabaseClient.auth.currentSessionOrNull()
            Timber.d("Auth Debug [Attempt $attempt] - currentSessionOrNull: ${session?.user?.id ?: "null"}")
            if (session != null) {
                if (session.user != null) return session.user
                
                Timber.d("Auth Debug [Attempt $attempt] - Session exists but user is null, trying retrieveUserForCurrentSession...")
                val retrieved = runCatching {
                    supabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
                }.onSuccess {
                    Timber.d("Auth Debug [Attempt $attempt] - retrieveUserForCurrentSession succeeded: ${it.id}")
                }.onFailure {
                    Timber.e(it, "Auth Debug [Attempt $attempt] - retrieveUserForCurrentSession failed")
                }.getOrNull()
                
                if (retrieved != null) return retrieved
            } else {
                Timber.d("Auth Debug [Attempt $attempt] - No session found, waiting 100ms and retrying...")
                delay(100) // Give auth time to load from storage
            }
        }
        
        Timber.w("Auth Debug - All 5 resolution attempts failed, no authenticated user found")
        return null
    }

    suspend fun getCurrentUserId(): String? = resolveAuthenticatedUser()?.id

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        return try {
            Timber.d("Avatar Upload - Starting upload process")
            val uid = getCurrentUserId().also { 
                Timber.d("Avatar Upload - Resolved user ID: ${it ?: "null"}")
            }
            if (uid == null) {
                val errorMsg = "No authenticated user. Please sign in again."
                Timber.e("Avatar Upload - FAILED: getCurrentUserId returned null. Auth state: currentUserOrNull=${supabaseClient.auth.currentUserOrNull()}, sessionStatus=${supabaseClient.auth.sessionStatus.value}")
                return Result.failure(Exception(errorMsg))
            }
            
            val objectPath = "$PROFILE_IMAGES_PATH/$uid.jpg"
            Timber.d("Avatar Upload - Upload path: $objectPath")
            val previousObjectPath = getExistingAvatarObjectPath(uid)
            
            val bytes = appContext.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return Result.failure(Exception("Unable to read selected image"))
            
            Timber.d("Avatar Upload - Image bytes read: ${bytes.size}")

            supabaseClient.storage
                .from(PROFILE_IMAGES_BUCKET)
                .upload(objectPath, bytes, upsert = true)

            Timber.d("Avatar Upload - Upload to storage completed successfully")

            if (!previousObjectPath.isNullOrBlank() && previousObjectPath != objectPath) {
                runCatching {
                    supabaseClient.storage
                        .from(PROFILE_IMAGES_BUCKET)
                        .delete(listOf(previousObjectPath))
                }.onSuccess {
                    Timber.d("Avatar Upload - Deleted old avatar object: $previousObjectPath")
                }.onFailure {
                    Timber.w(it, "Avatar Upload - Unable to delete old avatar object: $previousObjectPath")
                }
            }

            val publicUrl = buildPublicAvatarUrl(objectPath, version = System.currentTimeMillis())

            Timber.d("Avatar Upload - Public URL generated: $publicUrl")
            Result.success(publicUrl)
        } catch (e: Exception) {
            Timber.e(e, "Avatar Upload - Failed to upload profile image. Exception type: ${e::class.simpleName}")
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            if (resolveAuthenticatedUser() == null) {
                return Result.failure(Exception("No authenticated user. Please sign in again."))
            }

            val uid = getCurrentUserId() ?: return Result.failure(Exception("No authenticated user. Please sign in again."))
            val updatedAt = System.currentTimeMillis()

            val normalizedAvatarUrl = normalizeAvatarUrl(profile.avatarUrl)

            // Metadata sync is best-effort; profile table persistence remains the source of truth.
            runCatching {
                supabaseClient.auth.modifyUser {
                    data {
                        put("name", profile.name)
                        put("username", profile.username)
                        put("profileEmail", profile.email)
                        put("phoneNumber", profile.phoneNumber)
                        put("bio", profile.bio)
                        put("avatarUrl", normalizedAvatarUrl ?: "")
                        put("studyGoal", profile.studyGoal)
                        put("skillLevel", profile.skillLevel)
                        put("avatarColorIndex", profile.avatarColorIndex)
                        put("updatedAt", updatedAt)
                    }
                }
            }.onFailure {
                Timber.w(it, "Profile metadata sync failed; continuing with user_profiles upsert")
            }

            val payload = buildJsonObject {
                put("user_id", uid)
                put("name", profile.name)
                put("username", profile.username)
                put("email", profile.email)
                put("phone_no", profile.phoneNumber)
                put("bio", profile.bio)
                put("avatar_url", normalizedAvatarUrl ?: "")
                put("study_goal", profile.studyGoal)
                put("skill_level", profile.skillLevel)
                put("avatar_color_index", profile.avatarColorIndex)
                put("updated_at", updatedAt)
            }

            val existingRow = userProfilesTable.select {
                filter {
                    eq("user_id", uid)
                }
                limit(1)
            }.decodeSingleOrNull<UserProfileRow>()

            if (existingRow == null) {
                userProfilesTable.insert(payload)
            } else {
                userProfilesTable.update(payload) {
                    filter {
                        eq("user_id", uid)
                    }
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
            val user = resolveAuthenticatedUser() ?: return Result.success(null)
            val uid = user.id

            val rowProfile = userProfilesTable.select {
                filter {
                    eq("user_id", uid)
                }
                limit(1)
            }.decodeSingleOrNull<UserProfileRow>()

            if (rowProfile != null) {
                return Result.success(
                    UserProfile(
                        name = resolveDisplayName(user, preferred = rowProfile.name),
                        username = rowProfile.username.ifBlank {
                            user.userMetadata?.valueOrNull("username")
                                ?: user.userMetadata?.valueOrNull("user_name")
                                ?: user.userMetadata?.valueOrNull("preferred_username")
                                ?: user.email?.substringBefore('@').orEmpty()
                        },
                        email = rowProfile.email.ifBlank { user.email ?: "user@algoviz.com" },
                        phoneNumber = rowProfile.phoneNo,
                        bio = rowProfile.bio.ifBlank { "Learning algorithms and data structures" },
                        avatarUrl = normalizeAvatarUrl(rowProfile.avatarUrl),
                        studyGoal = rowProfile.studyGoal.ifBlank { "Master algorithms" },
                        skillLevel = rowProfile.skillLevel.ifBlank { "Beginner" },
                        avatarColorIndex = rowProfile.avatarColorIndex
                    )
                )
            }

            val metadata = user.userMetadata
            if (metadata == null || metadata.isEmpty()) {
                return Result.success(null)
            }

            fun JsonObject.string(key: String, fallback: String): String {
                return this[key]?.jsonPrimitive?.content?.ifBlank { fallback } ?: fallback
            }

            val profile = UserProfile(
                name = resolveDisplayName(user),
                username = metadata.string(
                    "username",
                    metadata.string(
                        "user_name",
                        metadata.string(
                            "preferred_username",
                            user.email?.substringBefore('@') ?: ""
                        )
                    )
                ),
                email = metadata.string(
                    "profileEmail",
                    metadata.string("email", user.email ?: "user@algoviz.com")
                ),
                phoneNumber = metadata.string("phoneNumber", metadata.string("phone", "")),
                bio = metadata.string("bio", "Learning algorithms and data structures"),
                avatarUrl = normalizeAvatarUrl(metadata["avatarUrl"]?.jsonPrimitive?.content),
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