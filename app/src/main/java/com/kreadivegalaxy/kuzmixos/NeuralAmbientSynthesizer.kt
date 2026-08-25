package com.kreadivegalaxy.kuzmixos

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.concurrent.thread
import kotlin.math.sin

enum class AmbientSoundMode(val label: String, val frequency: Double) {
    ALPHA_FOCUS("Alpha Focus (432 Hz)", 432.0),
    THETA_CALM("Theta Waves (216 Hz)", 216.0),
    PINK_NOISE("Deep Pink Noise", 120.0),
    NEURAL_RAIN("Rain Simulator", 80.0)
}

class SynthesizerEngine {
    private var audioTrack: AudioTrack? = null
    @Volatile private var isPlaying = false
    private var soundMode = AmbientSoundMode.ALPHA_FOCUS
    private var currentVolume = 0.5f

    fun start(mode: AmbientSoundMode, volume: Float = 0.5f) {
        if (isPlaying) stop()
        soundMode = mode
        currentVolume = volume
        isPlaying = true

        thread(name = "KuzmixAudioSynthThread") {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, 2048)

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                val buffer = ShortArray(1024)
                var phase = 0.0
                var randomState = 0x12345678

                while (isPlaying && audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    val baseFreq = soundMode.frequency
                    for (i in buffer.indices) {
                        val sample: Double = when (soundMode) {
                            AmbientSoundMode.ALPHA_FOCUS, AmbientSoundMode.THETA_CALM -> {
                                val binauralLfo = sin(2.0 * Math.PI * 4.0 * phase / sampleRate) * 0.15
                                sin(2.0 * Math.PI * baseFreq * phase / sampleRate) * (0.85 + binauralLfo)
                            }
                            AmbientSoundMode.PINK_NOISE -> {
                                randomState = randomState * 1664525 + 1013904223
                                val white = (randomState shr 16).toShort() / 32768.0
                                (sin(2.0 * Math.PI * 60.0 * phase / sampleRate) * 0.4) + (white * 0.4)
                            }
                            AmbientSoundMode.NEURAL_RAIN -> {
                                randomState = randomState * 1103515245 + 12345
                                val drop = if ((randomState and 0x3FF) == 0) 0.8 else 0.0
                                val rainNoise = ((randomState shr 16).toShort() / 32768.0) * 0.3
                                (rainNoise + drop).coerceIn(-1.0, 1.0)
                            }
                        }
                        buffer[i] = (sample * 32767.0 * currentVolume).toInt().coerceIn(-32768, 32767).toShort()
                        phase += 1.0
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.e("SynthesizerEngine", "Audio synthesis error: ${e.message}", e)
            } finally {
                stopInternal()
            }
        }
    }

    fun setVolume(vol: Float) {
        currentVolume = vol.coerceIn(0f, 1f)
        try {
            audioTrack?.setVolume(currentVolume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        isPlaying = false
        stopInternal()
    }

    private fun stopInternal() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioTrack = null
        }
    }
}

@Composable
fun NeuralAmbientSynthCard(
    modifier: Modifier = Modifier
) {
    val synthEngine = remember { SynthesizerEngine() }
    var isPlaying by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(AmbientSoundMode.ALPHA_FOCUS) }
    var volume by remember { mutableFloatStateOf(0.5f) }

    DisposableEffect(Unit) {
        onDispose {
            synthEngine.stop()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
            .border(1.dp, KuzmixYellow.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(KuzmixYellow.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = "Synth",
                            tint = KuzmixYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NEURAL SOUNDSYNTH",
                            color = KuzmixYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isPlaying) "Playing: ${selectedMode.label}" else "Ambient Audio Generator",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color(0xFFFF5252) else KuzmixYellow)
                        .clickable {
                            isPlaying = !isPlaying
                            if (isPlaying) {
                                synthEngine.start(selectedMode, volume)
                            } else {
                                synthEngine.stop()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Audio",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isPlaying) {
                Spacer(modifier = Modifier.height(10.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    val waveColor = KuzmixYellow.copy(alpha = 0.8f)

                    val points = 30
                    val step = width / points
                    for (i in 0 until points) {
                        val x1 = i * step
                        val x2 = (i + 1) * step
                        val rad1 = Math.toRadians((x1 + waveOffset).toDouble())
                        val rad2 = Math.toRadians((x2 + waveOffset).toDouble())
                        val y1 = centerY + (sin(rad1) * (height / 3f)).toFloat()
                        val y2 = centerY + (sin(rad2) * (height / 3f)).toFloat()
                        drawLine(
                            color = waveColor,
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 3f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sound Mode Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AmbientSoundMode.entries.forEach { mode ->
                    val selected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) KuzmixYellow.copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .border(
                                0.5.dp,
                                if (selected) KuzmixYellow else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedMode = mode
                                if (isPlaying) {
                                    synthEngine.start(mode, volume)
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name.replace("_", " "),
                            color = if (selected) KuzmixYellow else Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Volume Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        synthEngine.setVolume(it)
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = KuzmixYellow,
                        activeTrackColor = KuzmixYellow,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}
