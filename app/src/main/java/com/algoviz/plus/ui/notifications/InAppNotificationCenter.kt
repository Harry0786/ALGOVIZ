package com.algoviz.plus.ui.notifications

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Small app-wide in-app notification event bus for foreground banners.
 */
data class InAppNotification(
    val title: String,
    val message: String,
    val type: InAppNotificationType = InAppNotificationType.Info,
    val roomId: String? = null,
    val groupKey: String? = null,
    val count: Int = 1,
    val dedupeKey: String = "$title:$message"
)

enum class InAppNotificationType {
    Info,
    Success,
    Error,
    Chat,
    Update
}

object InAppNotificationCenter {
    private val _events = MutableSharedFlow<InAppNotification>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<InAppNotification> = _events.asSharedFlow()

    fun post(notification: InAppNotification) {
        _events.tryEmit(notification)
    }
}

@Composable
fun TopInAppNotificationBar(
    notification: InAppNotification?,
    onClick: (() -> Unit)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = notification?.type?.palette() ?: NotificationPalette.info()
    var dragOffsetPx by remember(notification?.dedupeKey) { mutableFloatStateOf(0f) }
    var measuredWidthPx by remember(notification?.dedupeKey) { mutableFloatStateOf(0f) }

    LaunchedEffect(notification?.dedupeKey) {
        if (notification != null) {
            vibrateForNotification(context)
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = palette.container,
            tonalElevation = 10.dp,
            shadowElevation = 14.dp,
            border = BorderStroke(1.2.dp, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { measuredWidthPx = it.width.toFloat() }
                .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
                .pointerInput(notification?.dedupeKey) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            dragOffsetPx += dragAmount
                        },
                        onDragEnd = {
                            val dismissThreshold = if (measuredWidthPx > 0f) measuredWidthPx * 0.25f else 180f
                            if (abs(dragOffsetPx) >= dismissThreshold) {
                                onDismiss()
                            }
                            dragOffsetPx = 0f
                        },
                        onDragCancel = {
                            dragOffsetPx = 0f
                        }
                    )
                }
                .then(
                    if (notification?.roomId != null && onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = palette.icon,
                        contentDescription = null,
                        tint = palette.iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = notification?.title.orEmpty(),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if ((notification?.count ?: 1) > 1) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                            ) {
                                Text(
                                    text = "x${notification?.count}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = notification?.message.orEmpty(),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (notification?.roomId != null && onClick != null) {
                        Text(
                            text = "Tap to open • swipe to dismiss",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class NotificationPalette(
    val container: Color,
    val border: Color,
    val iconBackground: Color,
    val iconTint: Color,
    val icon: ImageVector
) {
    companion object {
        fun info() = NotificationPalette(
            container = Color(0xFF24165C).copy(alpha = 0.97f),
            border = Color(0xFF8B5CF6).copy(alpha = 0.4f),
            iconBackground = Color(0xFF31206F),
            iconTint = Color(0xFFC4B5FD),
            icon = Icons.Default.NotificationsActive
        )

        fun success() = NotificationPalette(
            container = Color(0xFF123B31).copy(alpha = 0.97f),
            border = Color(0xFF34D399).copy(alpha = 0.45f),
            iconBackground = Color(0xFF0F2E28),
            iconTint = Color(0xFF6EE7B7),
            icon = Icons.Default.CheckCircle
        )

        fun error() = NotificationPalette(
            container = Color(0xFF4A1625).copy(alpha = 0.97f),
            border = Color(0xFFFB7185).copy(alpha = 0.45f),
            iconBackground = Color(0xFF3A1020),
            iconTint = Color(0xFFFDA4AF),
            icon = Icons.Default.ErrorOutline
        )

        fun chat() = NotificationPalette(
            container = Color(0xFF162B52).copy(alpha = 0.97f),
            border = Color(0xFF60A5FA).copy(alpha = 0.45f),
            iconBackground = Color(0xFF132344),
            iconTint = Color(0xFF93C5FD),
            icon = Icons.Default.Forum
        )

        fun update() = NotificationPalette(
            container = Color(0xFF453214).copy(alpha = 0.97f),
            border = Color(0xFFFBBF24).copy(alpha = 0.45f),
            iconBackground = Color(0xFF36270F),
            iconTint = Color(0xFFFDE68A),
            icon = Icons.Default.SystemUpdate
        )
    }
}

private fun InAppNotificationType.palette(): NotificationPalette {
    return when (this) {
        InAppNotificationType.Info -> NotificationPalette.info()
        InAppNotificationType.Success -> NotificationPalette.success()
        InAppNotificationType.Error -> NotificationPalette.error()
        InAppNotificationType.Chat -> NotificationPalette.chat()
        InAppNotificationType.Update -> NotificationPalette.update()
    }
}

private fun vibrateForNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(VibratorManager::class.java)
        val vibrator = vibratorManager?.defaultVibrator ?: return
        if (!vibrator.hasVibrator()) return
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 90, 40, 110), -1)
        vibrator.vibrate(effect)
        return
    }

    @Suppress("DEPRECATION")
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (!vibrator.hasVibrator()) return
    @Suppress("DEPRECATION")
    vibrator.vibrate(160)
}
