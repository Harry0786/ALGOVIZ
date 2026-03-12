package com.algoviz.plus.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.algoviz.plus.features.auth.presentation.components.PasswordTextField
import com.algoviz.plus.features.auth.presentation.components.PrimaryButton
import com.algoviz.plus.features.auth.presentation.state.AuthUiState
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.algoviz.plus.ui.profile.ProfileViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.algoviz.plus.BuildConfig
import com.algoviz.plus.ui.notifications.InAppNotification
import com.algoviz.plus.ui.notifications.InAppNotificationCenter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit,
    onVisualize: () -> Unit = {},
    onStudyRooms: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(authUiState) {
        when (authUiState) {
            is AuthUiState.PasswordChanged -> {
                InAppNotificationCenter.post(
                    InAppNotification(
                        title = "Password updated",
                        message = "Your password was changed successfully.",
                        type = com.algoviz.plus.ui.notifications.InAppNotificationType.Success,
                        groupKey = "home_success_actions",
                        dedupeKey = "home_pwd_changed"
                    )
                )
                showChangePasswordDialog = false
                authViewModel.clearError()
            }
            is AuthUiState.Error -> {
                val errorMessage = (authUiState as AuthUiState.Error).message
                InAppNotificationCenter.post(
                    InAppNotification(
                        title = "Account action failed",
                        message = errorMessage,
                        type = com.algoviz.plus.ui.notifications.InAppNotificationType.Error,
                        groupKey = "home_account_errors",
                        dedupeKey = "home_auth_error:$errorMessage"
                    )
                )
                authViewModel.clearError()
            }
            else -> {}
        }
    }
    
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    UserDrawerContent(
                        userProfile = userProfile,
                        onProfileClick = {
                            scope.launch { drawerState.close() }
                            onProfileClick()
                        },
                        onChangePasswordClick = {
                            scope.launch { drawerState.close() }
                            showChangePasswordDialog = true
                        },
                        onShareAppClick = {
                            scope.launch { 
                                drawerState.close()
                                try {
                                    // Get APK path
                                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                                    val apkPath = packageInfo.applicationInfo.sourceDir
                                    val apkFile = File(apkPath)
                                    
                                    if (apkFile.exists()) {
                                        // Copy APK to cache directory
                                        val cacheDir = File(context.cacheDir, "apk")
                                        if (!cacheDir.exists()) cacheDir.mkdirs()
                                        
                                        val copiedApk = File(cacheDir, "AlgoViz.apk")
                                        apkFile.inputStream().use { input ->
                                            copiedApk.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        
                                        // Get URI for the copied APK
                                        val apkUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            copiedApk
                                        )
                                        
                                        // Share the APK
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/vnd.android.package-archive"
                                            putExtra(Intent.EXTRA_STREAM, apkUri)
                                            putExtra(Intent.EXTRA_SUBJECT, "AlgoViz+ App")
                                            putExtra(Intent.EXTRA_TEXT, "Check out AlgoViz+ - Learn algorithms through visualization!")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        
                                        context.startActivity(Intent.createChooser(shareIntent, "Share AlgoViz+ App"))
                                    } else {
                                        InAppNotificationCenter.post(
                                            InAppNotification(
                                                title = "Share unavailable",
                                                message = "The app package could not be prepared for sharing.",
                                                type = com.algoviz.plus.ui.notifications.InAppNotificationType.Error,
                                                groupKey = "home_share_errors",
                                                dedupeKey = "home_share_unable"
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    val errorMessage = "Error sharing app: ${e.message}"
                                    InAppNotificationCenter.post(
                                        InAppNotification(
                                            title = "Share failed",
                                            message = errorMessage,
                                            type = com.algoviz.plus.ui.notifications.InAppNotificationType.Error,
                                            groupKey = "home_share_errors",
                                            dedupeKey = "home_share_error:${e.message}"
                                        )
                                    )
                                }
                            }
                        },
                        onLogoutClick = {
                            scope.launch { drawerState.close() }
                            onLogoutClick()
                        }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    containerColor = Color.Transparent
                ) { _ ->
        Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1344),
                        Color(0xFF2D1B69),
                        Color(0xFF3D2080)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (drawerState.isOpen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(radius = 16.dp)
                    } else {
                        Modifier
                    }
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(scrollState)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppLogo(size = 40.dp)
                    Text(
                        text = "AlgoViz+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                IconButton(
                    onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Welcome Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Master Algorithms\nThrough Visualization",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 38.sp
                )
                Text(
                    text = "Interactive learning platform for data structures and algorithms",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    lineHeight = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Quick Actions Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Quick Start",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.PlayArrow,
                        title = "Visualize",
                        subtitle = "Algorithms",
                        gradient = listOf(Color(0xFF06B6D4), Color(0xFF0891B2)),
                        onClick = onVisualize
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Groups,
                        title = "Study Rooms",
                        subtitle = "Collaborate",
                        gradient = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
                        onClick = onStudyRooms
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.School,
                        title = "Learn",
                        subtitle = "Concepts",
                        gradient = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
                        onClick = { /* Navigate */ }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Person,
                        title = "Profile",
                        subtitle = "Account",
                        gradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
                        onClick = onProfileClick
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Popular Topics
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Popular Topics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                TopicCard(
                    icon = Icons.Outlined.SwapVert,
                    title = "Sorting Algorithms",
                    description = "Bubble, Quick, Merge & more",
                    progress = 0.65f
                )
                
                TopicCard(
                    icon = Icons.Outlined.AccountTree,
                    title = "Graph Algorithms",
                    description = "BFS, DFS, Dijkstra & more",
                    progress = 0.40f
                )
                
                TopicCard(
                    icon = Icons.Outlined.DataObject,
                    title = "Data Structures",
                    description = "Arrays, Trees, Heaps & more",
                    progress = 0.80f
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (drawerState.isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
            )
        }
    }
                }
            }
        }
    }
    
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = { currentPwd, newPwd ->
                authViewModel.changePassword(currentPwd, newPwd)
            },
            isLoading = authUiState is AuthUiState.Loading
        )
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(gradient)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                
                Column {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicCard(
    icon: ImageVector,
    title: String,
    description: String,
    progress: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF5EEAD4).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF5EEAD4),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5EEAD4)
                )
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF5EEAD4),
                trackColor = Color.White.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
