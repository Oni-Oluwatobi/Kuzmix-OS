package com.kreadivegalaxy.kuzmixos

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class KuzmixVoiceService : Service(), RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var isListening = false
    private var isManualTrigger = false
    private var isListeningPaused = false
    private var isSpeakingTts = false
    private var activeQueryJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val audioBuffer = java.io.ByteArrayOutputStream()
    private var wakeLock: android.os.PowerManager.WakeLock? = null
    private var isMediaSessionActive = false

    private lateinit var overlayManager: KuzmixAuraOverlayManager
    private lateinit var intentEngine: KuzmixIntentEngine

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceSafely()

        overlayManager = KuzmixAuraOverlayManager(this@KuzmixVoiceService)
        overlayManager.onUserSendInput = { text ->
            isManualTrigger = true
            handleVoiceQuery(text)
        }
        overlayManager.onMicRequested = {
            isManualTrigger = true
            isListeningPaused = false
            overlayManager.activateAuraStandard()
            cancelAndRestartListening(100)
        }
        overlayManager.onStopRequested = {
            handleStopCommand()
        }

        intentEngine = KuzmixIntentEngine(this@KuzmixVoiceService)
        intentEngine.onTtsStart = {
            isSpeakingTts = true
            handler.post {
                try { speechRecognizer?.cancel() } catch (unused: Exception) {}
                isListening = false
            }
        }
        intentEngine.onTtsDone = {
            isSpeakingTts = false
            scheduleRestartListening(400)
        }

        serviceScope.launch(Dispatchers.Main) {
            handler.postDelayed({
                initSpeechRecognizer()
            }, 800)
        }

        // Keep-alive: periodically check if recognizer is alive, restart if dead
        serviceScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(5000)
                checkMediaSession()
                if (!isListeningPaused && !isSpeakingTts && !isListening) {
                    android.util.Log.d("KuzmixVoiceService", "Keep-alive: recognizer not listening, restarting...")
                    initSpeechRecognizer()
                }
            }
        }
    }

    private fun isStopCommand(text: String): Boolean {
        if (text.isBlank()) return false
        val lower = text.lowercase().trim()
        val stopPhrases = listOf(
            "stop", "be quiet", "shut up", "quiet", "cancel", "pause",
            "hush", "hold on", "stop talking", "never mind", "stop kuzmix",
            "kuzmix stop", "hey kuzmix stop", "stop speak", "stop speaking",
            "halt", "silence", "shut it", "enough"
        )
        return stopPhrases.any { lower.contains(it) }
    }

    private fun handleStopCommand() {
        activeQueryJob?.cancel()
        activeQueryJob = null
        intentEngine.stopSpeaking()

        val wasMediaPlaying = try {
            intentEngine.isMediaPlaying()
        } catch (e: Exception) { false }

        if (wasMediaPlaying) {
            try {
                intentEngine.pauseMedia()
            } catch (e: Exception) {}
            isSpeakingTts = false
            if (::overlayManager.isInitialized) {
                overlayManager.isProcessing = false
                overlayManager.addAiResponse("Music paused.")
            }
            cancelAndRestartListening(150)
            return
        }

        isSpeakingTts = false
        if (::overlayManager.isInitialized) {
            overlayManager.isProcessing = false
            overlayManager.addAiResponse("Stopped. Listening...")
            overlayManager.activateAuraStandard()
        }
        cancelAndRestartListening(150)
    }

    private fun startForegroundServiceSafely() {
        try {
            createNotificationChannel()
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
                var started = false
                val hasRecordAudio = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (hasRecordAudio) {
                    try {
                        startForeground(
                            2026,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                        )
                        started = true
                    } catch (e: Exception) {
                        android.util.Log.w("KuzmixOS", "Failed to start FGS as microphone: ${e.message}")
                    }
                }
                if (!started) {
                    try {
                        startForeground(
                            2026,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("KuzmixOS", "Failed to start FGS as dataSync fallback: ${e.message}")
                    }
                }
            } else {
                startForeground(2026, notification)
            }
            acquireWakeLock()
        } catch (e: Exception) {
            android.util.Log.e("KuzmixOS", "KuzmixVoiceService startForeground failed: ${e.message}")
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                wakeLock = powerManager.newWakeLock(
                    android.os.PowerManager.PARTIAL_WAKE_LOCK,
                    "KuzmixOS::VoiceServiceWakeLock"
                ).apply {
                    acquire(60 * 60 * 1000L) // 1 hour max, will be refreshed
                }
                android.util.Log.d("KuzmixOS", "WakeLock acquired for VoiceService")
            }
        } catch (e: Exception) {
            android.util.Log.w("KuzmixOS", "Failed to acquire WakeLock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    android.util.Log.d("KuzmixOS", "WakeLock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            android.util.Log.w("KuzmixOS", "Failed to release WakeLock: ${e.message}")
        }
    }

    private fun checkMediaSession() {
        try {
            // Simple approach: check if any audio is playing via AudioManager
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            isMediaSessionActive = audioManager.isMusicActive
        } catch (e: Exception) {
            isMediaSessionActive = false
        }
    }

    private fun initSpeechRecognizer() {
        handler.post {
            try {
                try {
                    speechRecognizer?.cancel()
                    speechRecognizer?.setRecognitionListener(null)
                    speechRecognizer?.destroy()
                } catch (e: Exception) {
                    android.util.Log.w("KuzmixVoiceService", "Error destroying old recognizer: ${e.message}")
                }
                speechRecognizer = null

                if (SpeechRecognizer.isRecognitionAvailable(this@KuzmixVoiceService)) {
                    speechRecognizer = try {
                        SpeechRecognizer.createSpeechRecognizer(this@KuzmixVoiceService)
                    } catch (e: Exception) {
                        android.util.Log.e("KuzmixOS", "Standard SpeechRecognizer creation failed: ${e.message}")
                        null
                    }

                    speechRecognizer?.setRecognitionListener(this@KuzmixVoiceService)

                    recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                    }

                    if (speechRecognizer != null && !isListeningPaused && !isSpeakingTts) {
                        startListeningLoop()
                    }
                } else {
                    android.util.Log.w("KuzmixOS", "Speech recognition not available on device.")
                    scheduleRestartListening(5000)
                }
            } catch (e: Exception) {
                android.util.Log.e("KuzmixVoiceService", "initSpeechRecognizer failed: ${e.message}")
                scheduleRestartListening(2000)
            }
        }
    }

    private fun startListeningLoop() {
        handler.post {
            if (isListeningPaused) {
                return@post
            }
            if (speechRecognizer == null) {
                initSpeechRecognizer()
                return@post
            }
            if (!isListening && recognizerIntent != null) {
                try {
                    isListening = true
                    speechRecognizer?.startListening(recognizerIntent)
                    android.util.Log.d("KuzmixVoiceService", "SpeechRecognizer startListening initiated.")
                } catch (e: Exception) {
                    android.util.Log.e("KuzmixVoiceService", "startListening failed: ${e.message}")
                    isListening = false
                    scheduleRestartListening(800)
                }
            }
        }
    }

    private fun cancelAndRestartListening(delayMs: Long = 200) {
        handler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isListening = false
            scheduleRestartListening(delayMs)
        }
    }

    private val restartRunnable = Runnable {
        if (!isListeningPaused && !isSpeakingTts) {
            isListening = false
            startListeningLoop()
        }
    }

    private fun scheduleRestartListening(delayMs: Long) {
        handler.removeCallbacks(restartRunnable)
        handler.postDelayed(restartRunnable, delayMs)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceSafely()

        intent?.let {
            if (it.getBooleanExtra("EXTRA_RELOAD_SETTINGS", false)) {
                android.util.Log.d("KuzmixVoiceService", "Reloaded voice settings from SharedPreferences")
                return@let
            } else if (it.getBooleanExtra("EXTRA_PAUSE_LISTENING", false)) {
                android.util.Log.d("KuzmixVoiceService", "Received EXTRA_PAUSE_LISTENING")
                isListeningPaused = true
                try { speechRecognizer?.cancel() } catch (unused: Exception) {}
                isListening = false
                return@let
            } else if (it.getBooleanExtra("EXTRA_RESUME_LISTENING", false)) {
                android.util.Log.d("KuzmixVoiceService", "Received EXTRA_RESUME_LISTENING")
                isListeningPaused = false
                startListeningLoop()
                return@let
            }

            val query = it.getStringExtra("EXTRA_QUERY")
            if (!query.isNullOrBlank()) {
                isManualTrigger = true
                isListeningPaused = false
                handleVoiceQuery(query)
            } else if (it.getBooleanExtra("EXTRA_START_LISTENING", false)) {
                isListeningPaused = false
                startListeningLoop()
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun containsWakeWord(text: String): Boolean {
        if (text.isBlank()) return false
        val lower = text.lowercase().trim()

        val prefs = getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
        val sensitivity = prefs.getFloat("wake_word_sensitivity", 0.5f)
        val customWakeWord = prefs.getString("custom_wake_word", "hey kuzmix")?.lowercase()?.trim() ?: "hey kuzmix"
        val strictMode = prefs.getBoolean("wake_word_strict_mode", false)

        // Check custom phrase if user specified one
        if (customWakeWord.isNotBlank() && lower.contains(customWakeWord)) {
            return true
        }

        if (strictMode || sensitivity <= 0.25f) {
            // Strict / Low False Positive Mode
            return lower.contains("hey kuzmix") ||
                    lower.contains("hey kusmix") ||
                    lower.contains("hey kozmix") ||
                    lower.contains("ok kuzmix") ||
                    lower.contains("okay kuzmix") ||
                    lower.startsWith("kuzmix")
        } else if (sensitivity >= 0.75f) {
            // High Sensitivity Mode
            return lower.contains("hey kuzmix") ||
                    lower.contains("hey kusmix") ||
                    lower.contains("hey kuz mix") ||
                    lower.contains("hey kozmix") ||
                    lower.contains("hey casemix") ||
                    lower.contains("hey cosmic") ||
                    lower.contains("hey cosmix") ||
                    lower.contains("hey cosmo") ||
                    lower.contains("hey cosmos") ||
                    lower.contains("hey qzmix") ||
                    lower.contains("hey cause mix") ||
                    lower.contains("hey curse mix") ||
                    lower.contains("hey kc") ||
                    lower.contains("hey k") ||
                    lower.contains("kuzmix") ||
                    lower.contains("kusmix") ||
                    lower.contains("kuz mix") ||
                    lower.contains("kozmix") ||
                    lower.contains("cosmix") ||
                    lower.contains("cosmic") ||
                    lower.contains("cause mix") ||
                    lower.contains("curse mix") ||
                    lower.contains("ok kuzmix") ||
                    lower.contains("okay kuzmix") ||
                    lower.contains("ok cosmic") ||
                    lower.contains("hi kuzmix") ||
                    lower.contains("hello kuzmix") ||
                    lower.startsWith("kc ") ||
                    lower.startsWith("kc,") ||
                    lower == "kc"
        } else {
            // Balanced (Default) Sensitivity Mode
            return lower.contains("hey kuzmix") ||
                    lower.contains("hey kusmix") ||
                    lower.contains("hey kuz mix") ||
                    lower.contains("hey kozmix") ||
                    lower.contains("hey cosmic") ||
                    lower.contains("hey cosmix") ||
                    lower.contains("hey kc") ||
                    lower.contains("kuzmix") ||
                    lower.contains("kusmix") ||
                    lower.contains("kozmix") ||
                    lower.contains("cosmix") ||
                    lower.contains("ok kuzmix") ||
                    lower.contains("okay kuzmix") ||
                    lower.contains("hi kuzmix") ||
                    lower.contains("hello kuzmix") ||
                    lower.startsWith("kc ") ||
                    lower == "kc"
        }
    }

    private fun isDirectCommand(text: String): Boolean {
        if (text.isBlank()) return false

        val prefs = getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
        val directCommandsEnabled = prefs.getBoolean("wake_word_direct_commands", true)
        val sensitivity = prefs.getFloat("wake_word_sensitivity", 0.5f)

        if (!directCommandsEnabled || sensitivity <= 0.15f) {
            return false
        }

        val lower = text.lowercase().trim()
        val directKeywords = listOf(
            "torch", "flashlight", "turn on", "turn off", "open ", "launch ",
            "whatsapp", "volume", "mute", "unmute", "wifi", "wi-fi", "bluetooth",
            "camera", "what time", "set alarm", "take screenshot", "hotspot",
            "navigate to", "directions", "search ", "play ", "call ", "dial ",
            "what is", "who is", "tell me", "how to", "where is", "can you",
            "show me", "add ", "schedule", "weather", "calculate", "help",
            "clear", "lock", "read", "generate", "create", "draw", "paint", "animate",
            "battery", "brightness", "airplane", "flight mode", "silent", "vibrate",
            "screenshot", "settings", "nfc", "bluetooth", "timer", "countdown",
            "random number", "joke", "notification", "clipboard", "device info",
            "storage", "wifi networks", "send sms", "text ", "wallpaper",
            "dark mode", "light mode", "developer options", "phone settings",
            "about my phone", "flip a coin", "roll a dice", "stopwatch",
            "screen brightness", "dim screen", "set timer", "ring mode",
            "send text", "copy to clipboard", "what's in my clipboard"
        )
        return directKeywords.any { lower.contains(it) }
    }

    private fun handleVoiceQuery(rawInput: String) {
        val lowerRaw = rawInput.lowercase().trim()
        val isLowkey = lowerRaw.contains("lowkey") || lowerRaw.contains("loweky")

        if (isLowkey) {
            overlayManager.activateAuraLowkey()
        } else {
            overlayManager.activateAuraStandard()
        }

        val sanitized = intentEngine.sanitizeSpeech(rawInput)
            .replace("lowkey", "", ignoreCase = true)
            .replace("loweky", "", ignoreCase = true)
            .trim()

        val lower = sanitized.lowercase().trim()

        // Greeting only - user just called the wake word ("Hey Kuzmix" / "Kuzmix" / "Hey Cosmic" / "KC")
        if (sanitized.isBlank() || lower in listOf("hey", "hello", "hi", "yes", "yo", "kuzmix", "kusmix", "kozmix", "cosmic", "cosmix", "kc")) {
            overlayManager.showAssistantPanel = true
            overlayManager.isProcessing = false
            val greetingMsg = "Online. How can I assist you?"
            overlayManager.addAiResponse(greetingMsg)
            intentEngine.speak(greetingMsg)
            isManualTrigger = true
            scheduleRestartListening(500)
            return
        }

        // Type C: Video Generation Query ("animate", "generate a video of", etc.)
        val videoResult = playGeneratedVideo(sanitized)
        if (videoResult != null && videoResult.startsWith("__OPENROUTER_VIDEO__:") && !isLowkey) {
            val videoPrompt = videoResult.removePrefix("__OPENROUTER_VIDEO__:")
            intentEngine.speak("Forging celestial animation for $videoPrompt via OpenRouter. This may take a moment.")
            serviceScope.launch {
                try {
                    val keyManager = OpenRouterKeyManager(this@KuzmixVoiceService)
                    val apiKey = keyManager.getActiveKey()
                    val videoUrl = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        generateVideoWithOpenRouter(
                            this@KuzmixVoiceService, apiKey, videoPrompt, "16:9", null
                        )
                    }
                    if (videoUrl != null) {
                        overlayManager.showVideoForge(videoPrompt, videoUrl)
                    } else {
                        intentEngine.speak("Video generation failed. Please try again.")
                    }
                } catch (e: Exception) {
                    intentEngine.speak("Video generation error: ${e.message}")
                }
            }
            return
        }

        // Type B: Image Generation Query
        if (!isLowkey && (lower.startsWith("generate ") ||
            lower.startsWith("draw ") ||
            lower.startsWith("create image") ||
            lower.startsWith("create an image of ") ||
            lower.startsWith("paint ") ||
            lower.startsWith("make an image of "))
        ) {
            val prompt = sanitized
                .replace("generate ", "", ignoreCase = true)
                .replace("draw ", "", ignoreCase = true)
                .replace("create image ", "", ignoreCase = true)
                .replace("create an image of ", "", ignoreCase = true)
                .replace("paint ", "", ignoreCase = true)
                .replace("make an image of ", "", ignoreCase = true)
                .trim()

            if (prompt.isNotBlank()) {
                overlayManager.showForge(prompt, null)
                intentEngine.speak("Forging neural visualization for $prompt via OpenRouter.")
                
                serviceScope.launch {
                    try {
                        val keyManager = OpenRouterKeyManager(this@KuzmixVoiceService)
                        val apiKey = keyManager.getActiveKey()
                        val imageUrl = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            generateImageWithOpenRouter(
                                this@KuzmixVoiceService, apiKey, prompt, "1:1", "high"
                            )
                        }
                        if (imageUrl != null) {
                            overlayManager.updateForgeImage(imageUrl)
                        } else {
                            intentEngine.speak("Image generation failed. Please try again.")
                            overlayManager.hideForge()
                        }
                    } catch (e: Exception) {
                        intentEngine.speak("Image generation error: ${e.message}")
                        overlayManager.hideForge()
                    }
                }
            } else {
                overlayManager.hideForge()
                intentEngine.speak("Please specify a prompt for neural image generation.")
            }
            return
        }

        // Type A: Normal Conversation / System Command / Query
        overlayManager.hideForge()
        if (!isLowkey) {
            overlayManager.showAssistantPanel = true
            if (rawInput.isNotBlank()) {
                overlayManager.overlayMessages.add(SingularityChatMessage(text = rawInput, isUser = true))
            }
        }
        overlayManager.isProcessing = true

        activeQueryJob?.cancel()
        activeQueryJob = serviceScope.launch {
            try {
                intentEngine.processIntent(sanitized) { responseText ->
                    if (responseText.isNotBlank()) {
                        if (!isLowkey) {
                            overlayManager.addAiResponse(responseText)
                        }
                    }
                }
                if (overlayManager.assistantResponseText.isNotBlank()) {
                    intentEngine.speak(overlayManager.assistantResponseText)
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    e.printStackTrace()
                }
            } finally {
                overlayManager.isProcessing = false
                handler.postDelayed({
                    if (!overlayManager.showAssistantPanel) {
                        overlayManager.deactivateAura()
                    }
                }, if (isLowkey) 1000 else 3500)
            }
        }
    }

    // RecognitionListener Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        isListening = true
    }

    override fun onBeginningOfSpeech() {
        audioBuffer.reset()
        if (isManualTrigger || (::overlayManager.isInitialized && overlayManager.isAuraActive)) {
            overlayManager.activateAura(0)
        }
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {
        if (buffer != null) {
            audioBuffer.write(buffer)
        }
    }

    override fun onEndOfSpeech() {
        isListening = false
    }

    override fun onError(error: Int) {
        handler.post {
            isListening = false
            android.util.Log.w("KuzmixVoiceService", "SpeechRecognizer onError code: $error")
            if (isListeningPaused) {
                return@post
            }

            when (error) {
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                SpeechRecognizer.ERROR_CLIENT -> {
                    handler.postDelayed({
                        if (!isListeningPaused) {
                            initSpeechRecognizer()
                        }
                    }, 400)
                }
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    scheduleRestartListening(250)
                }
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    scheduleRestartListening(4000)
                }
                SpeechRecognizer.ERROR_AUDIO -> {
                    handler.postDelayed({
                        if (!isListeningPaused) {
                            initSpeechRecognizer()
                        }
                    }, 600)
                }
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                SpeechRecognizer.ERROR_SERVER -> {
                    handler.postDelayed({
                        if (!isListeningPaused) {
                            initSpeechRecognizer()
                        }
                    }, 1500)
                }
                else -> {
                    handler.postDelayed({
                        if (!isListeningPaused) {
                            initSpeechRecognizer()
                        }
                    }, 800)
                }
            }
        }
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val matchingSpoken = matches.firstOrNull { containsWakeWord(it) || isStopCommand(it) || isDirectCommand(it) } ?: matches[0]

            val hasWake = matches.any { containsWakeWord(it) }
            val isStopCmd = isStopCommand(matchingSpoken)
            val isMediaActive = isMediaSessionActive

            // When media is playing, ONLY respond to explicit wake words or stop commands
            if (isMediaActive && !hasWake && !isStopCmd && !isManualTrigger) {
                android.util.Log.d("KuzmixVoiceService", "Media session active - ignoring non-wake-word input: '$matchingSpoken'")
                scheduleRestartListening(350)
                return
            }

            if (isStopCmd) {
                android.util.Log.d("KuzmixVoiceService", "Stop command recognized in results: '$matchingSpoken'")
                handleStopCommand()
                return
            }

            val hasDirect = matches.any { isDirectCommand(it) }
            val isOverlayActive = ::overlayManager.isInitialized && (overlayManager.showAssistantPanel || overlayManager.isAuraActive)

            android.util.Log.d("KuzmixVoiceService", "Speech recognized: '$matchingSpoken' (isManualTrigger=$isManualTrigger, hasWake=$hasWake, hasDirect=$hasDirect, isOverlayActive=$isOverlayActive, isMediaActive=$isMediaActive)")

            if (isManualTrigger || hasWake || hasDirect || isOverlayActive) {
                if (isSpeakingTts || intentEngine.isSpeaking) {
                    intentEngine.stopSpeaking()
                    activeQueryJob?.cancel()
                    isSpeakingTts = false
                }
                isManualTrigger = false
                handleVoiceQuery(matchingSpoken)
            } else {
                if (::overlayManager.isInitialized) {
                    overlayManager.deactivateAura()
                }
            }
        } else {
            isManualTrigger = false
            if (::overlayManager.isInitialized) {
                overlayManager.deactivateAura()
            }
        }
        scheduleRestartListening(350)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partialText = matches.firstOrNull { containsWakeWord(it) || isStopCommand(it) || isDirectCommand(it) } ?: matches[0]

            val hasWake = containsWakeWord(partialText)
            val isStopCmd = isStopCommand(partialText)

            // When media is playing, only react to wake words or stop commands
            if (isMediaSessionActive && !hasWake && !isStopCmd && !isManualTrigger) {
                return
            }

            if (isStopCmd) {
                android.util.Log.d("KuzmixVoiceService", "Stop command detected in partial speech: '$partialText'")
                handleStopCommand()
                return
            }
            if (isManualTrigger || hasWake || isDirectCommand(partialText)) {
                if (::overlayManager.isInitialized) {
                    overlayManager.activateAura(0)
                }
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {}
        speechRecognizer = null
        if (::overlayManager.isInitialized) {
            overlayManager.removeOverlay()
        }
        releaseWakeLock()
        restartService()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        android.util.Log.d("KuzmixOS", "KuzmixVoiceService onTaskRemoved - scheduling restart")
        restartService()
    }

    private fun restartService() {
        try {
            val restartIntent = Intent(applicationContext, KuzmixVoiceService::class.java).apply {
                putExtra("EXTRA_START_LISTENING", true)
            }
            val pendingIntent = android.app.PendingIntent.getService(
                applicationContext,
                2026,
                restartIntent,
                android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(
                android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                android.os.SystemClock.elapsedRealtime() + 1500,
                pendingIntent
            )
            android.util.Log.d("KuzmixOS", "KuzmixVoiceService restart scheduled in 1.5s")
        } catch (e: Exception) {
            android.util.Log.e("KuzmixOS", "Failed to schedule restart: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kuzmix Core Voice Sentinel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("KC Voice Assistant Active")
            .setContentText("Listening for \"Hey Kuzmix\" system-wide.")
            .setSmallIcon(R.drawable.ic_kuzmix_logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "kuzmix_voice_channel"
    }
}
