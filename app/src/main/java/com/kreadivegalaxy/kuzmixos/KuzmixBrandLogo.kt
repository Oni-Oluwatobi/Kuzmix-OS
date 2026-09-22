package com.kreadivegalaxy.kuzmixos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

/**
 * Pure Jetpack Compose brand logo for Kuzmix OS.
 * Completely immune to resource decoding and resolution errors.
 */
@Composable
fun KuzmixBrandLogo(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFA000), Color(0xFFFF6F00)),
                    start = Offset.Zero,
                    end = Offset.Infinite
                ),
                shape = CircleShape
            )
            .border(0.75.dp, Color(0x60FFE082), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.68f)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                // Vertical bar of K
                moveTo(w * 0.18f, h * 0.12f)
                lineTo(w * 0.18f, h * 0.88f)
                lineTo(w * 0.36f, h * 0.88f)
                lineTo(w * 0.36f, h * 0.54f)
                // Bottom diagonal leg
                lineTo(w * 0.68f, h * 0.88f)
                lineTo(w * 0.88f, h * 0.88f)
                // Center junction
                lineTo(w * 0.52f, h * 0.48f)
                // Top diagonal arm
                lineTo(w * 0.84f, h * 0.12f)
                lineTo(w * 0.65f, h * 0.12f)
                // Back to vertical bar junction
                lineTo(w * 0.36f, h * 0.44f)
                lineTo(w * 0.36f, h * 0.12f)
                close()
            }
            drawPath(path = path, color = tint)
        }
    }
}
