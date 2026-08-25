package com.kreadivegalaxy.kuzmixos

import android.app.ActivityManager
import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import kotlin.math.atan2
import kotlin.math.sqrt

enum class LightLevelCategory {
    DARK,
    DIM,
    INDOOR,
    BRIGHT,
    DIRECT_SUNLIGHT
}

enum class OrientationCategory {
    FLAT,
    UPRIGHT,
    TILT_LEFT,
    TILT_RIGHT
}

data class AeroGlassSensorState(
    val ambientLux: Float = 150f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val lightCategory: LightLevelCategory = LightLevelCategory.INDOOR,
    val orientationCategory: OrientationCategory = OrientationCategory.FLAT,
    val glassAlpha: Float = 0.10f,
    val borderAlpha: Float = 0.30f,
    val blurRadius: Float = 50f,
    val specularX: Float = 0.5f,
    val specularY: Float = 0.2f,
)

object HardwareDetection {
    fun detectTier(context: Context): HardwareTier {
        val cores = Runtime.getRuntime().availableProcessors()

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)

        return if ((cores >= 6) && (totalRamGb >= 5.5)) {
            HardwareTier.TITAN
        } else {
            HardwareTier.NOVA
        }
    }

    fun getDeviceModel(): String {
        return Build.MODEL
    }

    fun computeAeroGlassOpacity(lux: Float, pitch: Float, roll: Float): AeroGlassSensorState {
        val lightCat = when {
            lux < 20f -> LightLevelCategory.DARK
            lux < 150f -> LightLevelCategory.DIM
            lux < 1000f -> LightLevelCategory.INDOOR
            lux < 5000f -> LightLevelCategory.BRIGHT
            else -> LightLevelCategory.DIRECT_SUNLIGHT
        }

        val orientCat = when {
            pitch in -20f..20f && roll in -20f..20f -> OrientationCategory.FLAT
            pitch < -35f || pitch > 35f -> OrientationCategory.UPRIGHT
            roll < -25f -> OrientationCategory.TILT_LEFT
            roll > 25f -> OrientationCategory.TILT_RIGHT
            else -> OrientationCategory.FLAT
        }

        val (baseGlassAlpha, baseBorderAlpha, blur) = when (lightCat) {
            LightLevelCategory.DARK -> Triple(0.06f, 0.18f, 35f)
            LightLevelCategory.DIM -> Triple(0.08f, 0.22f, 45f)
            LightLevelCategory.INDOOR -> Triple(0.12f, 0.30f, 50f)
            LightLevelCategory.BRIGHT -> Triple(0.18f, 0.45f, 60f)
            LightLevelCategory.DIRECT_SUNLIGHT -> Triple(0.26f, 0.65f, 75f)
        }

        val tiltOffset = (kotlin.math.abs(pitch) + kotlin.math.abs(roll)) / 180f * 0.03f
        val finalGlassAlpha = (baseGlassAlpha + tiltOffset).coerceIn(0.04f, 0.35f)
        val finalBorderAlpha = (baseBorderAlpha + tiltOffset * 1.5f).coerceIn(0.12f, 0.80f)

        val normSpecX = ((roll.coerceIn(-60f, 60f) + 60f) / 120f).coerceIn(0.1f, 0.9f)
        val normSpecY = ((pitch.coerceIn(-60f, 60f) + 60f) / 120f).coerceIn(0.1f, 0.9f)

        return AeroGlassSensorState(
            ambientLux = lux,
            pitch = pitch,
            roll = roll,
            lightCategory = lightCat,
            orientationCategory = orientCat,
            glassAlpha = finalGlassAlpha,
            borderAlpha = finalBorderAlpha,
            blurRadius = blur,
            specularX = normSpecX,
            specularY = normSpecY
        )
    }
}
