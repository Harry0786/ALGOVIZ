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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val profileRemoteDataSource: ProfileRemoteDataSource
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        // Load profile data from DataStore
        viewModelScope.launch {
            combine(
                listOf(
                    preferencesManager.profileName,
                    preferencesManager.profileEmail,
                    preferencesManager.profileBio,
                    preferencesManager.profileAvatarUrl,
                    preferencesManager.profileStudyGoal,
                    preferencesManager.profileSkillLevel,
                    preferencesManager.profileAvatarColorIndex
                )
            ) { values ->
                UserProfile(
                    name = values[0] as String,
                    email = values[1] as String,
                    bio = values[2] as String,
                    avatarUrl = values[3] as String,
                    studyGoal = values[4] as String,
                    skillLevel = values[5] as String,
                    avatarColorIndex = values[6] as Int
                )
            }.collect { profile ->
                _userProfile.value = profile
            }
        }

        viewModelScope.launch {
            val remoteProfile = profileRemoteDataSource.getUserProfile().getOrNull()
            if (remoteProfile != null) {
                persistLocalProfile(remoteProfile)
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
            _isSaving.value = true
            _errorMessage.value = null

            val uploadResult = profileRemoteDataSource.uploadProfileImage(imageUri)
            val uploadedUrl = uploadResult.getOrElse {
                _isSaving.value = false
                _errorMessage.value = it.message ?: "Unable to upload profile image"
                return@launch
            }

            preferencesManager.saveProfileAvatarUrl(uploadedUrl)
            val updatedProfile = _userProfile.value.copy(avatarUrl = uploadedUrl)

            profileRemoteDataSource.saveUserProfile(updatedProfile)
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Unable to sync profile"
                }

            _isSaving.value = false
        }
    }

    fun saveProfileChanges(
        name: String,
        email: String,
        bio: String,
        studyGoal: String,
        skillLevel: String,
        avatarColorIndex: Int,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val trimmedName = name.trim().ifEmpty { "AlgoViz User" }
            val trimmedEmail = email.trim().ifEmpty { "user@algoviz.com" }
            val trimmedBio = bio.trim().ifEmpty { "Learning algorithms and data structures" }

            val updatedProfile = _userProfile.value.copy(
                name = trimmedName,
                email = trimmedEmail,
                bio = trimmedBio,
                studyGoal = studyGoal,
                skillLevel = skillLevel,
                avatarColorIndex = avatarColorIndex
            )

            persistLocalProfile(updatedProfile)

            profileRemoteDataSource.saveUserProfile(updatedProfile)
                .onSuccess { onSaved() }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Unable to sync profile"
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
            preferencesManager.saveProfileEmail("user@algoviz.com")
            preferencesManager.saveProfileBio("Learning algorithms and data structures")
            preferencesManager.saveProfileAvatarUrl("")
            preferencesManager.saveProfileStudyGoal("Master algorithms")
            preferencesManager.saveProfileSkillLevel("Beginner")
        }
    }

    private suspend fun persistLocalProfile(profile: UserProfile) {
        preferencesManager.saveProfileName(profile.name)
        preferencesManager.saveProfileEmail(profile.email)
        preferencesManager.saveProfileBio(profile.bio)
        preferencesManager.saveProfileAvatarUrl(profile.avatarUrl ?: "")
        preferencesManager.saveProfileStudyGoal(profile.studyGoal)
        preferencesManager.saveProfileSkillLevel(profile.skillLevel)
        preferencesManager.saveProfileAvatarColorIndex(profile.avatarColorIndex)
    }
}
