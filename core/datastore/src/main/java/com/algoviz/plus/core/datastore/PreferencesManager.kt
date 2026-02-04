package com.algoviz.plus.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    // Clear all preferences
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
