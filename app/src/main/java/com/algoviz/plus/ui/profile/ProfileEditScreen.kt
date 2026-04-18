package com.algoviz.plus.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import timber.log.Timber
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBackClick: () -> Unit,
    onSavedClick: () -> Unit = onBackClick,
    isFirstTimeOnboarding: Boolean = false,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isSaving by profileViewModel.isSaving.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    
    var name by remember { mutableStateOf(userProfile.name) }
    var username by remember { mutableStateOf(userProfile.username) }
    var email by remember { mutableStateOf(userProfile.email) }
    var phoneNumber by remember { mutableStateOf(userProfile.phoneNumber) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showCameraPermissionDeniedDialog by remember { mutableStateOf(false) }
    var avatarUri by remember { mutableStateOf<Uri?>(userProfile.avatarUrl?.let { Uri.parse(it) }) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var hasInitializedForm by remember { mutableStateOf(false) }
    var isFormDirty by remember { mutableStateOf(false) }
    val onboardingMode = remember { isFirstTimeOnboarding }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            profileViewModel.uploadAvatarFromGallery(it)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            avatarUri = tempImageUri
            tempImageUri?.let { uri ->
                profileViewModel.uploadAvatarFromCamera(uri)
            }
        }
    }

    val launchCameraCapture = {
        try {
            val file = File.createTempFile(
                "avatar_",
                ".jpg",
                context.cacheDir
            )
            tempImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            tempImageUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } catch (_: Exception) {
            showCameraPermissionDeniedDialog = true
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraCapture.invoke()
        } else {
            showCameraPermissionDeniedDialog = true
        }
    }

    // Always keep avatar preview synced with saved profile avatar.
    LaunchedEffect(userProfile.avatarUrl) {
        Timber.d("ProfileEditScreen - LaunchedEffect avatarUrl: ${userProfile.avatarUrl ?: "null"}")
        avatarUri = userProfile.avatarUrl?.takeIf { it.isNotBlank() }?.let(Uri::parse).also {
            Timber.d("ProfileEditScreen - Parsed URI: $it")
        }
    }

    // Initialize form once; avoid clobbering in-progress user edits from async profile refreshes.
    LaunchedEffect(userProfile.name, userProfile.username, userProfile.email, userProfile.phoneNumber) {
        if (!hasInitializedForm || !isFormDirty) {
            name = userProfile.name
            username = userProfile.username
            email = userProfile.email
            phoneNumber = userProfile.phoneNumber
            hasInitializedForm = true
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = com.algoviz.plus.R.drawable.auth_bg1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x55FFFFFF),
                            Color(0x22000000),
                            Color.Transparent
                        ),
                        radius = 720f
                    )
                )
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp),
                    enabled = !isSaving  // Disable back while uploading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isSaving) Color.Gray else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = if (onboardingMode) "Complete User Information" else "Edit Profile",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .background(Color(0xFFB7B7BA)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Profile Picture",
                                placeholder = androidx.compose.ui.res.painterResource(id = com.algoviz.plus.R.drawable.user),
                                error = androidx.compose.ui.res.painterResource(id = com.algoviz.plus.R.drawable.user),
                                fallback = androidx.compose.ui.res.painterResource(id = com.algoviz.plus.R.drawable.user),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.algoviz.plus.R.drawable.user),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = (-4).dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1D24)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { showAvatarDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.92f))
                ) {
                    Text(text = "Change Photo", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Name", color = Color.White.copy(alpha = 0.72f), fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF34363B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isFormDirty = true
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFFE7E8EC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    decorationBox = { inner ->
                        if (name.isBlank()) {
                            Text("Enter your name", color = Color(0xFFB7BAC1), fontSize = 16.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Username", color = Color.White.copy(alpha = 0.72f), fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF34363B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        isFormDirty = true
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFFE7E8EC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    decorationBox = { inner ->
                        if (username.isBlank()) {
                            Text("Enter your username", color = Color(0xFFB7BAC1), fontSize = 16.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Email", color = Color.White.copy(alpha = 0.72f), fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF34363B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isFormDirty = true
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFFE7E8EC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    decorationBox = { inner ->
                        if (email.isBlank()) {
                            Text("Enter your email", color = Color(0xFFB7BAC1), fontSize = 16.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Phone Number", color = Color.White.copy(alpha = 0.72f), fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF34363B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        isFormDirty = true
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFFE7E8EC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    decorationBox = { inner ->
                        if (phoneNumber.isBlank()) {
                            Text("Enter your phone number", color = Color(0xFFB7BAC1), fontSize = 16.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            Surface(
                onClick = {
                    isFormDirty = false
                    profileViewModel.saveProfileChanges(
                        name = name,
                        username = username,
                        email = email,
                        phoneNumber = phoneNumber,
                        onSaved = onSavedClick
                    )
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(26.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF42444A), Color(0xFF5A5D64))
                        )
                    )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isSaving) "Saving..." else "Save Changes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
        
        // Avatar Photo Options Dialog
        if (showAvatarDialog) {
            AlertDialog(
                onDismissRequest = { showAvatarDialog = false },
                containerColor = Color(0xFF1A1344),
                title = {
                    Text(
                        text = "Change Profile Photo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Gallery Option
                        Surface(
                            onClick = {
                                showAvatarDialog = false
                                galleryLauncher.launch("image/*")
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF5EEAD4).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PhotoLibrary,
                                        contentDescription = "Gallery",
                                        tint = Color(0xFF5EEAD4),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Choose from Gallery",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Select a photo from your library",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        
                        // Camera Option
                        Surface(
                            onClick = {
                                showAvatarDialog = false
                                val hasCameraPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                if (hasCameraPermission) {
                                    launchCameraCapture.invoke()
                                } else {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Camera",
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Take a Photo",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Capture a new photo with camera",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { showAvatarDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (isSaving) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                color = Color(0xFF2A2E36),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF6366F1)
                    )
                    Text(
                        text = "Uploading avatar...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { profileViewModel.clearError() },
                containerColor = Color(0xFF1A1344),
                title = {
                    Text(
                        text = "Profile Update Failed",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { profileViewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showCameraPermissionDeniedDialog) {
            AlertDialog(
                onDismissRequest = { showCameraPermissionDeniedDialog = false },
                containerColor = Color(0xFF1A1344),
                title = {
                    Text(
                        text = "Camera Permission Needed",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Allow camera access to take a profile photo.",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showCameraPermissionDeniedDialog = false
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }) {
                        Text("Allow")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            showCameraPermissionDeniedDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Settings")
                        }

                        TextButton(onClick = { showCameraPermissionDeniedDialog = false }) {
                            Text("Not now")
                        }
                    }
                }
            )
        }
    }
}
