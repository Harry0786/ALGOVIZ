package com.algoviz.plus.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val apkUrl: String,
        val releaseNotes: String,
        val forceUpdate: Boolean
    )

    sealed class UpdateState {
        object Idle : UpdateState()
        data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
        data class Downloading(val downloadId: Long) : UpdateState()
        object UpToDate : UpdateState()
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    init {
        checkForUpdate()
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("app_config")
                    .document("latest_version")
                    .get()
                    .await()

                if (!doc.exists()) {
                    _updateState.value = UpdateState.UpToDate
                    return@launch
                }

                val remoteVersionCode = doc.getLong("versionCode")?.toInt() ?: 0
                if (remoteVersionCode > BuildConfig.VERSION_CODE) {
                    _updateState.value = UpdateState.UpdateAvailable(
                        UpdateInfo(
                            versionCode = remoteVersionCode,
                            versionName = doc.getString("versionName") ?: "New Version",
                            apkUrl = doc.getString("apkUrl") ?: "",
                            releaseNotes = doc.getString("releaseNotes") ?: "Bug fixes and improvements.",
                            forceUpdate = doc.getBoolean("forceUpdate") ?: false
                        )
                    )
                } else {
                    _updateState.value = UpdateState.UpToDate
                }
            } catch (e: Exception) {
                // Fail silently — never block the app if version check fails
                _updateState.value = UpdateState.UpToDate
            }
        }
    }

    fun startDownload(context: Context, apkUrl: String): Long {
        val dm = context.getSystemService<DownloadManager>()!!
        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("AlgoViz Update")
            .setDescription("Downloading latest version…")
            .setDestinationInExternalFilesDir(context, null, "algoviz-update.apk")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
        val downloadId = dm.enqueue(request)
        _updateState.value = UpdateState.Downloading(downloadId)
        return downloadId
    }

    fun dismissUpdate() {
        _updateState.value = UpdateState.UpToDate
    }
}
