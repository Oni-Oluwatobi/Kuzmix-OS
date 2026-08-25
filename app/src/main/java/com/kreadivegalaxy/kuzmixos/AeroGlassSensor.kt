package com.kreadivegalaxy.kuzmixos

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberAeroGlassSensorState(context: Context = LocalContext.current): AeroGlassSensorState {
    var rawLux by remember { mutableStateOf(150f) }
    var rawPitch by remember { mutableStateOf(0f) }
    var rawRoll by remember { mutableStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager == null) {
            onDispose { }
        } else {
            val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            var gravityVals: FloatArray? = null
            var geoVals: FloatArray? = null

            val sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return
                    when (event.sensor.type) {
                        Sensor.TYPE_LIGHT -> {
                            val newLux = event.values[0]
                            rawLux = rawLux * 0.75f + newLux * 0.25f
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            gravityVals = event.values.clone()
                            val ax = event.values[0]
                            val ay = event.values[1]
                            val az = event.values[2]

                            val pitchDeg = (kotlin.math.atan2(ay.toDouble(), kotlin.math.sqrt((ax * ax + az * az).toDouble())) * (180.0 / Math.PI)).toFloat()
                            val rollDeg = (kotlin.math.atan2(-ax.toDouble(), az.toDouble()) * (180.0 / Math.PI)).toFloat()

                            rawPitch = rawPitch * 0.8f + pitchDeg * 0.2f
                            rawRoll = rawRoll * 0.8f + rollDeg * 0.2f
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            geoVals = event.values.clone()
                            if (gravityVals != null && geoVals != null) {
                                val rMatrix = FloatArray(9)
                                val iMatrix = FloatArray(9)
                                if (SensorManager.getRotationMatrix(rMatrix, iMatrix, gravityVals, geoVals)) {
                                    val orientation = FloatArray(3)
                                    SensorManager.getOrientation(rMatrix, orientation)
                                    val pDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
                                    val rDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
                                    rawPitch = rawPitch * 0.7f + pDeg * 0.3f
                                    rawRoll = rawRoll * 0.7f + rDeg * 0.3f
                                }
                            }
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            if (lightSensor != null) {
                sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_UI)
            }
            if (accelSensor != null) {
                sensorManager.registerListener(sensorListener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            }
            if (magSensor != null) {
                sensorManager.registerListener(sensorListener, magSensor, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                sensorManager.unregisterListener(sensorListener)
            }
        }
    }

    val computedState = remember(rawLux, rawPitch, rawRoll) {
        HardwareDetection.computeAeroGlassOpacity(rawLux, rawPitch, rawRoll)
    }

    val animGlassAlpha by animateFloatAsState(
        targetValue = computedState.glassAlpha,
        animationSpec = spring(stiffness = 300f),
        label = "anim_glass_alpha"
    )
    val animBorderAlpha by animateFloatAsState(
        targetValue = computedState.borderAlpha,
        animationSpec = spring(stiffness = 300f),
        label = "anim_border_alpha"
    )
    val animSpecX by animateFloatAsState(
        targetValue = computedState.specularX,
        animationSpec = spring(stiffness = 200f),
        label = "anim_spec_x"
    )
    val animSpecY by animateFloatAsState(
        targetValue = computedState.specularY,
        animationSpec = spring(stiffness = 200f),
        label = "anim_spec_y"
    )

    return computedState.copy(
        glassAlpha = animGlassAlpha,
        borderAlpha = animBorderAlpha,
        specularX = animSpecX,
        specularY = animSpecY
    )
}
