package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreadivegalaxy.kuzmixos.ui.theme.MyApplicationTheme
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class KuzmixVoiceSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    KuzmixWakeWordSettingsScreen(
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuzmixWakeWordSettingsScreen(
    context: Context = LocalContext.current,
    onClose: () -> Unit = {}
) {
    val prefs = remember { context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE) }

    // State bindings
    var sensitivity by remember { mutableStateOf(prefs.getFloat("wake_word_sensitivity", 0.5f)) }
    var customWakeWord by remember { mutableStateOf(prefs.getString("custom_wake_word", "Hey Kuzmix") ?: "Hey Kuzmix") }
    var strictFiltering by remember { mutableStateOf(prefs.getBoolean("wake_word_strict_mode", false)) }
    var allowDirectCommands by remember { mutableStateOf(prefs.getBoolean("wake_word_direct_commands", true)) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showOnboardingTutorial by remember { mutableStateOf(false) }

    if (showOnboardingTutorial) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showOnboardingTutorial = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            KuzmixWakeWordOnboarding(
                onDismiss = { showOnboardingTutorial = false }
            )
        }
    }

    if (showCalibrationDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showCalibrationDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            KuzmixVoiceCalibrationWizard(
                context = context,
                onComplete = { showCalibrationDialog = false },
                onDismiss = { showCalibrationDialog = false }
            )
        }
    }

    // Testing sandbox state
    var isTestingWakeWord by remember { mutableStateOf(false) }
    var testRecognizedText by remember { mutableStateOf("") }
    var testResultStatus by remember { mutableStateOf("") }
    var testResultColor by remember { mutableStateOf(Color.Gray) }
    val scope = rememberCoroutineScope()

    // Save changes helper
    fun saveSettings() {
        prefs.edit()
            .putFloat("wake_word_sensitivity", sensitivity)
            .putString("custom_wake_word", customWakeWord)
            .putBoolean("wake_word_strict_mode", strictFiltering)
            .putBoolean("wake_word_direct_commands", allowDirectCommands)
            .apply()

        // Notify service of settings change if service is running
        try {
            val intent = Intent(context, KuzmixVoiceService::class.java).apply {
                putExtra("EXTRA_RELOAD_SETTINGS", true)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // TOP NAVIGATION BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KuzmixYellow.copy(alpha = 0.15f))
                            .border(0.5.dp, KuzmixYellow.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Wake Word Settings",
                            tint = KuzmixYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Voice & Wake Settings",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "'Hey Kuzmix' Sensitivity & Calibration",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            // CURRENT SENSITIVITY MODE BANNER
            val sensitivityModeLabel = when {
                sensitivity <= 0.25f || strictFiltering -> "🔴 Strict / Low False Positives"
                sensitivity >= 0.75f -> "🟢 High Responsiveness"
                else -> "🟡 Balanced (Default)"
            }

            val sensitivityModeDesc = when {
                sensitivity <= 0.25f || strictFiltering -> "Filters ambient talk and TV noise. Requires exact phrase pronunciation."
                sensitivity >= 0.75f -> "Triggers easily, suitable for quiet environments."
                else -> "Optimized balance between responsiveness and false positive rejection."
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, KuzmixYellow.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Trigger Profile",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = sensitivityModeLabel,
                            color = KuzmixYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = sensitivityModeDesc,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }

            // 1. DETECTION SENSITIVITY SLIDER & PRESETS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "WAKE WORD DETECTION SENSITIVITY",
                        color = KuzmixYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sensitivity Level: ${(sensitivity * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Slider(
                        value = sensitivity,
                        onValueChange = {
                            sensitivity = it
                            saveSettings()
                        },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = KuzmixYellow,
                            activeTrackColor = KuzmixYellow,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    // Quick Preset Buttons
                    Text(
                        text = "Quick Sensitivity Presets:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            0.1f to "🛡️ Strict",
                            0.5f to "⚖️ Balanced",
                            0.9f to "⚡ Sensitive"
                        )
                        presets.forEach { (valPreset, labelPreset) ->
                            val isSelected = kotlin.math.abs(sensitivity - valPreset) < 0.15f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) KuzmixYellow.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                                    .border(if (isSelected) 1.dp else 0.5.dp, if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        sensitivity = valPreset
                                        saveSettings()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labelPreset,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 2. CUSTOM WAKE WORD PHRASE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "WAKE WORD PHRASE CUSTOMIZATION",
                        color = KuzmixYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Set your preferred trigger phrase. Kuzmix OS will activate when this phrase is detected.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = customWakeWord,
                        onValueChange = {
                            customWakeWord = it
                            saveSettings()
                        },
                        label = { Text("Custom Wake Phrase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KuzmixYellow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = KuzmixYellow,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Hey Kuzmix", "Hey Cosmic", "Hey Computer").forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        customWakeWord = suggestion
                                        saveSettings()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(suggestion, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 3. FALSE POSITIVE PREVENTION CONTROLS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "FALSE POSITIVE PROTECTION",
                        color = KuzmixYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Strict Keyword Filter Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Strict Keyword Filter",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ignores casual background speech unless explicit wake phrase is spoken.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = strictFiltering,
                            onCheckedChange = {
                                strictFiltering = it
                                saveSettings()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KuzmixYellow,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Direct Commands Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Allow Direct Action Words",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Execute immediate commands (e.g. 'torch on') without requiring wake word first.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = allowDirectCommands,
                            onCheckedChange = {
                                allowDirectCommands = it
                                saveSettings()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KuzmixYellow,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }

            // 4. LIVE TEST SANDBOX CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, KuzmixYellow.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE SENSITIVITY TEST SANDBOX",
                            color = KuzmixYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isTestingWakeWord) Color.Red else Color.Green, CircleShape)
                        )
                    }

                    Text(
                        text = "Speak into your microphone to verify if your current sensitivity settings trigger correctly or reject false positives.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (testRecognizedText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .border(0.5.dp, testResultColor, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Recognized Speech: \"$testRecognizedText\"",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = testResultStatus,
                                    color = testResultColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (!isTestingWakeWord) {
                                isTestingWakeWord = true
                                testRecognizedText = "Listening for test phrase..."
                                testResultStatus = "Say 'Hey Kuzmix' or test words..."
                                testResultColor = KuzmixYellow

                                scope.launch {
                                    startSandboxSpeechTest(context, customWakeWord, sensitivity, strictFiltering) { text, passed, statusMsg ->
                                        testRecognizedText = text
                                        testResultStatus = statusMsg
                                        testResultColor = if (passed) Color.Green else Color(0xFFFF5252)
                                        isTestingWakeWord = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isTestingWakeWord) Color.Red else KuzmixYellow),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isTestingWakeWord) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Test Mic",
                            tint = Color.Black
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isTestingWakeWord) "Listening... (Speak Now)" else "🎤 Test Trigger Sensitivity",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 5. STEP-BY-STEP VOICE CALIBRATION WIZARD & BIOMETRICS
            val isCalibrated = remember { prefs.getBoolean("vocal_biometric_enrolled", false) }
            val clarityScore = remember { prefs.getInt("vocal_clarity_score", 96) }
            val ambientDb = remember { prefs.getInt("vocal_ambient_db", -48) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, KuzmixYellow.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VOICE CALIBRATION WIZARD",
                            color = KuzmixYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCalibrated) KuzmixYellow.copy(alpha = 0.2f) else Color(0xFFFF9900).copy(alpha = 0.2f))
                                .border(0.5.dp, if (isCalibrated) KuzmixYellow else Color(0xFFFF9900), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isCalibrated) "CALIBRATED ($clarityScore%)" else "UNCALIBRATED",
                                color = if (isCalibrated) KuzmixYellow else Color(0xFFFF9900),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Guide yourself through a 5-step recording wizard to calibrate room acoustics, baseline speech, cadence variations, and custom wake word verification.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = { showCalibrationDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isCalibrated) "🎙️ Re-run Step-by-Step Voice Calibration" else "🎙️ Start Step-by-Step Voice Calibration Wizard",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showOnboardingTutorial = true },
                        colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Hearing, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "📖 Play 'Hey Kuzmix' Tutorial Onboarding",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (isCalibrated) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Acoustic Floor: $ambientDb dB", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("Clarity Rating: $clarityScore%", color = KuzmixYellow, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Text(
                        text = "Quick Vocal Biometrics Key Status:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )

                    VocalBiometricsSetupCard(context = context)
                }
            }

            // 6. RESET DEFAULTS BUTTON
            OutlinedButton(
                onClick = {
                    sensitivity = 0.5f
                    customWakeWord = "Hey Kuzmix"
                    strictFiltering = false
                    allowDirectCommands = true
                    saveSettings()
                    Toast.makeText(context, "Voice settings reset to defaults", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f)),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Restore Default Voice Settings", fontSize = 13.sp)
            }
        }
    }
}

