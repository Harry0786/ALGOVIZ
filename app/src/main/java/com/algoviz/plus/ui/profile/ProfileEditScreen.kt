package com.algoviz.plus.ui.profile

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.VisualTransformation
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
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = com.algoviz.plus.R.drawable.auth_bg1),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Edit Profile",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFBFC1C6)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(168.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Profile Picture",
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
                            .offset(x = (-2).dp, y = (-10).dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1D24)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { showAvatarDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.92f))
                ) {
                    Text(text = "Change Photo", fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "Name", color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF35373D).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFFE7E8EC),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 19.dp),
                    decorationBox = { inner ->
                        if (name.isBlank()) {
                            Text("Enter your name", color = Color(0xFFB7BAC1), fontSize = 17.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Email", color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF35373D).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFFE7E8EC),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 19.dp),
                    decorationBox = { inner ->
                        if (email.isBlank()) {
                            Text("Enter your email", color = Color(0xFFB7BAC1), fontSize = 17.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Bio", color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF35373D).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    singleLine = false,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFFE7E8EC),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    decorationBox = { inner ->
                        if (bio.isBlank()) {
                            Text("Tell us about your learning focus", color = Color(0xFFB7BAC1), fontSize = 17.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Study Goal", color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Surface(
                    onClick = { showStudyGoalMenu = true },
                    color = Color(0xFF35373D).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = studyGoal,
                            color = Color(0xFFE7E8EC),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF8B90A0))
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF8B90A0))
                        }
                    }
                }

                DropdownMenu(
                    expanded = showStudyGoalMenu,
                    onDismissRequest = { showStudyGoalMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF1A1C23))
                        .fillMaxWidth(0.92f)
                ) {
                    studyGoals.forEach { goal ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    goal,
                                    color = if (goal == studyGoal) Color.White else Color.White.copy(alpha = 0.78f),
                                    fontWeight = if (goal == studyGoal) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                studyGoal = goal
                                showStudyGoalMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Skill Level", color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Surface(
                    onClick = { showSkillMenu = true },
                    color = Color(0xFF35373D).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = skillLevel,
                            color = Color(0xFFE7E8EC),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF8B90A0))
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF8B90A0))
                        }
                    }
                }

                DropdownMenu(
                    expanded = showSkillMenu,
                    onDismissRequest = { showSkillMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF1A1C23))
                        .fillMaxWidth(0.92f)
                ) {
                    skillLevels.forEach { level ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    level,
                                    color = if (level == skillLevel) Color.White else Color.White.copy(alpha = 0.78f),
                                    fontWeight = if (level == skillLevel) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                skillLevel = level
                                showSkillMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(42.dp))

            Surface(
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
