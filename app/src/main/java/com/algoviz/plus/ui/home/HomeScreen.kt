package com.algoviz.plus.ui.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.features.auth.presentation.components.PasswordTextField
import com.algoviz.plus.features.auth.presentation.components.PrimaryButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.algoviz.plus.ui.learn.viewmodel.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onVisualize: () -> Unit = {},
    onLearn: () -> Unit = {},
    onStudyRooms: () -> Unit = {},
    learnViewModel: LearnViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val sheetProgressList by learnViewModel.homeSheetProgress.collectAsStateWithLifecycle()
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
                    onClick = onProfileClick,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "VIEW ALL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        icon = Icons.Outlined.PlayArrow,
                        title = "Visualize",
                        subtitle = "Algorithms",
                        onClick = onVisualize
                    )
                    QuickActionCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        icon = Icons.Outlined.Groups,
                        title = "Study",
                        subtitle = "Rooms",
                        onClick = onStudyRooms
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = "Learn",
                        subtitle = "Concepts",
                        onClick = onLearn
                    )
                    QuickActionCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        icon = Icons.Outlined.Person,
                        title = "Profile",
                        subtitle = "Settings",
                        onClick = onProfileClick
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sheet Progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sheet Progress",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                sheetProgressList.forEachIndexed { index, item ->
                    val icon = when (index % 3) {
                        0 -> Icons.AutoMirrored.Outlined.MenuBook
                        1 -> Icons.Outlined.AccountTree
                        else -> Icons.Outlined.School
                    }
                    TopicCard(
                        icon = icon,
                        title = item.title,
                        description = item.subtitle,
                        progress = item.progress
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = com.algoviz.plus.R.drawable.quick_action_button),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(37.dp))
                        .background(Color(0xFFE8E8E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
private fun AppLogo(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawable = ContextCompat.getDrawable(context, com.algoviz.plus.R.drawable.app_logo)

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
