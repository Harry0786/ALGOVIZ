package com.algoviz.plus.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AdminAppUpdateViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

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
                firestore.collection("app_config")
                    .document("latest_version")
                    .set(
                        mapOf(
                            "versionCode" to versionCode,
                            "versionName" to current.versionName.trim(),
                            "apkUrl" to current.apkUrl.trim(),
                            "releaseNotes" to current.releaseNotes.trim(),
                            "forceUpdate" to current.forceUpdate,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()

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
                val doc = firestore.collection("app_config")
                    .document("latest_version")
                    .get()
                    .await()

                if (!doc.exists()) return@launch

                _state.value = _state.value.copy(
                    versionCode = (doc.getLong("versionCode") ?: 0L).toString(),
                    versionName = doc.getString("versionName") ?: "",
                    apkUrl = doc.getString("apkUrl") ?: "",
                    releaseNotes = doc.getString("releaseNotes") ?: "",
                    forceUpdate = doc.getBoolean("forceUpdate") ?: false
                )
            } catch (_: Exception) {
                // Keep default values if loading fails.
            }
        }
    }
}