private fun startSandboxSpeechTest(
    context: Context,
    targetWakeWord: String,
    sensitivity: Float,
    strictMode: Boolean,
    onResult: (String, Boolean, String) -> Unit
) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        onResult("Speech recognizer unavailable", false, "Speech Service Not Available")
        return
    }

    try {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                onResult("Speech error code $error", false, "No speech detected or mic timeout")
                try { recognizer.destroy() } catch (e: Exception) {}
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""
                val lowerSpoken = spokenText.lowercase().trim()
                val lowerTarget = targetWakeWord.lowercase().trim()

                val hasMatch = if (strictMode || sensitivity <= 0.25f) {
                    lowerSpoken.contains(lowerTarget) || lowerSpoken.contains("hey kuzmix")
                } else {
                    lowerSpoken.contains(lowerTarget) || lowerSpoken.contains("kuzmix") || lowerSpoken.contains("cosmic") || lowerSpoken.contains("kc")
                }

                val status = if (hasMatch) {
                    "SUCCESS: Triggered wake word! Confidence matches settings."
                } else {
                    "REJECTED: Filtered out as false positive or ambient noise."
                }

                onResult(spokenText.ifBlank { "(silence)" }, hasMatch, status)
                try { recognizer.destroy() } catch (e: Exception) {}
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
    } catch (e: Exception) {
        onResult("Error: ${e.message}", false, "Failed to initialize test mic")
    }
}
