package com.algoviz.plus.features.auth.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1F2126),
                            Color(0xFF3B3E45)
                        )
                    )
                )
                .alpha(if (enabled && !isLoading) 1f else 0.45f)
                .clickable(
                    enabled = enabled && !isLoading,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        enabled = enabled,
        border = BorderStroke(1.dp, Color(0xFFC9CBD0)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFF1F1F2),
            contentColor = Color(0xFF111111)
        ),
        shape = RoundedCornerShape(34.dp)
    ) {
        Icon(
            painter = painterResource(id = com.algoviz.plus.features.auth.R.drawable.ic_google),
            contentDescription = "Google",
            modifier = Modifier.size(21.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Continue with Google",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
