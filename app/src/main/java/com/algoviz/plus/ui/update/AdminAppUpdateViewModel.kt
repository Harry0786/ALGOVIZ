package com.algoviz.plus.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

@HiltViewModel
class AdminAppUpdateViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    @Serializable
    private data class AppConfigRow(
        val id: String,
        @SerialName("version_code") val versionCode: Int,
        @SerialName("version_name") val versionName: String,
        @SerialName("apk_url") val apkUrl: String,
        @SerialName("release_notes") val releaseNotes: String,
        @SerialName("force_update") val forceUpdate: Boolean
    )

    data class FormState(
        val versionCode: String = "",
        val versionName: String = "",
        val apkUrl: String = "",
        val releaseNotes: String = "",
        val forceUpdate: Boolean = false,
        val isLoading: Boolean = false,
        val message: String? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(FormState())
    val state: StateFlow<FormState> = _state.asStateFlow()
    private val appConfigTable = supabaseClient.postgrest["app_config"]

    init {
        loadCurrentConfig()
    }

    fun updateVersionCode(value: String) {
        _state.value = _state.value.copy(versionCode = value.filter { it.isDigit() }, error = null, message = null)
    }

    fun updateVersionName(value: String) {
        _state.value = _state.value.copy(versionName = value, error = null, message = null)
    }

    fun updateApkUrl(value: String) {
        _state.value = _state.value.copy(apkUrl = value, error = null, message = null)
    }

    fun updateReleaseNotes(value: String) {
        _state.value = _state.value.copy(releaseNotes = value, error = null, message = null)
    }

    fun updateForceUpdate(value: Boolean) {
        _state.value = _state.value.copy(forceUpdate = value, error = null, message = null)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, error = null)
    }

    fun publishUpdateConfig() {
        val current = _state.value
        val versionCode = current.versionCode.toIntOrNull()
        if (versionCode == null || versionCode <= 0) {
            _state.value = current.copy(error = "Enter a valid version code")
            return
        }
        if (current.versionName.isBlank()) {
            _state.value = current.copy(error = "Version name is required")
            return
        }
        if (!current.apkUrl.startsWith("http")) {
            _state.value = current.copy(error = "APK URL must start with http or https")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, message = null)
            try {
                val existingConfig = appConfigTable
                    .select {
                        limit(1)
                        filter {
                            eq("id", "latest_version")
                        }
                    }
                    .decodeSingleOrNull<AppConfigRow>()

                val payload = buildJsonObject {
                    put("version_code", versionCode)
                    put("version_name", current.versionName.trim())
                    put("apk_url", current.apkUrl.trim())
                    put("release_notes", current.releaseNotes.trim())
                    put("force_update", current.forceUpdate)
                }

                if (existingConfig == null) {
                    val insertPayload = buildJsonObject {
                        put("id", "latest_version")
                        put("version_code", versionCode)
                        put("version_name", current.versionName.trim())
                        put("apk_url", current.apkUrl.trim())
                        put("release_notes", current.releaseNotes.trim())
                        put("force_update", current.forceUpdate)
                    }
                    appConfigTable.insert(insertPayload)
                } else {
                    appConfigTable.update(payload) {
                        filter {
                            eq("id", "latest_version")
                        }
                    }
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Update configuration published successfully"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to publish update config"
                )
            }
        }
    }

    private fun loadCurrentConfig() {
        viewModelScope.launch {
            try {
                val config = appConfigTable
                    .select {
                        limit(1)
                        filter {
                            eq("id", "latest_version")
                        }
                    }
                    .decodeSingleOrNull<AppConfigRow>() ?: return@launch

                _state.value = _state.value.copy(
                    versionCode = config.versionCode.toString(),
                    versionName = config.versionName,
                    apkUrl = config.apkUrl,
                    releaseNotes = config.releaseNotes,
                    forceUpdate = config.forceUpdate
                )
            } catch (_: Exception) {
                // Keep default values if loading fails.
            }
        }
    }
}
