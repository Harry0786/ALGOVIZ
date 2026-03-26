package com.algoviz.plus.ui.profile

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private companion object {
        const val PROFILES_COLLECTION = "user_profiles"
        const val PROFILE_IMAGES_PATH = "profile_images"
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        return try {
            val uid = getCurrentUserId() ?: return Result.failure(Exception("No authenticated user"))
            val imageRef = storage.reference
                .child(PROFILE_IMAGES_PATH)
                .child("$uid.jpg")

            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload profile image")
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val uid = getCurrentUserId() ?: return Result.failure(Exception("No authenticated user"))
            val payload = hashMapOf(
                "name" to profile.name,
                "email" to profile.email,
                "bio" to profile.bio,
                "avatarUrl" to (profile.avatarUrl ?: ""),
                "studyGoal" to profile.studyGoal,
                "skillLevel" to profile.skillLevel,
                "avatarColorIndex" to profile.avatarColorIndex,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(PROFILES_COLLECTION)
                .document(uid)
                .set(payload, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save user profile")
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val uid = getCurrentUserId() ?: return Result.success(null)
            val snapshot = firestore.collection(PROFILES_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.success(null)
            }

            val profile = UserProfile(
                name = snapshot.getString("name") ?: "AlgoViz User",
                email = snapshot.getString("email") ?: "user@algoviz.com",
                bio = snapshot.getString("bio") ?: "Learning algorithms and data structures",
                avatarUrl = snapshot.getString("avatarUrl"),
                studyGoal = snapshot.getString("studyGoal") ?: "Master algorithms",
                skillLevel = snapshot.getString("skillLevel") ?: "Beginner",
                avatarColorIndex = (snapshot.getLong("avatarColorIndex") ?: 0L).toInt()
            )

            Result.success(profile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch user profile")
            Result.failure(e)
        }
    }
}