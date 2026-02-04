package com.algoviz.plus.features.auth.presentation.components

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

@Composable
fun AppLogo(
    @DrawableRes logoRes: Int,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val painter = remember(logoRes) {
        val drawable = ContextCompat.getDrawable(context, logoRes)
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
            ?: android.graphics.Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight,
                android.graphics.Bitmap.Config.ARGB_8888
            ).also { bmp ->
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        BitmapPainter(bitmap.asImageBitmap())
    }
    
    Image(
        painter = painter,
        contentDescription = "AlgoViz Logo",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.25f))
    )
}
