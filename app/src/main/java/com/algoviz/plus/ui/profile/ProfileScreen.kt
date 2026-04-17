package com.algoviz.plus.ui.profile

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.BuildConfig
import com.algoviz.plus.R
import com.algoviz.plus.features.auth.presentation.state.AuthUiState
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.algoviz.plus.update.AppUpdateViewModel
import com.algoviz.plus.ui.version.installedVersionLabel
import com.algoviz.plus.ui.version.versionStatusText
import java.io.File

@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.PasswordChanged -> {
                showChangePasswordDialog = false
                passwordChangeError = null
                authViewModel.clearError()
            }
            is AuthUiState.Error -> {
                if (showChangePasswordDialog) {
                    passwordChangeError = state.message
                }
                authViewModel.clearError()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (showChangePasswordDialog) Modifier.blur(12.dp) else Modifier)
        ) {
            Image(
                painter = painterResource(id = R.drawable.auth_bg1),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
            Spacer(modifier = Modifier.height(48.dp))

            ProfileAvatarHeader(
                name = userProfile.name,
                email = userProfile.email
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileActionCard(
                icon = Icons.Outlined.Person,
                title = "Profile",
                subtitle = "ACCOUNT DETAILS",
                onClick = onEditProfileClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileActionCard(
                icon = Icons.Outlined.Lock,
                title = "Change Password",
                subtitle = "SECURITY SETTINGS",
                onClick = { showChangePasswordDialog = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileActionCard(
                icon = Icons.Outlined.Favorite,
                title = "Favorites",
                subtitle = "SAVED ALGORITHMS",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileActionCard(
                icon = Icons.Outlined.Share,
                title = "Share App",
                subtitle = "INVITE FRIENDS",
                onClick = { shareAppPackage(context) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                onClick = onLogoutClick,
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFF2F2F4),
                                Color(0xFFE1E2E6)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Logout",
                        color = Color(0xFF111111),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "VERSION ${installedVersionLabel()}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color(0xFFB8B8BA),
                letterSpacing = 2.2.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (val state = updateState) {
                    is AppUpdateViewModel.UpdateState.UpdateAvailable -> versionStatusText(state.info.versionName)
                    is AppUpdateViewModel.UpdateState.Downloading -> "Installed: ${installedVersionLabel()}  •  Update downloading"
                    is AppUpdateViewModel.UpdateState.DownloadFailed -> versionStatusText(state.info?.versionName)
                    else -> versionStatusText(null)
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = Color(0xFF8F9095),
                letterSpacing = 0.4.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(34.dp)
                    .clip(RoundedCornerShape(99.dp)),
                thickness = 4.dp,
                color = Color(0xFF7B7C80).copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(androidx.compose.foundation.layout.WindowInsets.navigationBars))
            }
        }
    }

    if (showChangePasswordDialog) {
        ProfileChangePasswordDialog(
            onDismiss = {
                showChangePasswordDialog = false
                passwordChangeError = null
            },
            onChangePassword = { currentPwd, newPwd ->
                authViewModel.changePassword(currentPwd, newPwd)
            },
            backendErrorMessage = passwordChangeError,
            onInputChanged = { passwordChangeError = null },
            isLoading = authUiState is AuthUiState.Loading
        )
    }
}

@Composable
private fun ProfileAvatarHeader(
    name: String,
    email: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Color(0xFF10131A))
                .border(4.dp, Color(0xFFBFC1C7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "Profile image",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = name,
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp,
            lineHeight = 28.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = email,
            color = Color(0xFF9A9A9C),
            fontSize = 15.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun ProfileActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val iconBg = Color(0xFF1E1F24)
    val iconTint = Color(0xFFF0F0F0)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.profile_container),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color(0xFFF6F6F7),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFFA8A8AB),
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF6E6E72),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    backendErrorMessage: String?,
    onInputChanged: () -> Unit,
    isLoading: Boolean
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCurrentPassword by remember { mutableStateOf(false) }
    val dialogFont = FontFamily.SansSerif

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .wrapContentHeight()
                    .heightIn(max = 620.dp)
                    .clip(RoundedCornerShape(30.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg5),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.36f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                Text(
                    text = "CHANGE PASSWORD",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.2.sp,
                    fontFamily = dialogFont
                )

                Spacer(modifier = Modifier.height(6.dp))

                ChangePasswordField(
                    label = "Current Password",
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        errorMessage = null
                        onInputChanged()
                    },
                    trailingIcon = if (showCurrentPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    onTrailingClick = { showCurrentPassword = !showCurrentPassword },
                    visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    imeAction = ImeAction.Next
                )

                ChangePasswordField(
                    label = "New Password",
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                        onInputChanged()
                    },
                    trailingIcon = Icons.Outlined.Lock,
                    onTrailingClick = null,
                    visualTransformation = PasswordVisualTransformation(),
                    imeAction = ImeAction.Next
                )

                ChangePasswordField(
                    label = "Confirm Password",
                    value = confirmNewPassword,
                    onValueChange = {
                        confirmNewPassword = it
                        errorMessage = null
                        onInputChanged()
                    },
                    trailingIcon = Icons.Outlined.Shield,
                    onTrailingClick = null,
                    visualTransformation = PasswordVisualTransformation(),
                    imeAction = ImeAction.Done
                )

                Row(
                    modifier = Modifier.fillMaxWidth(0.94f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFFAEB0B5),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Requires at least 8 characters",
                        color = Color(0xFFC9CBD0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = dialogFont
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        modifier = Modifier.fillMaxWidth(0.94f),
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = dialogFont
                    )
                }

                if (!backendErrorMessage.isNullOrBlank()) {
                    Text(
                        text = backendErrorMessage,
                        modifier = Modifier.fillMaxWidth(0.94f),
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = dialogFont
                    )
                }

                Surface(
                    onClick = {
                        when {
                            currentPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank() -> {
                                errorMessage = "All fields are required"
                            }
                            newPassword != confirmNewPassword -> {
                                errorMessage = "Passwords do not match"
                            }
                            newPassword.length < 8 -> {
                                errorMessage = "Password must be at least 8 characters"
                            }
                            else -> {
                                onChangePassword(currentPassword, newPassword)
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xEE0A0E16), Color(0xC7141820))
                            )
                        )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isLoading) "Changing..." else "Change Password",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = dialogFont
                        )
                    }
                }

                Surface(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFE7E7EA),
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .height(58.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF16181D),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = dialogFont
                        )
                    }
                }

                Text(
                    text = "Forgot current?",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 2.dp)
                        .clickable(enabled = !isLoading) { },
                    textAlign = TextAlign.Center,
                    color = Color(0xFFE5E6EA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = dialogFont
                )
            }
            }
        }
    }
}

