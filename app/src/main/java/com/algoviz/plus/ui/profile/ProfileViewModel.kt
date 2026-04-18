package com.algoviz.plus.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.core.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val profileRemoteDataSource: ProfileRemoteDataSource
) : ViewModel() {

    private companion object {
        const val DEFAULT_NAME = "AlgoViz User"
        const val DEFAULT_USERNAME = ""
        const val DEFAULT_EMAIL = "user@algoviz.com"
        const val DEFAULT_PHONE_NUMBER = ""
        const val DEFAULT_BIO = "Learning algorithms and data structures"
        const val DEFAULT_STUDY_GOAL = "Master algorithms"
        const val DEFAULT_SKILL_LEVEL = "Beginner"
    }
    
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()
    
    init {
        // Load onboarding status
        viewModelScope.launch {
            preferencesManager.profileOnboardingCompleted.collect { completed ->
                _isOnboardingCompleted.value = completed
            }
        }
        
        // Load profile data from DataStore
        viewModelScope.launch {
            combine(
                listOf(
                    preferencesManager.profileName,
                    preferencesManager.profileUsername,
                    preferencesManager.profileEmail,
                    preferencesManager.profilePhoneNumber,
                    preferencesManager.profileBio,
                    preferencesManager.profileAvatarUrl,
                    preferencesManager.profileStudyGoal,
                    preferencesManager.profileSkillLevel,
                    preferencesManager.profileAvatarColorIndex
                )
            ) { values ->
                UserProfile(
                    name = values[0] as String,
                    username = values[1] as String,
                    email = values[2] as String,
                    phoneNumber = values[3] as String,
                    bio = values[4] as String,
                    avatarUrl = values[5] as String,
                    studyGoal = values[6] as String,
                    skillLevel = values[7] as String,
                    avatarColorIndex = values[8] as Int
                )
            }.collect { profile ->
                _userProfile.value = profile
                Timber.d("ProfileViewModel - DataStore loaded: avatarUrl=${profile.avatarUrl}")
            }
        }

        viewModelScope.launch {
            preferencesManager.userId
                .distinctUntilChanged()
                .collect { activeUserId ->
                    if (activeUserId.isNullOrBlank()) return@collect

                    val remoteProfile = profileRemoteDataSource.getUserProfile().getOrNull()
                    Timber.d("ProfileViewModel - User switch/profile refresh: userId=$activeUserId, avatarUrl=${remoteProfile?.avatarUrl ?: "null"}")

                    if (remoteProfile != null) {
                        persistLocalProfile(remoteProfile)
                        _userProfile.value = remoteProfile
                        Timber.d("ProfileViewModel - Remote hydration done for userId=$activeUserId")
                    }
                }
        }
    }
    
    fun updateProfile(
        name: String? = null,
        bio: String? = null,
        studyGoal: String? = null,
        skillLevel: String? = null
    ) {
        viewModelScope.launch {
            name?.let { preferencesManager.saveProfileName(it) }
            bio?.let { preferencesManager.saveProfileBio(it) }
            studyGoal?.let { preferencesManager.saveProfileStudyGoal(it) }
            skillLevel?.let { preferencesManager.saveProfileSkillLevel(it) }
        }
    }
    
    fun updateEmail(email: String) {
        viewModelScope.launch {
            preferencesManager.saveProfileEmail(email)
        }
    }
    
    fun updateAvatarUrl(avatarUrl: String) {
        viewModelScope.launch {
            preferencesManager.saveProfileAvatarUrl(avatarUrl)
        }
    }
    
    fun updateAvatarColorIndex(colorIndex: Int) {
        viewModelScope.launch {
            preferencesManager.saveProfileAvatarColorIndex(colorIndex)
        }
    }

    fun uploadAvatarFromGallery(imageUri: Uri) {
        viewModelScope.launch {
            uploadAndSyncAvatar(imageUri)
        }
    }

    fun uploadAvatarFromCamera(imageUri: Uri) {
        viewModelScope.launch {
            uploadAndSyncAvatar(imageUri)
        }
    }

    fun saveProfileChanges(
        name: String,
        username: String,
        email: String,
        phoneNumber: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val trimmedName = name.trim().ifEmpty { "AlgoViz User" }
            val trimmedUsername = username.trim()
            val trimmedEmail = email.trim().ifEmpty { "user@algoviz.com" }
            val trimmedPhoneNumber = phoneNumber.trim()

            if (trimmedName == DEFAULT_NAME) {
                _errorMessage.value = "Please enter your name"
                _isSaving.value = false
                return@launch
            }

            if (trimmedUsername.isBlank()) {
                _errorMessage.value = "Please enter a username"
                _isSaving.value = false
                return@launch
            }

            val updatedProfile = _userProfile.value.copy(
                name = trimmedName,
                username = trimmedUsername,
                email = trimmedEmail,
                phoneNumber = trimmedPhoneNumber
            )

            // Persist locally first so profile edits always save even if remote sync fails.
            persistLocalProfile(updatedProfile)
            _userProfile.value = updatedProfile

            profileRemoteDataSource.saveUserProfile(updatedProfile)
                .onSuccess {
                    // Mark onboarding as completed on successful save
                    preferencesManager.saveProfileOnboardingCompleted(true)
                    onSaved()
                }
                .onFailure { error ->
                    _errorMessage.value = sanitizeProfileError(
                        error.message,
                        fallback = "Saved locally, but unable to sync profile"
                    )
                }

            _isSaving.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
    
    fun resetProfile() {
        viewModelScope.launch {
            preferencesManager.saveProfileName("AlgoViz User")
            preferencesManager.saveProfileUsername("")
            preferencesManager.saveProfileEmail("user@algoviz.com")
            preferencesManager.saveProfilePhoneNumber("")
            preferencesManager.saveProfileBio("Learning algorithms and data structures")
            preferencesManager.saveProfileAvatarUrl("")
            preferencesManager.saveProfileStudyGoal("Master algorithms")
            preferencesManager.saveProfileSkillLevel("Beginner")
        }
    }

    private suspend fun persistLocalProfile(profile: UserProfile) {
        preferencesManager.saveProfileName(profile.name)
        preferencesManager.saveProfileUsername(profile.username)
        preferencesManager.saveProfileEmail(profile.email)
        preferencesManager.saveProfilePhoneNumber(profile.phoneNumber)
        preferencesManager.saveProfileBio(profile.bio)
        preferencesManager.saveProfileAvatarUrl(profile.avatarUrl ?: "")
        preferencesManager.saveProfileStudyGoal(profile.studyGoal)
        preferencesManager.saveProfileSkillLevel(profile.skillLevel)
        preferencesManager.saveProfileAvatarColorIndex(profile.avatarColorIndex)
    }

    private suspend fun uploadAndSyncAvatar(imageUri: Uri) {
        _isSaving.value = true
        _errorMessage.value = null

        Timber.d("Avatar Upload - uploadAndSyncAvatar called with URI: $imageUri")
        val uploadResult = profileRemoteDataSource.uploadProfileImage(imageUri)
        val uploadedUrl = uploadResult.getOrElse {
            _isSaving.value = false
            _errorMessage.value = sanitizeProfileError(
                it.message,
                fallback = "Unable to upload profile image"
            )
            Timber.e("Avatar Upload - Failed: ${it.message}")
            return
        }

        Timber.d("Avatar Upload - Success! URL: $uploadedUrl")

        val updatedProfile = _userProfile.value.copy(avatarUrl = uploadedUrl)
        persistLocalProfile(updatedProfile)
        _userProfile.value = updatedProfile
        Timber.d("Avatar Upload - Profile updated locally with URL: $uploadedUrl")

        profileRemoteDataSource.saveUserProfile(updatedProfile)
            .onFailure { error ->
                _errorMessage.value = sanitizeProfileError(
                    error.message,
                    fallback = "Unable to sync profile"
                )
                Timber.e("Avatar Upload - Remote sync failed: ${error.message}")
            }
            .onSuccess {
                Timber.d("Avatar Upload - Remote sync successful")
            }

        _isSaving.value = false
    }

    private fun sanitizeProfileError(message: String?, fallback: String): String {
        val raw = message?.trim().orEmpty()
        if (raw.isBlank()) return fallback

        return when {
            raw.contains("row-level security policy", ignoreCase = true) -> {
                "Profile save is blocked by database permissions. Please apply the latest recovery SQL and try again."
            }
            raw.contains("Authorization=[Bearer", ignoreCase = true) -> fallback
            raw.contains("http://localhost", ignoreCase = true) && raw.contains("Job was cancelled", ignoreCase = true) -> {
                "Profile update was interrupted. Please wait and try again."
            }
            else -> raw
        }
    }
}
