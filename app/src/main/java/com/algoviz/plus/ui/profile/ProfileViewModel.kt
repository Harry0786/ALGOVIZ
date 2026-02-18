package com.algoviz.plus.ui.profile

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
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()
    
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
}
