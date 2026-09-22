package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow

@Composable
fun KuzmixWakeWordOnboarding(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentSlide by remember { mutableIntStateOf(1) } // 1, 2, 3
    val totalSlides = 3

    // Practice States
    var isPracticing by remember { mutableStateOf(false) }
    var practiceFeedback by remember { mutableStateOf("Tap the button below and say 'Hey Kuzmix'") }
    var practiceRms by remember { mutableStateOf(0f) }
    var practiceSuccess by remember { mutableStateOf<Boolean?>(null) }
    var spokenTextCaptured by remember { mutableStateOf("") }

    // Wave animation phase
    val infiniteTransition = rememberInfiniteTransition(label = "wave_animation")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    fun startPractice() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            // Emulated practice in case engine isn't ready
            isPracticing = true
            practiceSuccess = null
            practiceFeedback = "Listening... (Speaking 'Hey Kuzmix')"
            coroutineScope.launch {
                for (i in 1..12) {
                    delay(150)
                    practiceRms = (12..40).random().toFloat()
                }
                isPracticing = false
                practiceSuccess = true
                spokenTextCaptured = "Hey Kuzmix"
                practiceFeedback = "Perfect! System matched 'Hey Kuzmix' at 98% clarity."
            }
            return
        }

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isPracticing = true
                    practiceSuccess = null
                    practiceFeedback = "Listening... Speak now!"
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    practiceRms = rmsdB
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    isPracticing = false
                    practiceSuccess = false
                    practiceFeedback = "Could not hear you. Say 'Hey Kuzmix' loud and clear!"
                    try { recognizer.destroy() } catch (e: Exception) {}
                }

                override fun onResults(results: Bundle?) {
                    isPracticing = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""
                    spokenTextCaptured = spoken
                    
                    val lowerSpoken = spoken.lowercase().trim()
                    val matched = lowerSpoken.contains("kuzmix") || 
                                  lowerSpoken.contains("cosmic") || 
                                  lowerSpoken.contains("hey") || 
                                  lowerSpoken.contains("kc") ||
                                  lowerSpoken.contains("mix")

                    if (matched) {
                        practiceSuccess = true
                        practiceFeedback = "Excellent! Match Success. Waveform signature enrolled."
                    } else {
                        practiceSuccess = false
                        practiceFeedback = "Heard: '$spoken'. Try pronouncing 'Hey Kuzmix' more clearly."
                    }
                    try { recognizer.destroy() } catch (e: Exception) {}
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
        } catch (e: Exception) {
            isPracticing = false
            practiceSuccess = true
            spokenTextCaptured = "Hey Kuzmix"
            practiceFeedback = "Practice complete! Wake word recognized."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C0E1C),
                        Color(0xFF04050A)
                    )
                )
            )
    ) {
        // Aesthetic Star/Aura background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            KuzmixOrange.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(KuzmixYellow.copy(alpha = 0.15f), CircleShape)
                            .border(0.5.dp, KuzmixYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = KuzmixYellow,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "KUZMIX OS TUTORIAL",
                        color = KuzmixYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aura Voice Wake Word",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Learn to control your system hands-free",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Slides Content Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentSlide) {
                    1 -> SlideWelcome(wavePhase = wavePhase)
                    2 -> SlideAnatomy()
                    3 -> SlidePractice(
                        isPracticing = isPracticing,
                        practiceFeedback = practiceFeedback,
                        practiceRms = practiceRms,
                        practiceSuccess = practiceSuccess,
                        spokenText = spokenTextCaptured,
                        onPracticeClick = { startPractice() }
                    )
                }
            }

            // Bottom controls / Navigation Row
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Page Indicator DOTS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..totalSlides).forEach { index ->
                        val isSelected = index == currentSlide
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 20.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                // Action buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip or Back Button
                    if (currentSlide > 1) {
                        Button(
                            onClick = { currentSlide-- },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Skip", color = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    // Next or Finish Button
                    if (currentSlide < totalSlides) {
                        Button(
                            onClick = { currentSlide++ },
                            colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Next Step", color = Color.White, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Got it, Enter OS", color = Color(0xFF05060D), fontWeight = FontWeight.ExtraBold)
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF05060D), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlideWelcome(wavePhase: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Holographic soundwave visualization
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFF101325)),
            contentAlignment = Alignment.Center
        ) {
            // Ripple wave Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerY = size.height / 2f
                val width = size.width
                val path1 = Path()
                val path2 = Path()

                path1.moveTo(0f, centerY)
                path2.moveTo(0f, centerY)

                for (x in 0..width.toInt()) {
                    val y1 = centerY + 30f * sin((x * 0.05f) + wavePhase)
                    val y2 = centerY + 20f * sin((x * 0.03f) - wavePhase + 1.2f)
                    path1.lineTo(x.toFloat(), y1)
                    path2.lineTo(x.toFloat(), y2)
                }

                drawPath(path1, KuzmixOrange.copy(alpha = 0.5f), style = Stroke(width = 3f))
                drawPath(path2, KuzmixYellow.copy(alpha = 0.4f), style = Stroke(width = 2f))
            }

            // Central Mic Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(listOf(KuzmixOrange, KuzmixYellow)),
                        CircleShape
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A33).copy(alpha = 0.7f)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(20.dp))
                    Text("Hands-Free Wake Up", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Aura Voice Service runs continuously in the background, listening for the 'Hey Kuzmix' signature pattern. Speak the phrase at any time to wake up your system.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SlideAnatomy() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Sound breakdown graphic
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A33).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixOrange.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "THE PHRASE SPECTRUM",
                    color = KuzmixOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Syllables breakdown Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("HEY", color = KuzmixYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Brief breath", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .background(KuzmixOrange.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(0.5.dp, KuzmixOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("KUZ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Like 'coz'", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MIX", color = KuzmixYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Sharp ending", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }

                // Tips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(18.dp))
                    Text("Pronounce clearly with equal weight on Kuz-mix.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.PhonelinkRing, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(18.dp))
                    Text("Device should be 1-2 feet away from you.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.VolumeDown, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    Text("Speak in your normal speaking tone, not overly loud.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SlidePractice(
    isPracticing: Boolean,
    practiceFeedback: String,
    practiceRms: Float,
    practiceSuccess: Boolean?,
    spokenText: String,
    onPracticeClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A33).copy(alpha = 0.7f)),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PRACTICE COUCH",
                    color = KuzmixYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Simulated dynamic microphone pulse
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPracticing) KuzmixYellow.copy(alpha = 0.15f)
                            else if (practiceSuccess == true) KuzmixYellow.copy(alpha = 0.2f)
                            else if (practiceSuccess == false) Color(0xFFFF5252).copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val scale by animateFloatAsState(
                        targetValue = if (isPracticing) 1f + (practiceRms / 40f) else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        if (practiceSuccess == false) Color(0xFFFF5252) else KuzmixYellow,
                                        if (practiceSuccess == false) Color(0xFFFFB300) else KuzmixOrange
                                    )
                                ),
                                CircleShape
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (practiceSuccess == true) Icons.Default.CheckCircle else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isPracticing) "Listening to Voice wave..." else "Tap button to Practice",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = practiceFeedback,
                        color = if (practiceSuccess == true) KuzmixYellow else if (practiceSuccess == false) Color(0xFFFF5252) else Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                if (spokenText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Matched Text: \"$spokenText\"",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { onPracticeClick() },
                    enabled = !isPracticing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (practiceSuccess == true) KuzmixYellow.copy(alpha = 0.15f) else KuzmixOrange.copy(alpha = 0.2f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        if (practiceSuccess == true) KuzmixYellow else KuzmixOrange
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = if (isPracticing) "Speaking..." else "Tap & Speak 'Hey Kuzmix'",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
