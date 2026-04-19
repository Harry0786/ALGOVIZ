package com.algoviz.plus.core.common.constants

object AppConstants {
    const val DATABASE_NAME = "algoviz_database"
    const val PREFERENCES_NAME = "algoviz_preferences"
    
    const val NETWORK_TIMEOUT = 30L
    const val CACHE_SIZE = 10L * 1024 * 1024 // 10 MB
    
    const val PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 10
}

object ApiConstants {
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"
    
    const val CONTENT_TYPE_JSON = "application/json"
}

object PreferenceKeys {
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    
    // User Profile
    const val KEY_PROFILE_NAME = "profile_name"
    const val KEY_PROFILE_EMAIL = "profile_email"
    const val KEY_PROFILE_USERNAME = "profile_username"
    const val KEY_PROFILE_PHONE_NUMBER = "profile_phone_number"
    const val KEY_PROFILE_AVATAR_URL = "profile_avatar_url"
    const val KEY_PROFILE_AVATAR_COLOR_INDEX = "profile_avatar_color_index"
    const val KEY_PROFILE_ONBOARDING_COMPLETED = "profile_onboarding_completed"
}
