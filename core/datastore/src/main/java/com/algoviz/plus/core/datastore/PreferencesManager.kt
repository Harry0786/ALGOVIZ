package com.algoviz.plus.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.algoviz.plus.core.common.constants.AppConstants
import com.algoviz.plus.core.common.constants.PreferenceKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.PREFERENCES_NAME
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    private val learnItemPrefix = "learn_item_"
    private val learnPlaylistNamePrefix = "learn_playlist_name_"
    private val learnPlaylistItemsPrefix = "learn_playlist_items_"
    private val learnPlaylistIdsKey = stringSetPreferencesKey("learn_playlist_ids")

    data class StoredLearnPlaylist(
        val id: String,
        val name: String,
        val itemIds: Set<String>
    )

    // Auth Token
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.KEY_AUTH_TOKEN)] = token
        }
    }

    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(PreferenceKeys.KEY_AUTH_TOKEN)]
    }

    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(PreferenceKeys.KEY_AUTH_TOKEN))
        }
    }

    // User ID
    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.KEY_USER_ID)] = userId
        }
    }

    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(PreferenceKeys.KEY_USER_ID)]
    }

    // Theme Mode
    suspend fun saveThemeMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(PreferenceKeys.KEY_THEME_MODE)] = isDarkMode
        }
    }

    val themeMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(PreferenceKeys.KEY_THEME_MODE)] ?: false
    }

    // Notifications
    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(PreferenceKeys.KEY_NOTIFICATIONS_ENABLED)] = enabled
        }
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(PreferenceKeys.KEY_NOTIFICATIONS_ENABLED)] ?: true
    }

    // User Profile
    suspend fun saveProfileName(name: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_NAME)] = name
        }
    }

    val profileName: Flow<String> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_NAME)] ?: "AlgoViz User"
    }

    suspend fun saveProfileEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_EMAIL)] = email
        }
    }

    val profileEmail: Flow<String> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_EMAIL)] ?: "user@algoviz.com"
    }

    suspend fun saveProfileBio(bio: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_BIO)] = bio
        }
    }

    val profileBio: Flow<String> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_BIO)] ?: "Learning algorithms and data structures"
    }

    suspend fun saveProfileAvatarUrl(avatarUrl: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_AVATAR_URL)] = avatarUrl
        }
    }

    val profileAvatarUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_AVATAR_URL)] ?: ""
    }

    suspend fun saveProfileStudyGoal(studyGoal: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_STUDY_GOAL)] = studyGoal
        }
    }

    val profileStudyGoal: Flow<String> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_STUDY_GOAL)] ?: "Master algorithms"
    }

    suspend fun saveProfileSkillLevel(skillLevel: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_SKILL_LEVEL)] = skillLevel
        }
    }

    val profileSkillLevel: Flow<String> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(PreferenceKeys.KEY_PROFILE_SKILL_LEVEL)] ?: "Beginner"
    }

    suspend fun saveProfileAvatarColorIndex(colorIndex: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(PreferenceKeys.KEY_PROFILE_AVATAR_COLOR_INDEX)] = colorIndex
        }
    }

    val profileAvatarColorIndex: Flow<Int> = dataStore.data.map { preferences ->
        preferences[intPreferencesKey(PreferenceKeys.KEY_PROFILE_AVATAR_COLOR_INDEX)] ?: 0
    }

    // Learn progress
    suspend fun setLearnItemCompleted(itemId: String, completed: Boolean) {
        val key = booleanPreferencesKey("$learnItemPrefix$itemId")
        dataStore.edit { preferences ->
            if (completed) {
                preferences[key] = true
            } else {
                preferences.remove(key)
            }
        }
    }

    val learnItemCompletion: Flow<Map<String, Boolean>> = dataStore.data.map { preferences ->
        preferences.asMap()
            .mapNotNull { (key, value) ->
                if (key.name.startsWith(learnItemPrefix) && value is Boolean) {
                    key.name.removePrefix(learnItemPrefix) to value
                } else {
                    null
                }
            }
            .toMap()
    }

    suspend fun createLearnPlaylist(name: String): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Playlist name cannot be empty" }

        val id = buildString {
            append(trimmed.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_'))
            append("_")
            append(System.currentTimeMillis())
        }

        dataStore.edit { preferences ->
            val ids = preferences[learnPlaylistIdsKey].orEmpty().toMutableSet()
            ids.add(id)
            preferences[learnPlaylistIdsKey] = ids
            preferences[stringPreferencesKey("$learnPlaylistNamePrefix$id")] = trimmed
            preferences[stringSetPreferencesKey("$learnPlaylistItemsPrefix$id")] = emptySet()
        }
        return id
    }

    suspend fun addLearnItemToPlaylist(playlistId: String, itemId: String) {
        val itemsKey = stringSetPreferencesKey("$learnPlaylistItemsPrefix$playlistId")
        dataStore.edit { preferences ->
            val updatedItems = preferences[itemsKey].orEmpty().toMutableSet()
            updatedItems.add(itemId)
            preferences[itemsKey] = updatedItems
        }
    }

    suspend fun removeLearnItemFromPlaylist(playlistId: String, itemId: String) {
        val itemsKey = stringSetPreferencesKey("$learnPlaylistItemsPrefix$playlistId")
        dataStore.edit { preferences ->
            val updatedItems = preferences[itemsKey].orEmpty().toMutableSet()
            updatedItems.remove(itemId)
            preferences[itemsKey] = updatedItems
        }
    }

    val learnPlaylists: Flow<List<StoredLearnPlaylist>> = dataStore.data.map { preferences ->
        preferences[learnPlaylistIdsKey]
            .orEmpty()
            .map { id ->
                val name = preferences[stringPreferencesKey("$learnPlaylistNamePrefix$id")]
                    ?: "Custom Sheet"
                val items = preferences[stringSetPreferencesKey("$learnPlaylistItemsPrefix$id")].orEmpty()
                StoredLearnPlaylist(id = id, name = name, itemIds = items)
            }
            .sortedBy { it.name.lowercase() }
    }

    // Clear all preferences
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
