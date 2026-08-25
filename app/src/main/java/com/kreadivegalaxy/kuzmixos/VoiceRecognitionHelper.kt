package com.kreadivegalaxy.kuzmixos

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

/**
 * Helper component that manages microphone runtime permissions and SpeechRecognizer lifecycle.
 * Ensures RECORD_AUDIO permission is granted before initializing or triggering speech recognition.
 */
class VoiceRecognitionHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String, Int) -> Unit,
    private val onListeningStateChange: (Boolean) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null

    /**
     * Checks if microphone permission is granted.
     */
    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests microphone permission if an Activity reference is available.
     */
    fun requestMicrophonePermission(activity: Activity?, requestCode: Int = 1001) {
        if (activity != null) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                requestCode
            )
        }
    }

    /**
     * Initializes the SpeechRecognizer safely if permission is available.
     */
    fun initializeRecognizer(): Boolean {
        if (!hasMicrophonePermission()) {
            Log.w("VoiceRecognitionHelper", "Cannot initialize SpeechRecognizer: RECORD_AUDIO permission not granted.")
            return false
        }

        if (speechRecognizer == null && SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(createRecognitionListener())
                Log.d("VoiceRecognitionHelper", "SpeechRecognizer successfully initialized.")
                return true
            } catch (e: Exception) {
                Log.e("VoiceRecognitionHelper", "Failed to create SpeechRecognizer: ${e.message}", e)
                speechRecognizer = null
                return false
            }
        }
        return speechRecognizer != null
    }

    /**
     * Starts listening for speech after verifying microphone permissions.
     */
    fun startListening(activity: Activity? = null): Boolean {
        if (!hasMicrophonePermission()) {
            onError("Microphone permission required. Please grant RECORD_AUDIO permission.", SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
            requestMicrophonePermission(activity)
            return false
        }

        if (speechRecognizer == null) {
            val initialized = initializeRecognizer()
            if (!initialized) {
                onError("Speech recognition unavailable on this device.", SpeechRecognizer.ERROR_CLIENT)
                return false
            }
        }

        return try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
            true
        } catch (e: Exception) {
            Log.e("VoiceRecognitionHelper", "Error starting SpeechRecognizer: ${e.message}", e)
            onError("Failed to start speech recognition: ${e.message}", SpeechRecognizer.ERROR_CLIENT)
            false
        }
    }

    /**
     * Cancels current recognition.
     */
    fun stopListening() {
        try {
            speechRecognizer?.cancel()
            onListeningStateChange(false)
        } catch (e: Exception) {
            Log.e("VoiceRecognitionHelper", "Error stopping SpeechRecognizer: ${e.message}", e)
        }
    }

    /**
     * Cleans up SpeechRecognizer resources.
     */
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("VoiceRecognitionHelper", "Error destroying SpeechRecognizer: ${e.message}", e)
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onListeningStateChange(true)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                onListeningStateChange(false)
            }

            override fun onError(error: Int) {
                onListeningStateChange(false)
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timeout"
                    else -> "Speech recognition error (Code $error)"
                }
                onError(errorMessage, error)
            }

            override fun onResults(results: Bundle?) {
                onListeningStateChange(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                } else {
                    onError("No speech recognized", SpeechRecognizer.ERROR_NO_MATCH)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
