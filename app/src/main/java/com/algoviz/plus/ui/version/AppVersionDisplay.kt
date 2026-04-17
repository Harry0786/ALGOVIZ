package com.algoviz.plus.ui.version

import com.algoviz.plus.BuildConfig

fun installedVersionLabel(): String {
    return if (BuildConfig.DEBUG) {
        "DEBUG ${BuildConfig.VERSION_NAME.uppercase()}"
    } else {
        BuildConfig.VERSION_NAME.substringBefore('-').uppercase()
    }
}

fun versionStatusText(latestVersionName: String?): String {
    val installed = installedVersionLabel()
    val latest = latestVersionName?.takeIf { it.isNotBlank() } ?: BuildConfig.VERSION_NAME
    return "Installed: $installed  •  Latest: $latest"
}