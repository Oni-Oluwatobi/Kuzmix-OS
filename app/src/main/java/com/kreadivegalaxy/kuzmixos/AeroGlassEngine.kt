/* 
 * KC Studio - Professional Mobile IDE 
 * Developed by: ONI OLUWATOBI 
 * Copyright 2026 THE KREADIVE GALAXY 
 */
package com.kreadivegalaxy.kuzmixos

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.asComposeRenderEffect
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkSurface

@androidx.annotation.RequiresApi(Build.VERSION_CODES.S)
object RenderEffectCompat {
    fun createBlur(radius: Float): androidx.compose.ui.graphics.RenderEffect {
        return android.graphics.RenderEffect.createBlurEffect(
            radius, radius, android.graphics.Shader.TileMode.MIRROR
        ).asComposeRenderEffect()
    }
}

object AeroGlassEngine {
    
    fun Modifier.liquidGlassBackdrop(
        hardwareTier: HardwareTier,
        cornerRadius: Float = 32f,
        sensorState: AeroGlassSensorState? = null
    ): Modifier = composed {
        val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
        val glassAlpha = sensorState?.glassAlpha ?: (if (isLight) 0.12f else 0.06f)
        val borderAlpha = sensorState?.borderAlpha ?: 0.25f
        val blurRadius = sensorState?.blurRadius ?: 50f
        val specX = sensorState?.specularX ?: 0.5f
        val specY = sensorState?.specularY ?: 0.2f

        val borderBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (borderAlpha * 1.2f).coerceAtMost(0.9f)),
                Color.White.copy(alpha = (borderAlpha * 0.4f).coerceAtLeast(0.08f))
            ),
            start = androidx.compose.ui.geometry.Offset(specX * 300f, specY * 300f),
            end = androidx.compose.ui.geometry.Offset((1f - specX) * 300f, (1f - specY) * 300f)
        )

        this.then(
            Modifier
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(cornerRadius.dp)
                }
                .background(
                    if (isLight) Color.White.copy(alpha = 0.35f) else DarkBackground,
                    RoundedCornerShape(cornerRadius.dp)
                )
                .border(
                    0.5.dp,
                    borderBrush,
                    RoundedCornerShape(cornerRadius.dp)
                )
        )
    }
}