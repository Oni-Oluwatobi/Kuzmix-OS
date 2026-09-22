package com.kreadivegalaxy.kuzmixos

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

/**
 * Clean native shimmer placeholder for ThemePack cards while loading.
 * Features a dark gradient background with a centered 4-geometric-shape glyph cluster
 * (rounded square, circle, diamond/heart, triangle) in subtle elevated gray.
 * Pure native rendering with zero ads or SDK delays.
 */
@Composable
fun ThemeCardPlaceholder(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(
        modifier = modifier.width(160.dp)
    ) {
        // Card Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF18181B),
                            Color(0xFF222226),
                            Color(0xFF18181B)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Centered 4-geometric-shape cluster (Square, Circle, Diamond, Triangle)
            Canvas(
                modifier = Modifier
                    .size(56.dp)
            ) {
                val glyphColor = Color(0xFF3A3A3C).copy(alpha = alphaAnim)
                val half = size.width / 2f
                val shapeSize = size.width * 0.32f

                // Top-Left: Rounded Square
                drawRoundRect(
                    color = glyphColor,
                    topLeft = Offset(half - shapeSize - 4.dp.toPx(), half - shapeSize - 4.dp.toPx()),
                    size = Size(shapeSize, shapeSize),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Top-Right: Circle
                drawCircle(
                    color = glyphColor,
                    radius = shapeSize / 2f,
                    center = Offset(half + 4.dp.toPx() + shapeSize / 2f, half - shapeSize / 2f - 4.dp.toPx())
                )

                // Bottom-Left: Triangle
                val trianglePath = Path().apply {
                    val left = half - shapeSize - 4.dp.toPx()
                    val top = half + 4.dp.toPx()
                    moveTo(left + shapeSize / 2f, top)
                    lineTo(left + shapeSize, top + shapeSize)
                    lineTo(left, top + shapeSize)
                    close()
                }
                drawPath(
                    path = trianglePath,
                    color = glyphColor
                )

                // Bottom-Right: Diamond
                val diamondPath = Path().apply {
                    val cx = half + 4.dp.toPx() + shapeSize / 2f
                    val cy = half + 4.dp.toPx() + shapeSize / 2f
                    moveTo(cx, cy - shapeSize / 2f)
                    lineTo(cx + shapeSize / 2f, cy)
                    lineTo(cx, cy + shapeSize / 2f)
                    lineTo(cx - shapeSize / 2f, cy)
                    close()
                }
                drawPath(
                    path = diamondPath,
                    color = glyphColor
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title Shimmer Bar
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2C2C2E).copy(alpha = alphaAnim))
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Price Shimmer Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF3A3A3C).copy(alpha = alphaAnim))
            )
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF2C2C2E).copy(alpha = alphaAnim))
            )
        }
    }
}