@Composable
private fun ChangePasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    trailingIcon: ImageVector,
    onTrailingClick: (() -> Unit)?,
    visualTransformation: VisualTransformation,
    imeAction: ImeAction
) {
    val dialogFont = FontFamily.SansSerif

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(0.94f),
            color = Color(0xFFD5D7DC),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.9.sp,
            fontFamily = dialogFont
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(54.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8E8EA))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF1B1D21),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = dialogFont
                    ),
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        imeAction = imeAction,
                        keyboardType = KeyboardType.Password
                    ),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
                                color = Color(0xFF6C6E73),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = dialogFont
                            )
                        }
                        innerTextField()
                    }
                )

                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = Color(0xFF16181D),
                    modifier = Modifier
                        .size(22.dp)
                        .then(
                            if (onTrailingClick != null) Modifier.clickable { onTrailingClick() } else Modifier
                        )
                )
            }
        }
    }
}

private fun shareAppPackage(context: android.content.Context) {
    try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val apkPath = packageInfo.applicationInfo.sourceDir
        val sourceApk = File(apkPath)
        if (!sourceApk.exists()) {
            return
        }

        val cacheDir = File(context.cacheDir, "apk")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val sharedApk = File(cacheDir, "AlgoViz-${BuildConfig.VERSION_NAME}.apk")
        sourceApk.inputStream().use { input ->
            sharedApk.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            sharedApk
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            putExtra(Intent.EXTRA_SUBJECT, "AlgoViz+ App")
            putExtra(Intent.EXTRA_TEXT, "Install AlgoViz+ app")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share AlgoViz+ App"))
    } catch (_: Exception) {
        // Keep silent to avoid blocking the rest of the profile flow.
    }
}
