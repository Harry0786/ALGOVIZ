package com.algoviz.plus.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.ui.notifications.InAppNotification
import com.algoviz.plus.ui.notifications.InAppNotificationCenter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppUpdateScreen(
    onBackClick: () -> Unit,
    viewModel: AdminAppUpdateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.message, state.error) {
        state.message?.let {
            InAppNotificationCenter.post(
                InAppNotification(
                    title = "Update published",
                    message = it,
                    type = com.algoviz.plus.ui.notifications.InAppNotificationType.Update,
                    groupKey = "admin_update_success",
                    dedupeKey = "admin_update_msg:$it"
                )
            )
            viewModel.clearMessage()
        }
        state.error?.let {
            InAppNotificationCenter.post(
                InAppNotification(
                    title = "Update publish failed",
                    message = it,
                    type = com.algoviz.plus.ui.notifications.InAppNotificationType.Error,
                    groupKey = "admin_update_errors",
                    dedupeKey = "admin_update_err:$it"
                )
            )
            viewModel.clearMessage()
        }
    }

    val bg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1344),
            Color(0xFF2D1B69),
            Color(0xFF3D2080)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text("Admin App Updates", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !state.isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Publish update metadata to Supabase",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = state.versionCode,
                    onValueChange = viewModel::updateVersionCode,
                    label = { Text("Version Code", color = Color.White.copy(alpha = 0.7f)) },
                    enabled = !state.isLoading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                OutlinedTextField(
                    value = state.versionName,
                    onValueChange = viewModel::updateVersionName,
                    label = { Text("Version Name", color = Color.White.copy(alpha = 0.7f)) },
                    enabled = !state.isLoading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                OutlinedTextField(
                    value = state.apkUrl,
                    onValueChange = viewModel::updateApkUrl,
                    label = { Text("APK Download URL", color = Color.White.copy(alpha = 0.7f)) },
                    enabled = !state.isLoading,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                OutlinedTextField(
                    value = state.releaseNotes,
                    onValueChange = viewModel::updateReleaseNotes,
                    label = { Text("Release Notes", color = Color.White.copy(alpha = 0.7f)) },
                    enabled = !state.isLoading,
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Force Update", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            "If enabled, users cannot dismiss the update prompt",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.forceUpdate,
                        onCheckedChange = viewModel::updateForceUpdate,
                        enabled = !state.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.publishUpdateConfig() },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5EEAD4),
                        contentColor = Color(0xFF1A1344)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF1A1344)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Publishing...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Publish Update Config", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF5EEAD4),
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedLabelColor = Color(0xFF5EEAD4),
    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
    cursorColor = Color(0xFF5EEAD4),
    focusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.3f),
    unfocusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.2f)
)
