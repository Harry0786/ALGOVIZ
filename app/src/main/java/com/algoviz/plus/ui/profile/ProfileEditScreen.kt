package com.algoviz.plus.ui.profile

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBackClick: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isSaving by profileViewModel.isSaving.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    
    var name by remember { mutableStateOf(userProfile.name) }
    var email by remember { mutableStateOf(userProfile.email) }
    var bio by remember { mutableStateOf(userProfile.bio) }
    var studyGoal by remember { mutableStateOf(userProfile.studyGoal) }
    var skillLevel by remember { mutableStateOf(userProfile.skillLevel) }
    var showSkillMenu by remember { mutableStateOf(false) }
    var showStudyGoalMenu by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var avatarUri by remember { mutableStateOf<Uri?>(userProfile.avatarUrl?.let { Uri.parse(it) }) }
    var avatarColor by remember { mutableStateOf(userProfile.avatarColorIndex) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val skillLevels = listOf("Beginner", "Intermediate", "Advanced", "Expert")
    val studyGoals = listOf(
        "Master Data Structures & Algorithms",
        "Prepare for Technical Interviews",
        "Improve Problem-Solving Skills",
        "Learn Competitive Programming",
        "Understand Algorithm Complexity"
    )
    val avatarColors = listOf(
        listOf(Color(0xFF5EEAD4), Color(0xFF06B6D4)), // Cyan
        listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)), // Purple
        listOf(Color(0xFFEC4899), Color(0xFFDB2777)), // Pink
        listOf(Color(0xFF10B981), Color(0xFF059669)), // Green
        listOf(Color(0xFFF59E0B), Color(0xFFD97706)), // Orange
        listOf(Color(0xFFEF4444), Color(0xFFDC2626))  // Red
    )
    
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            profileViewModel.uploadAvatarFromGallery(it)
        }
    }

    val galleryPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            showPermissionDeniedDialog = true
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            avatarUri = tempImageUri
            profileViewModel.updateAvatarUrl(tempImageUri.toString())
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Update local state when profile changes
    LaunchedEffect(userProfile) {
        name = userProfile.name
        email = userProfile.email
        bio = userProfile.bio
        studyGoal = userProfile.studyGoal
        skillLevel = userProfile.skillLevel
        avatarColor = userProfile.avatarColorIndex
        if (userProfile.avatarUrl?.isNotEmpty() == true) {
            avatarUri = Uri.parse(userProfile.avatarUrl)
        }
    }
    
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
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Text(
                        text = "Edit Profile",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Button(
                    onClick = {
                        profileViewModel.saveProfileChanges(
                            name = name,
                            email = email,
                            bio = bio,
                            studyGoal = studyGoal,
                            skillLevel = skillLevel,
                            avatarColorIndex = avatarColor,
                            onSaved = onBackClick
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5EEAD4),
                        contentColor = Color(0xFF1A1344)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(44.dp),
                    enabled = !isSaving
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Picture Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(45.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = avatarColors[avatarColor]
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(45.dp))
                            )
                        } else {
                            Text(
                                text = name.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = { showAvatarDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF5EEAD4)
                        )
                    ) {
                        Text(
                            "Change Photo",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Name Field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { 
                            Text("Enter your name", color = Color.White.copy(alpha = 0.4f)) 
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF5EEAD4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF5EEAD4),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Email Field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { 
                            Text("Enter your email", color = Color.White.copy(alpha = 0.4f)) 
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF5EEAD4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF5EEAD4),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bio Field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bio",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        placeholder = {
                            Text("Tell us about your learning focus", color = Color.White.copy(alpha = 0.4f))
                        },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF5EEAD4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF5EEAD4),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Study Goal Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Study Goal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Box {
                        Surface(
                            onClick = { showStudyGoalMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.3f)
                            ),
                            color = Color.White.copy(alpha = 0.03f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = studyGoal,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showStudyGoalMenu,
                            onDismissRequest = { showStudyGoalMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF2D1B69))
                                .fillMaxWidth(0.85f)
                        ) {
                            studyGoals.forEach { goal ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            goal,
                                            color = if (goal == studyGoal) {
                                                Color(0xFF5EEAD4)
                                            } else {
                                                Color.White
                                            },
                                            fontWeight = if (goal == studyGoal) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        studyGoal = goal
                                        showStudyGoalMenu = false
                                    },
                                    modifier = Modifier.background(
                                        if (goal == studyGoal) {
                                            Color.White.copy(alpha = 0.1f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Skill Level Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Skill Level",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Box {
                        Surface(
                            onClick = { showSkillMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.3f)
                            ),
                            color = Color.White.copy(alpha = 0.03f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = skillLevel,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showSkillMenu,
                            onDismissRequest = { showSkillMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF2D1B69))
                                .fillMaxWidth(0.85f)
                        ) {
                            skillLevels.forEach { level ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            level,
                                            color = if (level == skillLevel) {
                                                Color(0xFF5EEAD4)
                                            } else {
                                                Color.White
                                            },
                                            fontWeight = if (level == skillLevel) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )
                                    },
                                    onClick = {
                                        skillLevel = level
                                        showSkillMenu = false
                                    },
                                    modifier = Modifier.background(
                                        if (level == skillLevel) {
                                            Color.White.copy(alpha = 0.1f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
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

                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    galleryPermission
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    galleryPermissionLauncher.launch(galleryPermission)
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
                                // Request camera permission first
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
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

        if (showPermissionDeniedDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDeniedDialog = false },
                containerColor = Color(0xFF1A1344),
                title = {
                    Text(
                        text = "Photo Permission Needed",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Allow photo access to select a profile image from gallery.",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showPermissionDeniedDialog = false
                        galleryPermissionLauncher.launch(galleryPermission)
                    }) {
                        Text("Allow")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDeniedDialog = false }) {
                        Text("Not now")
                    }
                }
            )
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
    }
}
