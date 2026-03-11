package com.algoviz.plus.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
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
import java.io.File
import java.net.URI
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
        data class DownloadFailed(val message: String, val info: UpdateInfo?) : UpdateState()
        object UpToDate : UpdateState()
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    private var lastUpdateInfo: UpdateInfo? = null
    private var pendingInstallLocalUri: String? = null

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
                    val updateInfo = UpdateInfo(
                        versionCode = remoteVersionCode,
                        versionName = doc.getString("versionName") ?: "New Version",
                        apkUrl = doc.getString("apkUrl") ?: "",
                        releaseNotes = doc.getString("releaseNotes") ?: "Bug fixes and improvements.",
                        forceUpdate = doc.getBoolean("forceUpdate") ?: false
                    )
                    lastUpdateInfo = updateInfo
                    _updateState.value = UpdateState.UpdateAvailable(updateInfo)
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
            .setDescription("Downloading latest version...")
            .setDestinationInExternalFilesDir(context, null, "algoviz-update.apk")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .addRequestHeader("User-Agent", "AlgoViz-Android/${BuildConfig.VERSION_NAME}")
            .addRequestHeader("Accept", "application/vnd.android.package-archive,application/octet-stream,*/*")
            .setAllowedOverMetered(true)
        val downloadId = dm.enqueue(request)
        _updateState.value = UpdateState.Downloading(downloadId)
        return downloadId
    }

    fun onDownloadComplete(context: Context, downloadId: Long) {
        val dm = context.getSystemService<DownloadManager>() ?: run {
            _updateState.value = UpdateState.DownloadFailed("Download service unavailable.", lastUpdateInfo)
            return
        }
        val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))
        if (!cursor.moveToFirst()) {
            cursor.close()
            _updateState.value = UpdateState.DownloadFailed("Download record not found.", lastUpdateInfo)
            return
        }

        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
        val localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        cursor.close()

        if (status != DownloadManager.STATUS_SUCCESSFUL || localUri.isNullOrBlank()) {
            _updateState.value = UpdateState.DownloadFailed(reasonToMessage(reason), lastUpdateInfo)
            return
        }

        val installResult = installDownloadedApk(context, localUri)
        if (installResult != null) {
            _updateState.value = UpdateState.DownloadFailed(installResult, lastUpdateInfo)
            return
        }

        _updateState.value = UpdateState.UpToDate
    }

    fun retryLastUpdate() {
        val info = lastUpdateInfo ?: return
        _updateState.value = UpdateState.UpdateAvailable(info)
    }

    fun onAppResumed(context: Context) {
        val localUri = pendingInstallLocalUri ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            return
        }

        val installResult = installDownloadedApk(context, localUri)
        if (installResult != null) {
            _updateState.value = UpdateState.DownloadFailed(installResult, lastUpdateInfo)
            return
        }

        pendingInstallLocalUri = null
        _updateState.value = UpdateState.UpToDate
    }

    private fun installDownloadedApk(context: Context, localUriStr: String): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            pendingInstallLocalUri = localUriStr
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.also { context.startActivity(it) }
            return "Allow install permission and return to app. Installation will continue automatically."
        }

        pendingInstallLocalUri = null

        val file = try {
            File(URI(localUriStr).path)
        } catch (e: Exception) {
            File(Uri.parse(localUriStr).path ?: return "Downloaded APK path is invalid.")
        }

        if (!file.exists()) {
            return "Downloaded APK file not found."
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }.also { context.startActivity(it) }

        return null
    }

    private fun reasonToMessage(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Download interrupted. Please retry."
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage device not found."
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "Downloaded file already exists."
            DownloadManager.ERROR_FILE_ERROR -> "File write error while downloading."
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "Network data error while downloading update."
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Not enough storage space for update."
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects from update server."
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Server rejected the download request."
            DownloadManager.ERROR_UNKNOWN -> "Download failed due to an unknown error."
            DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Waiting for Wi-Fi."
            DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "Waiting for network connection."
            else -> "Download failed (code: $reason)."
        }
    }

    fun dismissUpdate() {
        _updateState.value = UpdateState.UpToDate
    }
}