private fun UserDrawerContent(
    userProfile: com.algoviz.plus.ui.profile.UserProfile,
    onProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val versionLabel = "Version ${BuildConfig.VERSION_NAME.substringBefore('-')}"
    val avatarColors = listOf(
        listOf(Color(0xFF5EEAD4), Color(0xFF06B6D4)), // Cyan
        listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)), // Purple
        listOf(Color(0xFFEC4899), Color(0xFFDB2777)), // Pink
        listOf(Color(0xFF10B981), Color(0xFF059669)), // Green
        listOf(Color(0xFFF59E0B), Color(0xFFD97706)), // Orange
        listOf(Color(0xFFEF4444), Color(0xFFDC2626))  // Red
    )
    
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF1A1344),
        drawerContentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1344),
                            Color(0xFF2D1B69),
                            Color(0xFF1A1344)
                        )
                    )
                )
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // User Profile Section - Clickable
            Surface(
                onClick = onProfileClick,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(48.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = avatarColors.getOrNull(userProfile.avatarColorIndex) 
                                    ?: avatarColors[0]
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userProfile.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = userProfile.avatarUrl,
                            contentDescription = "Profile Picture",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(48.dp))
                        )
                    } else {
                        Text(
                            text = userProfile.name.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = userProfile.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = userProfile.email,
                        fontSize = 14.sp,
                        color = Color(0xFF5EEAD4),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Menu Items
            DrawerMenuItem(
                icon = Icons.Outlined.Person,
                title = "Profile",
                onClick = onProfileClick
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Lock,
                title = "Change Password",
                onClick = onChangePasswordClick
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Favorite,
                title = "Favorites",
                onClick = { /* Navigate to favorites */ }
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Share,
                title = "Share App",
                onClick = onShareAppClick
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = versionLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.55f),
                letterSpacing = 0.2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Logout Button
            DrawerMenuItem(
                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                title = "Logout",
                onClick = onLogoutClick,
                isDestructive = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isDestructive) 
                            Color(0xFFEF4444).copy(alpha = 0.15f)
                        else 
                            Color(0xFF5EEAD4).copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isDestructive) Color(0xFFEF4444) else Color(0xFF5EEAD4),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = title,
                fontSize = 15.sp,
                color = if (isDestructive) Color(0xFFEF4444) else Color.White,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun AppLogo(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawable = ContextCompat.getDrawable(context, com.algoviz.plus.R.mipmap.ic_launcher)
    
    val painter = remember(drawable) {
        drawable?.let {
            val bitmap = if (it is BitmapDrawable) {
                it.bitmap
            } else {
                val bmp = Bitmap.createBitmap(
                    it.intrinsicWidth.takeIf { w -> w > 0 } ?: 1,
                    it.intrinsicHeight.takeIf { h -> h > 0 } ?: 1,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bmp)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
                bmp
            }
            BitmapPainter(bitmap.asImageBitmap())
        }
    }
    
    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = "AlgoViz Logo",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.25f))
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    isLoading: Boolean
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        text = {
            Column {
                PasswordTextField(
                    value = currentPassword,
                    onValueChange = { 
                        currentPassword = it
                        errorMessage = null
                    },
                    label = "Current Password",
                    imeAction = ImeAction.Next
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                PasswordTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        errorMessage = null
                    },
                    label = "New Password",
                    imeAction = ImeAction.Next
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                PasswordTextField(
                    value = confirmNewPassword,
                    onValueChange = { 
                        confirmNewPassword = it
                        errorMessage = null
                    },
                    label = "Confirm New Password",
                    imeAction = ImeAction.Done
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Change Password",
                onClick = {
                    when {
                        currentPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank() -> {
                            errorMessage = "All fields are required"
                        }
                        newPassword != confirmNewPassword -> {
                            errorMessage = "New passwords do not match"
                        }
                        newPassword.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                        }
                        else -> {
                            onChangePassword(currentPassword, newPassword)
                        }
                    }
                },
                enabled = !isLoading,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF5EEAD4)
                )
            }
        },
        containerColor = Color(0xFF1A1344),
        shape = RoundedCornerShape(16.dp)
    )
}
