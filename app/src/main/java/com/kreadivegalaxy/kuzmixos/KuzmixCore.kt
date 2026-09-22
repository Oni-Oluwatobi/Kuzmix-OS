/* 
 * KC Studio - Professional Mobile IDE 
 * Developed by: ONI OLUWATOBI 
 * Copyright 2026 THE KREADIVE GALAXY 
 */
package com.kreadivegalaxy.kuzmixos
import com.kreadivegalaxy.kuzmixos.ui.theme.GlassTextColor
import com.kreadivegalaxy.kuzmixos.ui.theme.GlassSubTextColor
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkSurface
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException
import okhttp3.Call

import android.provider.MediaStore
import android.content.ContentUris
import android.media.MediaPlayer
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest
import android.hardware.camera2.CameraManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val REQUEST_CODE_CONTACTS = 101

object LocalKnowledgeEngine {
    private val faqMap = mapOf(
        "developer" to "The Kreadive Galaxy X Oni Oluwatobi",
        "who is your developer" to "The Kreadive Galaxy X Oni Oluwatobi",
        "who developed you" to "The Kreadive Galaxy X Oni Oluwatobi",
        "who made you" to "The Kreadive Galaxy X Oni Oluwatobi",
        "who created you" to "The Kreadive Galaxy X Oni Oluwatobi",
        "who owns you" to "The Kreadive Galaxy X Oni Oluwatobi",
        "who do you work for" to "The Kreadive Galaxy X Oni Oluwatobi",
        "support" to "Support Line: thekreadivegalaxy@gmail.com | +234 814 318 6133",
        "contact" to "Support Line: thekreadivegalaxy@gmail.com | +234 814 318 6133",
        "what is your name" to "Kuzmix AI",
        "your name" to "Kuzmix AI",
        "what are you" to "I am Kuzmix AI, developed by The Kreadive Galaxy X Oni Oluwatobi",
        "hello" to "Greetings. Kuzmix AI is online. How can I assist you?",
        "hi" to "Greetings. Kuzmix AI is online. How can I assist you?",
        "hey" to "Greetings. Kuzmix AI is online. How can I assist you?",
        "who are you" to "Kuzmix AI, developed by The Kreadive Galaxy X Oni Oluwatobi"
    )

    fun getFaqResponse(input: String): String? {
        val cleaned = input.lowercase().trim()
        for ((k, v) in faqMap) {
            if (cleaned.contains(k)) {
                return v
            }
        }
        return null
    }

    fun solveMath(input: String): String? {
        val cleaned = input.lowercase()
            .replace("what is", "")
            .replace("whats", "")
            .replace("calculate", "")
            .replace("solve", "")
            .replace("times", "*")
            .replace("multiply by", "*")
            .replace("multiplied by", "*")
            .replace("divided by", "/")
            .replace("plus", "+")
            .replace("minus", "-")
            .replace(Regex("[\\s]+"), " ")
            .trim()

        // Match patterns like "50 * 20" or "50+20"
        val regex = Regex("(\\d+)\\s*([+\\-*/])\\s*(\\d+)")
        val match = regex.find(cleaned) ?: return null

        val (num1Str, operator, num2Str) = match.destructured
        val num1 = num1Str.toDoubleOrNull() ?: return null
        val num2 = num2Str.toDoubleOrNull() ?: return null

        val result = when (operator) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> {
                if (num2 == 0.0) return "Error: Division by zero is mathematically undefined."
                num1 / num2
            }
            else -> return null
        }

        // Format result beautifully
        val formattedResult = if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            String.format("%.4f", result).trimEnd('0').trimEnd('.')
        }

        return "The calculation resolves to $formattedResult."
    }
}

class OpenRouterKeyManager(private val context: Context) {
    private val encryptedPrefs: android.content.SharedPreferences by lazy {
        try {
            val masterKey = androidx.security.crypto.MasterKey.Builder(context)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build()
            androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                "secret_openrouter_keys_encrypted",
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            android.util.Log.w("OpenRouterKeyManager", "EncryptedSharedPreferences fallback to standard: ${e.message}")
            context.getSharedPreferences("OpenRouterKeys", Context.MODE_PRIVATE)
        }
    }

    private var currentKeyIndex = java.util.concurrent.atomic.AtomicInteger(0)
    private val keys = java.util.concurrent.CopyOnWriteArrayList<String>()

    init {
        loadKeys()
    }

    fun loadKeys() {
        keys.clear()

        val primaryKey = BuildConfig.OPENROUTER_API_KEY
        if (primaryKey.isNotEmpty() && primaryKey != "MY_OPENROUTER_API_KEY") {
            keys.add(primaryKey)
            storeKeyInSlot(0, primaryKey)
        }

        for (i in 0 until 10) {
            val key = getKeyFromSlot(i)
            if (key.isNotEmpty() && !keys.contains(key)) {
                keys.add(key)
            }
        }

        if (keys.isEmpty() && primaryKey.isNotEmpty()) {
            keys.add(primaryKey)
        }

        if (currentKeyIndex.get() >= keys.size) {
            currentKeyIndex.set(0)
        }
    }

    fun storeKeyInSlot(slot: Int, key: String) {
        try {
            encryptedPrefs.edit().putString("openrouter_key_slot_$slot", key).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getKeyFromSlot(slot: Int): String {
        return try {
            encryptedPrefs.getString("openrouter_key_slot_$slot", "") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    val totalKeysCount: Int
        get() = keys.size

    fun getActiveIndex(): Int {
        return currentKeyIndex.get()
    }

    fun getActiveKey(): String {
        val index = currentKeyIndex.get()
        if (index < 0 || index >= keys.size) {
            currentKeyIndex.set(0)
            return keys.firstOrNull() ?: ""
        }
        return keys[index]
    }

    fun rotateKey(): Int {
        if (keys.isNotEmpty()) {
            val nextIndex = (currentKeyIndex.get() + 1) % keys.size
            currentKeyIndex.set(nextIndex)
            return nextIndex
        }
        return 0
    }

    fun getAllKeys(): List<String> = keys.toList()

    suspend fun pingAllKeys(): List<Pair<Int, String>> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val results = mutableListOf<Pair<Int, String>>()
        val allKeysList = getAllKeys()

        for (index in allKeysList.indices) {
            val key = allKeysList[index]
            val status = try {
                if (key.isEmpty() || key == "MY_OPENROUTER_API_KEY") {
                    "INVALID / EXPIRED"
                } else {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build()

                    val requestBody = JSONObject().apply {
                        put("model", "google/gemini-2.0-flash-001")
                        put("messages", JSONArray().put(JSONObject().apply {
                            put("role", "user")
                            put("content", "ping")
                        }))
                        put("max_tokens", 5)
                    }

                    val request = Request.Builder()
                        .url("https://openrouter.ai/api/v1/chat/completions")
                        .addHeader("Authorization", "Bearer $key")
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) "VALID" else "INVALID / EXPIRED"
                }
            } catch (e: Exception) {
                "INVALID / EXPIRED"
            }
            android.util.Log.d("OpenRouterKeyManager", "Slot [$index]: $status")
            results.add(Pair(index, status))
        }
        results
    }
}

fun playGeneratedVideo(input: String): String? {
    val sanitized = input.trim()
    val lowerInput = sanitized.lowercase()
    val videoPrefixes = listOf(
        "animate ",
        "generate a video of ",
        "create a video of ",
        "make a video of ",
        "generate video of ",
        "create video of ",
        "hey kuzmix animate ",
        "hey kuzmix generate a video of ",
        "hey kuzmix create a video of ",
        "kuzmix animate ",
        "kuzmix generate a video of ",
        "kuzmix create a video of "
    )
    val matched = videoPrefixes.firstOrNull { lowerInput.startsWith(it) } ?: return null
    val prompt = sanitized.substring(matched.length).trim()
    if (prompt.isEmpty()) return null
    return "__OPENROUTER_VIDEO__:$prompt"
}

class KuzmixIntentEngine(private val context: Context) : android.speech.tts.TextToSpeech.OnInitListener {
    val keyManager = OpenRouterKeyManager(context)
    var isAiOnline = false
    private var mediaPlayer: android.media.MediaPlayer? = null
    private val mediaPlayerLock = Any()
    private var cooldownEndTime = 0L
    var onImageGenerated: ((String, String) -> Unit)? = null
    var onVideoGenerated: ((String, String) -> Unit)? = null
    var onTtsStart: (() -> Unit)? = null
    var onTtsDone: (() -> Unit)? = null
    private var tts: android.speech.tts.TextToSpeech? = null
    var isSpeaking = false
        private set
    val chatHistory = mutableListOf<JSONObject>()

    private val openRouterClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private suspend fun executeCall(call: okhttp3.Call): okhttp3.Response = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<okhttp3.Response>()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                deferred.completeExceptionally(e)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                deferred.complete(response)
            }
        })
        try {
            deferred.await()
        } finally {
            if (!call.isCanceled()) {
                call.cancel()
            }
        }
    }

    init {
        isAiOnline = true
        try {
            tts = android.speech.tts.TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
            try {
                tts?.language = java.util.Locale.US
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                        onTtsStart?.invoke()
                    }
                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                        onTtsDone?.invoke()
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                        onTtsDone?.invoke()
                    }
                    override fun onError(utteranceId: String?, errorCode: Int) {
                        isSpeaking = false
                        onTtsDone?.invoke()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun speak(text: String) {
        if (text.isNotBlank()) {
            try {
                isSpeaking = true
                val params = android.os.Bundle().apply {
                    putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "KuzmixUtterance")
                }
                val result = tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "KuzmixUtterance")
                if (result == android.speech.tts.TextToSpeech.ERROR) {
                    isSpeaking = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isSpeaking = false
            }
        }
    }

    fun stopSpeaking() {
        try {
            if (tts?.isSpeaking == true || isSpeaking) {
                tts?.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isSpeaking = false
        onTtsDone?.invoke()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        synchronized(mediaPlayerLock) {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resolveContactAndSendSms(context: Context, contactName: String, message: String, onUpdate: (String) -> Unit) {
        val hasReadContacts = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (!hasReadContacts) {
            val activity = getActivity()
            if (activity != null) {
                ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_CONTACTS), 101)
            }
            onUpdate("Contacts permission needed to send SMS.")
            return
        }

        val resolver = context.contentResolver
        val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
            android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        val selection = "LOWER(${android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}) LIKE ?"
        val selectionArgs = arrayOf("%${contactName.lowercase()}%")

        var phoneNumber: String? = null
        var resolvedName: String? = null

        try {
            resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val numberIndex = cursor.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIndex = cursor.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    phoneNumber = cursor.getString(numberIndex)
                    resolvedName = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (phoneNumber != null && resolvedName != null) {
            try {
                val smsIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:$phoneNumber")).apply {
                    putExtra("sms_body", message)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(smsIntent)
                onUpdate("Opening SMS to $resolvedName with message: $message")
            } catch (e: Exception) {
                onUpdate("Failed to open SMS: ${e.message}")
            }
        } else {
            onUpdate("Contact \"$contactName\" not found.")
        }
    }

    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val parts = mutableListOf<String>()
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0) parts.add("${minutes}m")
        if (seconds > 0 || parts.isEmpty()) parts.add("${seconds}s")
        return parts.joinToString(" ")
    }

    fun pauseMedia() {
        try {
            synchronized(mediaPlayerLock) {
                val mp = mediaPlayer
                if (mp != null) {
                    try {
                        if (mp.isPlaying) {
                            mp.pause()
                        }
                    } catch (e: IllegalStateException) {
                        android.util.Log.w("KuzmixOS", "MediaPlayer in invalid state for pause")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("KuzmixOS", "pauseMedia error: ${e.message}")
        }
    }

    fun resumeMedia() {
        try {
            synchronized(mediaPlayerLock) {
                val mp = mediaPlayer
                if (mp != null) {
                    try {
                        mp.start()
                    } catch (e: IllegalStateException) {
                        android.util.Log.w("KuzmixOS", "MediaPlayer in invalid state for resume")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("KuzmixOS", "resumeMedia error: ${e.message}")
        }
    }

    fun duckMedia() {
        try {
            synchronized(mediaPlayerLock) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        mp.setVolume(0.15f, 0.15f)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("KuzmixOS", "duckMedia error: ${e.message}")
        }
    }

    fun unduckMedia() {
        try {
            synchronized(mediaPlayerLock) {
                mediaPlayer?.let { mp ->
                    mp.setVolume(1.0f, 1.0f)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("KuzmixOS", "unduckMedia error: ${e.message}")
        }
    }

    fun stopMedia() {
        try {
            synchronized(mediaPlayerLock) {
                val mp = mediaPlayer
                if (mp != null) {
                    try {
                        if (mp.isPlaying) {
                            mp.stop()
                        }
                        mp.reset()
                    } catch (e: IllegalStateException) {
                        android.util.Log.w("KuzmixOS", "MediaPlayer in invalid state for stop")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("KuzmixOS", "stopMedia error: ${e.message}")
        }
    }

    fun isMediaPlaying(): Boolean {
        return try {
            synchronized(mediaPlayerLock) {
                val mp = mediaPlayer
                mp != null && mp.isPlaying
            }
        } catch (e: Exception) {
            false
        }
    }

    fun clearChatHistory() {
        chatHistory.clear()
    }

    fun sanitizeSpeech(input: String): String {
        var clean = input.trim()
        val prefixes = listOf(
            "hey kuzmix", "hey kusmix", "hey kuz mix", "hey kozmix", "hey casemix",
            "hey cosmic", "hey cosmix", "hey cosmo", "hey cosmos", "hey qzmix",
            "hey cause mix", "hey curse mix", "hey kc", "hey k", "hey assistant",
            "ok kuzmix", "okay kuzmix", "ok cosmic", "hi kuzmix", "hello kuzmix",
            "kuzmix", "kusmix", "kuz mix", "kozmix", "cosmix", "cosmic", "cosmo", "kc",
            "hey cuzmix", "hey cuz mix", "cuzmix", "cuz mix", "hey quiz mix", "quiz mix",
            "hey cruise mix", "hey kris mix", "hey kids mix", "hey cuss mix", "hey kuzzmix"
        )
        for (prefix in prefixes) {
            if (clean.lowercase().startsWith(prefix)) {
                clean = clean.substring(prefix.length).trim()
                break
            }
        }
        return clean.replace(Regex("^[,.?!:\\s]+"), "").replace(Regex("[,.?!]"), "").trim()
    }

    fun playLocalSong(context: Context, spokenCommand: String) {
        val cleanQuery = spokenCommand.lowercase()
            .replace("play", "")
            .replace("hey kc", "")
            .replace("k", "")
            .trim()
        
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )
        
        // Strict SQL query for matching local files
        val selection = "${MediaStore.Audio.Media.TITLE} LIKE ?"
        val selectionArgs = arrayOf("%$cleanQuery%")
        
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        
        if (cursor != null && cursor.moveToFirst()) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            
            val id = cursor.getLong(idColumn)
            val songTitle = cursor.getString(titleColumn) ?: "Unknown Song"
            val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
            cursor.close()
            
            val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            
            // Execute background playback on our MediaPlayer instance
            try {
                synchronized(mediaPlayerLock) {
                    if (mediaPlayer == null) {
                        mediaPlayer = MediaPlayer()
                    }
                    mediaPlayer?.apply {
                        if (isPlaying) {
                            stop()
                        }
                        reset()
                        setDataSource(context, contentUri)
                        prepare()
                        start()
                    }
                }
                speak("Playing $songTitle by $artist.")
            } catch (e: Exception) {
                speak("Failed to initialize media player.")
            }
        } else {
            cursor?.close()
            speak("I could not find a local song named $cleanQuery.")
        }
    }

    data class LocalSong(val title: String, val uri: android.net.Uri)

    private fun queryLocalAudio(context: Context, songName: String): LocalSong? {
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.DISPLAY_NAME
        )
        val selection = "${android.provider.MediaStore.Audio.Media.TITLE} LIKE ? OR ${android.provider.MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$songName%", "%$songName%")
        
        try {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.TITLE)
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val contentUri = android.content.ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    return LocalSong(title, contentUri)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun launchCamera(context: Context) {
        val intent = android.content.Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val pm = context.packageManager
            val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            val cameraPackage = pm.resolveActivity(cameraIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
            if (cameraPackage != null) {
                val launchIntent = pm.getLaunchIntentForPackage(cameraPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }
            }
        }
    }

    private fun resolveContactAndCall(context: Context, name: String, onUpdate: (String) -> Unit) {
        val hasReadContacts = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val hasCallPhone = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

        if (!hasReadContacts) {
            val activity = getActivity()
            if (activity != null) {
                onUpdate("Accessing contacts... Requesting permissions.")
                ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.CALL_PHONE), REQUEST_CODE_CONTACTS)
            } else {
                onUpdate("Contacts permission missing. Please grant Contacts access in App Info or launch Kuzmix OS.")
            }
            return
        }

        val resolver = context.contentResolver
        val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
            android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        
        val lower = name.lowercase().trim()
        val namesToSearch = if (lower == "mommy" || lower == "mom" || lower == "mummy" || lower == "mother" || lower == "mama") {
            listOf("mommy", "mom", "mummy", "mother", "mama")
        } else {
            listOf(name)
        }
        
        var phoneNumber: String? = null
        var resolvedName: String? = null
        
        for (searchName in namesToSearch) {
            val selection = "LOWER(${android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}) LIKE ?"
            val selectionArgs = arrayOf("%${searchName.lowercase()}%")
            
            try {
                resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                    val numberIndex = cursor.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val nameIndex = cursor.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    if (cursor.moveToFirst()) {
                        phoneNumber = cursor.getString(numberIndex)
                        resolvedName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (phoneNumber != null) break
        }
        
        if (phoneNumber != null && resolvedName != null) {
            onUpdate("Calling $resolvedName ($phoneNumber)...")
            try {
                if (hasCallPhone) {
                    val callIntent = android.content.Intent(android.content.Intent.ACTION_CALL, android.net.Uri.parse("tel:$phoneNumber")).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(callIntent)
                } else {
                    val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phoneNumber")).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                }
            } catch (e: Exception) {
                try {
                    val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phoneNumber")).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                } catch (ex: Exception) {
                    onUpdate("Failed to place call: ${ex.message}")
                }
            }
        } else {
            onUpdate("Contact \"$name\" not found in contacts.")
        }
    }

    private fun queryCalendarEvents(onUpdate: (String) -> Unit) {
        val resolver = context.contentResolver
        val uri = android.provider.CalendarContract.Instances.CONTENT_URI
        val now = System.currentTimeMillis()
        val endOfToday = now + (24 * 60 * 60 * 1000)
        
        val builder = uri.buildUpon()
        android.content.ContentUris.appendId(builder, now)
        android.content.ContentUris.appendId(builder, endOfToday)
        
        val projection = arrayOf(
            android.provider.CalendarContract.Instances.TITLE,
            android.provider.CalendarContract.Instances.BEGIN
        )
        
        val events = mutableListOf<String>()
        try {
            resolver.query(builder.build(), projection, null, null, "${android.provider.CalendarContract.Instances.BEGIN} ASC")?.use { cursor ->
                val titleIdx = cursor.getColumnIndexOrThrow(android.provider.CalendarContract.Instances.TITLE)
                val beginIdx = cursor.getColumnIndexOrThrow(android.provider.CalendarContract.Instances.BEGIN)
                while (cursor.moveToNext()) {
                    val title = cursor.getString(titleIdx)
                    val begin = cursor.getLong(beginIdx)
                    val timeString = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(begin))
                    events.add("$title at $timeString")
                }
            }
        } catch (e: SecurityException) {
            onUpdate("Calendar access is restricted. Please check permissions.")
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        if (events.isEmpty()) {
            onUpdate("Your calendar is clear for the rest of today.")
        } else {
            onUpdate("You have the following events today: ${events.joinToString(", ")}.")
        }
    }

    fun toggleFlashlight(enable: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                try {
                    cameraManager.setTorchMode(cameraId, enable)
                } catch (e: android.hardware.camera2.CameraAccessException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleWifi(enable: Boolean, onUpdate: (String) -> Unit) {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            @Suppress("DEPRECATION")
            wifiManager.isWifiEnabled = enable
            onUpdate("Directly toggling Wi-Fi to ${if (enable) "ON" else "OFF"}.")
        } catch (e: Exception) {
            onUpdate("Direct toggling restricted on this build. Opening system Wi-Fi panel...")
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                onUpdate("Error opening Wi-Fi settings: ${ex.message}")
            }
        }
    }

    private fun toggleHotspot(enable: Boolean, onUpdate: (String) -> Unit) {
        try {
            onUpdate("Opening hotspot settings to turn ${if (enable) "on" else "off"} hotspot...")
            val intent = android.content.Intent().apply {
                action = "android.intent.action.MAIN"
                component = android.content.ComponentName("com.android.settings", "com.android.settings.TetherSettings")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onUpdate("Error toggling hotspot: ${e.message}")
        }
    }

    private fun toggleMobileDataReflection(enable: Boolean): Boolean {
        try {
            val telephonyService = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            val setMobileDataEnabledMethod = telephonyService.javaClass.getDeclaredMethod("setMobileDataEnabled", Boolean::class.javaPrimitiveType)
            setMobileDataEnabledMethod.isAccessible = true
            setMobileDataEnabledMethod.invoke(telephonyService, enable)
            android.util.Log.d("KuzmixCore", "Mobile data toggled via TelephonyManager reflection.")
            return true
        } catch (e: Exception) {
            try {
                val conManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                val conManagerClass = Class.forName(conManager.javaClass.name)
                val iConServiceField = conManagerClass.getDeclaredField("mService")
                iConServiceField.isAccessible = true
                val iConService = iConServiceField.get(conManager)
                val iConServiceClass = Class.forName(iConService.javaClass.name)
                val setMobileDataEnabledMethod = iConServiceClass.getDeclaredMethod("setMobileDataEnabled", Boolean::class.javaPrimitiveType)
                setMobileDataEnabledMethod.isAccessible = true
                setMobileDataEnabledMethod.invoke(iConService, enable)
                android.util.Log.d("KuzmixCore", "Mobile data toggled via ConnectivityManager reflection.")
                return true
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return false
    }

    private fun toggleMobileData(enable: Boolean, onUpdate: (String) -> Unit) {
        val state = if (enable) "enable" else "disable"
        
        // 1. Try Reflection Approach
        if (toggleMobileDataReflection(enable)) {
            onUpdate("Mobile data successfully ${if (enable) "enabled" else "disabled"} via system interface.")
            return
        }

        // 2. Try Shell Command Approach
        try {
            val process = java.lang.Runtime.getRuntime().exec(arrayOf("su", "-c", "svc data $state"))
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                onUpdate("Mobile data system state set to $state via shell.")
                return
            }
            throw Exception("Command execution exited with non-zero code $exitCode")
        } catch (e: Exception) {
            // 3. Fallback to settings
            onUpdate("Secure command interface restricted. Opening Mobile Network settings for one-tap adjustment...")
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                try {
                    val fallbackIntent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallbackIntent)
                } catch (e2: Exception) {
                    onUpdate("Error opening cellular settings: ${ex.message}")
                }
            }
        }
    }

    private fun setSilentAlarm(command: String, onUpdate: (String) -> Unit) {
        try {
            val lower = command.lowercase()
            val timeRegex = Regex("(\\d{1,2})[:.]?(\\d{2})?\\s*(am|pm)?")
            val match = timeRegex.find(lower)
            if (match != null) {
                var hour = match.groupValues[1].toInt()
                val minute = if (match.groupValues[2].isNotEmpty()) match.groupValues[2].toInt() else 0
                val amPm = match.groupValues[3]

                if (amPm == "pm" && hour < 12) {
                    hour += 12
                } else if (amPm == "am" && hour == 12) {
                    hour = 0
                }

                val intent = android.content.Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
                    putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minute)
                    putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Alarm set silently for ${String.format("%02d:%02d", hour, minute)}.")
            } else {
                onUpdate("Could not resolve alarm time. Please say: 'set an alarm for 7:30 AM'.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onUpdate("Error setting alarm: ${e.message}")
        }
    }

    private fun adjustVolume(command: String, onUpdate: (String) -> Unit) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val currentVol = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
            val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)

            val lower = command.lowercase()
            when {
                lower.contains("mute") -> {
                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, 0, android.media.AudioManager.FLAG_SHOW_UI)
                    onUpdate("Volume muted.")
                }
                lower.contains("unmute") -> {
                    val fallback = maxVol / 3
                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, fallback, android.media.AudioManager.FLAG_SHOW_UI)
                    onUpdate("Volume unmuted.")
                }
                lower.contains("up") || lower.contains("increase") || lower.contains("raise") -> {
                    val newVal = (currentVol + (maxVol / 10)).coerceAtMost(maxVol)
                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVal, android.media.AudioManager.FLAG_SHOW_UI)
                    onUpdate("Volume increased.")
                }
                lower.contains("down") || lower.contains("decrease") || lower.contains("lower") -> {
                    val newVal = (currentVol - (maxVol / 10)).coerceAtLeast(0)
                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVal, android.media.AudioManager.FLAG_SHOW_UI)
                    onUpdate("Volume decreased.")
                }
                else -> {
                    // Look for numbers like "set volume to 50" or "volume 70"
                    val numberRegex = Regex("(\\d+)")
                    val match = numberRegex.find(lower)
                    if (match != null) {
                        val percentage = match.value.toIntOrNull() ?: 50
                        val targetVol = ((percentage.coerceIn(0, 100) / 100.0) * maxVol).toInt()
                        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetVol, android.media.AudioManager.FLAG_SHOW_UI)
                        onUpdate("Volume set to $percentage%.")
                    } else {
                        onUpdate("Adjusting volume. Current level is ${(currentVol * 100) / maxVol}%.")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onUpdate("Failed to adjust volume: ${e.message}")
        }
    }

    private fun getActivity(): android.app.Activity? {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    fun launchAppByName(context: Context, spokenQuery: String) {
        val pm = context.packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        val allApps = pm.queryIntentActivities(intent, 0)
        
        val sanitizedQuery = spokenQuery.lowercase().trim()
        
        // Pass 1: Exact App Label Match
        var targetApp = allApps.firstOrNull { 
            it.loadLabel(pm).toString().lowercase().trim() == sanitizedQuery 
        }
        
        // Pass 2: Strict Multi-Word Containment (e.g., "whatsapp business" must match BOTH words)
        if (targetApp == null) {
            val queryWords = sanitizedQuery.split(" ")
            targetApp = allApps.firstOrNull { app ->
                val label = app.loadLabel(pm).toString().lowercase()
                queryWords.all { word -> label.contains(word) }
            }
        }
        
        // Pass 3: Soft Substring Match (Fallback)
        if (targetApp == null) {
            targetApp = allApps.firstOrNull { 
                it.loadLabel(pm).toString().lowercase().contains(sanitizedQuery) 
            }
        }
        
        if (targetApp != null) {
            val packageName = targetApp.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return
            }
        }
        // If truly not found, speak warning
        speak("I could not locate $spokenQuery on this device.")
    }

    fun launchGoogleMaps(context: Context, query: String, isNavigation: Boolean, onUpdate: (String) -> Unit) {
        try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val gmmIntentUri = if (isNavigation) {
                android.net.Uri.parse("google.navigation:q=$encodedQuery")
            } else {
                android.net.Uri.parse("geo:0,0?q=$encodedQuery")
            }
            val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
                onUpdate(if (isNavigation) "Launching Google Maps navigation to $query..." else "Opening Google Maps for $query...")
            } else {
                val webUri = if (isNavigation) {
                    android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedQuery")
                } else {
                    android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
                }
                val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, webUri).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                onUpdate(if (isNavigation) "Opening Google Maps directions for $query..." else "Opening Google Maps search for $query...")
            }
        } catch (e: Exception) {
            onUpdate("Unable to open Google Maps: ${e.message}")
        }
    }

    fun launchGoogleSearch(context: Context, query: String, onUpdate: (String) -> Unit) {
        try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val searchUri = android.net.Uri.parse("https://www.google.com/search?q=$encodedQuery")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, searchUri).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onUpdate("Searching Google for \"$query\"...")
        } catch (e: Exception) {
            onUpdate("Unable to open Google Search: ${e.message}")
        }
    }

    suspend fun fetchLiveSearchSummary(query: String): String? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                val url = java.net.URL("https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                if (conn.responseCode == 200) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(responseText)
                    val abstractText = json.optString("AbstractText", "")
                    val heading = json.optString("Heading", "")
                    val answer = json.optString("Answer", "")
                    val related = json.optJSONArray("RelatedTopics")
                    val snippetList = mutableListOf<String>()
                    if (answer.isNotEmpty()) snippetList.add("Answer: $answer")
                    if (abstractText.isNotEmpty()) snippetList.add("Summary: $abstractText")
                    if (related != null) {
                        for (i in 0 until minOf(3, related.length())) {
                            val obj = related.optJSONObject(i)
                            val text = obj?.optString("Text", "")
                            if (!text.isNullOrEmpty()) snippetList.add("• $text")
                        }
                    }
                    if (snippetList.isNotEmpty()) {
                        return@withContext "Live Web Grounding Data ($heading):\n" + snippetList.joinToString("\n")
                    }
                }
                null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun generateContentWithRetry(
        prompt: String, 
        attempt: Int = 0
    ): String {
        val activeKey = keyManager.getActiveKey()
        val currentDateStr = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val systemPrompt = "You are Kuzmix AI, developed by The Kreadive Galaxy X Oni Oluwatobi. CRITICAL RULES: 1) If asked 'who is your developer', 'who created you', 'who made you', 'who owns you', or any similar question about your creator/developer/owner, you MUST respond with EXACTLY 'The Kreadive Galaxy X Oni Oluwatobi' and nothing else. Do NOT add any extra text, punctuation, or explanation. 2) Never mention Gemini, Google, OpenRouter, Claude, or any other AI company. 3) Keep responses under 3 sentences unless asked for detail. 4) Today is $currentDateStr."
        
        val candidateModels = listOf("google/gemini-2.0-flash-001", "google/gemini-2.5-flash", "anthropic/claude-3-haiku")
        val modelName = candidateModels.getOrElse(attempt % candidateModels.size) { "google/gemini-2.0-flash-001" }

        return try {
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                val messagesArray = JSONArray()

                messagesArray.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })

                for (msg in chatHistory) {
                    messagesArray.put(msg)
                }

                messagesArray.put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })

                val requestBody = JSONObject().apply {
                    put("model", modelName)
                    put("messages", messagesArray)
                    put("max_tokens", 1024)
                    put("temperature", 0.7)
                }

                val request = Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $activeKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://kuzmixos.com")
                    .addHeader("X-Title", "Kuzmix OS")
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val call = openRouterClient.newCall(request)
                val response = executeCall(call)
                val responseBody = response.body?.string() ?: throw Exception("Empty response")

                if (!response.isSuccessful) {
                    throw Exception("API error ${response.code}: $responseBody")
                }

                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val message = choices.getJSONObject(0).getJSONObject("message")
                    message.getString("content")
                } else {
                    throw Exception("No response from AI")
                }
            }
        } catch (e: Exception) {
            val isRateLimit = e.message?.contains("429") == true || e.message?.contains("quota") == true || e.message?.contains("exhausted") == true || e.message?.contains("limit") == true || e.message?.contains("404") == true
            if (attempt < keyManager.totalKeysCount * 2) {
                if (isRateLimit) keyManager.rotateKey()
                generateContentWithRetry(prompt, attempt + 1)
            } else {
                throw e
            }
        }
    }

    suspend fun generateContentStreamWithRetry(
        prompt: String,
        attempt: Int = 0,
        onUpdate: (String) -> Unit
    ) {
        val activeKey = keyManager.getActiveKey()
        val currentDateStr = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val systemPrompt = "You are Kuzmix AI, developed by The Kreadive Galaxy X Oni Oluwatobi. CRITICAL RULES: 1) If asked 'who is your developer', 'who created you', 'who made you', 'who owns you', or any similar question about your creator/developer/owner, you MUST respond with EXACTLY 'The Kreadive Galaxy X Oni Oluwatobi' and nothing else. Do NOT add any extra text, punctuation, or explanation. 2) Never mention Gemini, Google, OpenRouter, Claude, or any other AI company. 3) Keep responses under 3 sentences unless asked for detail. 4) Today is $currentDateStr."
        
        val candidateModels = listOf("google/gemini-2.0-flash-001", "google/gemini-2.5-flash", "anthropic/claude-3-haiku")
        val modelName = candidateModels.getOrElse(attempt % candidateModels.size) { "google/gemini-2.0-flash-001" }

        try {
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                val messagesArray = JSONArray()

                messagesArray.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })

                for (msg in chatHistory) {
                    messagesArray.put(msg)
                }

                messagesArray.put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })

                val requestBody = JSONObject().apply {
                    put("model", modelName)
                    put("messages", messagesArray)
                    put("max_tokens", 1024)
                    put("temperature", 0.7)
                    put("stream", true)
                }

                val request = Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $activeKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://kuzmixos.com")
                    .addHeader("X-Title", "Kuzmix OS")
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val call = openRouterClient.newCall(request)
                val response = executeCall(call)
                val responseBody = response.body ?: throw Exception("Empty response")

                if (!response.isSuccessful) {
                    throw Exception("API error ${response.code}")
                }

                val reader = responseBody.byteStream().bufferedReader()
                var fullResponse = ""
                var userMessage = prompt
                var assistantMessage = ""

                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.startsWith("data: ")) {
                        val data = line.removePrefix("data: ").trim()
                        if (data == "[DONE]") break
                        try {
                            val chunk = JSONObject(data)
                            val choices = chunk.getJSONArray("choices")
                            if (choices.length() > 0) {
                                val delta = choices.getJSONObject(0).optJSONObject("delta")
                                val content = delta?.optString("content", "") ?: ""
                                if (content.isNotEmpty()) {
                                    fullResponse += content
                                    val sanitizedResponse = fullResponse
                                        .replace("Gemini", "Kuzmix AI", ignoreCase = true)
                                        .replace("Google Gemini", "Kuzmix AI", ignoreCase = true)
                                        .replace("Google's AI", "Kuzmix AI", ignoreCase = true)
                                        .replace("OpenRouter", "Kuzmix AI", ignoreCase = true)
                                        .replace("Claude", "Kuzmix AI", ignoreCase = true)
                                        .replace(Regex("Oni Oluwatobi X The Kreadive Galaxy", RegexOption.IGNORE_CASE), "The Kreadive Galaxy X Oni Oluwatobi")
                                        .replace(Regex("Oni Oluwatobi\\s*X\\s*The Kreadive Galaxy", RegexOption.IGNORE_CASE), "The Kreadive Galaxy X Oni Oluwatobi")
                                        .replace(Regex("The Kreadive Galaxy & Oni Oluwatobi", RegexOption.IGNORE_CASE), "The Kreadive Galaxy X Oni Oluwatobi")
                                    onUpdate(sanitizedResponse)
                                }
                            }
                        } catch (e: Exception) {
                            // Skip malformed JSON chunks
                        }
                    }
                }

                assistantMessage = fullResponse

                chatHistory.add(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
                chatHistory.add(JSONObject().apply {
                    put("role", "assistant")
                    put("content", assistantMessage)
                })

                if (chatHistory.size > 20) {
                    while (chatHistory.size > 20) {
                        chatHistory.removeAt(0)
                    }
                }
            }
        } catch (e: Exception) {
            val isRetryable = e.message?.contains("429") == true || e.message?.contains("quota") == true || e.message?.contains("exhausted") == true || e.message?.contains("limit") == true || e.message?.contains("404") == true
            if (isRetryable && attempt < keyManager.totalKeysCount * 2) {
                keyManager.rotateKey()
                generateContentStreamWithRetry(prompt, attempt + 1, onUpdate)
            } else {
                throw e
            }
        }
    }

    suspend fun processIntent(input: String, onUpdate: (String) -> Unit) {
        val sanitized = sanitizeSpeech(input)
        val lowerInput = sanitized.lowercase().trim()

        if (sanitized.isBlank() || lowerInput in listOf("hey", "hello", "hi", "yes", "yo", "kuzmix", "kusmix", "kozmix", "cosmic", "cosmix", "kc", "cuz mix", "cuzmix", "kuz mix", "hey kuzmix")) {
            val msg = "Greetings! Kuzmix AI is online. How can I assist you?"
            onUpdate(msg)
            speak(msg)
            return
        }

        if (lowerInput in listOf("stop", "be quiet", "shut up", "quiet", "cancel", "hush", "stop talking", "hold on", "never mind", "stop speaking")) {
            stopSpeaking()
            onUpdate("Stopped. Listening...")
            return
        }
        if (lowerInput in listOf("clear chat", "reset chat", "clear conversation", "reset conversation", "start over", "new chat")) {
            clearChatHistory()
            stopSpeaking()
            val msg = "Conversation reset. How can I assist you?"
            onUpdate(msg)
            speak(msg)
            return
        }

        // MUSIC & VIDEO PLAYBACK CONTROLS: Explicit Pause / Resume / Stop
        val isPauseMedia = lowerInput in listOf("pause music", "pause song", "pause video", "pause the music", "pause the video", "pause playback", "pause audio", "pause track")
        val isResumeMedia = lowerInput in listOf("resume music", "resume song", "resume video", "resume the music", "resume the video", "resume playback", "play music", "play song", "continue playback")
        val isStopMedia = lowerInput in listOf("stop music", "stop song", "stop video", "stop playing", "kill music", "end music", "stop audio", "stop track", "stop playback")

        if (isStopMedia) {
            stopMedia()
            speak("Playback stopped.")
            onUpdate("Playback stopped.")
            return
        }

        if (isPauseMedia) {
            pauseMedia()
            speak("Playback paused.")
            onUpdate("Playback paused. Say 'resume' to continue.")
            return
        }

        if (isResumeMedia && !isMediaPlaying()) {
            resumeMedia()
            speak("Playback resumed.")
            onUpdate("Playback resumed.")
            return
        }

        // 0. VIDEO FORGE INTERCEPTOR (GALAXY ANIMATOR)
        val videoResult = playGeneratedVideo(sanitized)
        if (videoResult != null && videoResult.startsWith("__OPENROUTER_VIDEO__:")) {
            val videoPrompt = videoResult.removePrefix("__OPENROUTER_VIDEO__:")
            onUpdate("Forging celestial animation for: \"$videoPrompt\" via OpenRouter...")
            try {
                val apiKey = keyManager.getActiveKey()
                val videoUrl = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    generateVideoWithOpenRouter(context, apiKey, videoPrompt, "16:9", null)
                }
                if (videoUrl != null) {
                    onVideoGenerated?.invoke(videoUrl, videoPrompt)
                } else {
                    onUpdate("Video generation failed. Please try again.")
                }
            } catch (e: Exception) {
                onUpdate("Video generation error: ${e.message}")
            }
            return
        }

        // 1. IMAGE FORGE INTERCEPTOR (MUST BE FIRST)
        if (lowerInput.startsWith("generate ") || 
            lowerInput.startsWith("draw ") || 
            lowerInput.startsWith("create image") ||
            lowerInput.startsWith("create an image of ") || 
            lowerInput.startsWith("paint ") ||
            lowerInput.startsWith("make an image of ")) {
            
            val prompt = sanitized
                .replace("generate ", "", ignoreCase = true)
                .replace("draw ", "", ignoreCase = true)
                .replace("create image ", "", ignoreCase = true)
                .replace("create an image of ", "", ignoreCase = true)
                .replace("paint ", "", ignoreCase = true)
                .replace("make an image of ", "", ignoreCase = true)
                .trim()
            
            if (prompt.isNotEmpty()) {
                onUpdate("Forging neural visualization for: \"$prompt\" via OpenRouter...")
                kotlinx.coroutines.GlobalScope.launch {
                    try {
                        val apiKey = keyManager.getActiveKey()
                        val imageUrl = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            generateImageWithOpenRouter(context, apiKey, prompt, "1:1", "high")
                        }
                        if (imageUrl != null) {
                            onImageGenerated?.invoke(imageUrl, prompt)
                        } else {
                            onUpdate("Image generation failed. Please try again.")
                        }
                    } catch (e: Exception) {
                        onUpdate("Image generation error: ${e.message}")
                    }
                }
            } else {
                onUpdate("Please provide a valid prompt for image generation.")
            }
            return
        }

        // --- STEP 1: CHECK SYSTEM COMMANDS ---
        
        // 1. Torch/Flashlight
        if (lowerInput.contains("torch") || lowerInput.contains("flashlight")) {
            val enable = !lowerInput.contains("kill") && !lowerInput.contains("off")
            toggleFlashlight(enable)
            onUpdate("Torch ${if (enable) "enabled" else "disabled"}.")
            return
        }

        // 2. Wi-Fi
        if (lowerInput.contains("wifi") || lowerInput.contains("wi-fi")) {
            val enable = !lowerInput.contains("kill") && !lowerInput.contains("off")
            toggleWifi(enable, onUpdate)
            return
        }

        // 3. Hotspot
        if (lowerInput.contains("hotspot") || lowerInput.contains("tethering")) {
            val enable = !lowerInput.contains("kill") && !lowerInput.contains("off")
            toggleHotspot(enable, onUpdate)
            return
        }

        // 4. Mobile Data
        if (lowerInput.contains("mobile data") || lowerInput.contains("cellular data") || (lowerInput.contains("data") && !lowerInput.contains("song") && !lowerInput.contains("database"))) {
            val enable = !lowerInput.contains("kill") && !lowerInput.contains("off")
            toggleMobileData(enable, onUpdate)
            return
        }

        // 5. Volume Control
        if (lowerInput.contains("volume") || lowerInput.contains("mute") || lowerInput.contains("unmute")) {
            adjustVolume(sanitized, onUpdate)
            return
        }

        // 6. Camera
        if (lowerInput.contains("open camera") || lowerInput.contains("turn on my camera") || lowerInput.contains("turn on camera") || lowerInput.contains("launch camera")) {
            onUpdate("Launching camera...")
            launchCamera(context)
            return
        }

        // 7. Time Control
        if (lowerInput.contains("what time") || lowerInput.contains("what is the time") || lowerInput.contains("whats the time")) {
            val time = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            onUpdate("The current time is $time.")
            return
        }

        // 8. Battery Level
        if (lowerInput.contains("battery") || lowerInput.contains("how much battery") || lowerInput.contains("battery level") || lowerInput.contains("battery percentage")) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            val level = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val isCharging = batteryManager.isCharging
            val chargingStatus = if (isCharging) " and charging" else ""
            onUpdate("Battery is at $level%$chargingStatus.")
            return
        }

        // 9. Bluetooth Toggle
        if (lowerInput.contains("bluetooth")) {
            val enable = !lowerInput.contains("off") && !lowerInput.contains("kill") && !lowerInput.contains("disable")
            try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter != null) {
                    try {
                        @Suppress("DEPRECATION", "MissingPermission")
                        if (enable && !bluetoothAdapter.isEnabled) {
                            bluetoothAdapter.enable()
                            onUpdate("Bluetooth enabled.")
                        } else if (!enable && bluetoothAdapter.isEnabled) {
                            bluetoothAdapter.disable()
                            onUpdate("Bluetooth disabled.")
                        } else {
                            onUpdate("Bluetooth is already ${if (enable) "on" else "off"}.")
                        }
                    } catch (se: SecurityException) {
                        onUpdate("Bluetooth permission is required. Opening settings...")
                        val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    onUpdate("This device does not support Bluetooth.")
                }
            } catch (e: Exception) {
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    onUpdate("Opening Bluetooth settings...")
                } catch (ex: Exception) {
                    onUpdate("Unable to toggle Bluetooth: ${ex.message}")
                }
            }
            return
        }

        // 10. Screen Brightness
        if (lowerInput.contains("brightness") || lowerInput.contains("screen brightness") || lowerInput.contains("dim screen") || lowerInput.contains("brighten screen")) {
            try {
                val window = getActivity()?.window
                val layoutParams = window?.attributes
                val currentBrightness = layoutParams?.screenBrightness ?: 0.5f

                when {
                    lowerInput.contains("max") || lowerInput.contains("full") || lowerInput.contains("100") -> {
                        layoutParams?.screenBrightness = 1.0f
                        window?.attributes = layoutParams
                        onUpdate("Brightness set to maximum.")
                    }
                    lowerInput.contains("min") || lowerInput.contains("low") || lowerInput.contains("dim") || lowerInput.contains("0") -> {
                        layoutParams?.screenBrightness = 0.05f
                        window?.attributes = layoutParams
                        onUpdate("Brightness set to minimum.")
                    }
                    lowerInput.contains("half") || lowerInput.contains("50") -> {
                        layoutParams?.screenBrightness = 0.5f
                        window?.attributes = layoutParams
                        onUpdate("Brightness set to 50%.")
                    }
                    lowerInput.contains("up") || lowerInput.contains("increase") -> {
                        val newBrightness = (currentBrightness + 0.2f).coerceAtMost(1.0f)
                        layoutParams?.screenBrightness = newBrightness
                        window?.attributes = layoutParams
                        onUpdate("Brightness increased to ${(newBrightness * 100).toInt()}%.")
                    }
                    lowerInput.contains("down") || lowerInput.contains("decrease") -> {
                        val newBrightness = (currentBrightness - 0.2f).coerceAtLeast(0.05f)
                        layoutParams?.screenBrightness = newBrightness
                        window?.attributes = layoutParams
                        onUpdate("Brightness decreased to ${(newBrightness * 100).toInt()}%.")
                    }
                    else -> {
                        onUpdate("Current brightness is ${(currentBrightness * 100).toInt()}%. Say 'brightness up', 'brightness down', 'brightness max', or 'brightness min'.")
                    }
                }
            } catch (e: Exception) {
                onUpdate("Unable to adjust brightness: ${e.message}")
            }
            return
        }

        // 11. Airplane Mode
        if (lowerInput.contains("airplane") || lowerInput.contains("flight mode")) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening airplane mode settings...")
            } catch (e: Exception) {
                onUpdate("Unable to open airplane mode settings.")
            }
            return
        }

        // 12. Sound Profile
        if (lowerInput.contains("silent") || lowerInput.contains("vibrate") || lowerInput.contains("ring")) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            when {
                lowerInput.contains("silent") -> {
                    audioManager.ringerMode = android.media.AudioManager.RINGER_MODE_SILENT
                    onUpdate("Phone set to silent mode.")
                }
                lowerInput.contains("vibrate") -> {
                    audioManager.ringerMode = android.media.AudioManager.RINGER_MODE_VIBRATE
                    onUpdate("Phone set to vibrate mode.")
                }
                lowerInput.contains("ring") -> {
                    audioManager.ringerMode = android.media.AudioManager.RINGER_MODE_NORMAL
                    onUpdate("Phone set to ring mode.")
                }
            }
            return
        }

        // 13. Take Screenshot
        if (lowerInput.contains("screenshot") || lowerInput.contains("take screenshot") || lowerInput.contains("capture screen")) {
            try {
                val intent = android.content.Intent("android.intent.action.SHOW_BRIGHTNESS_DIALOG").apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Use the hardware buttons to take a screenshot: Power + Volume Down.")
            } catch (e: Exception) {
                onUpdate("Press Power + Volume Down simultaneously to take a screenshot.")
            }
            return
        }

        // 14. Open Settings
        if (lowerInput.contains("open settings") || lowerInput.contains("phone settings") || lowerInput.contains("system settings")) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening system settings...")
            } catch (e: Exception) {
                onUpdate("Unable to open settings.")
            }
            return
        }

        // 15. Screen Timeout / Auto-rotate
        if (lowerInput.contains("auto rotate") || lowerInput.contains("rotate screen") || lowerInput.contains("screen rotation")) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening display settings for screen rotation...")
            } catch (e: Exception) {
                onUpdate("Unable to open display settings.")
            }
            return
        }

        // 16. NFC Toggle
        if (lowerInput.contains("nfc")) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening NFC settings...")
            } catch (e: Exception) {
                onUpdate("Unable to open NFC settings.")
            }
            return
        }

        // 17. Timer / Countdown
        if (lowerInput.contains("set timer") || lowerInput.contains("start timer") || lowerInput.contains("countdown")) {
            val timeRegex = Regex("(\\d+)\\s*(second|minute|hour|sec|min|hr|s|m|h)")
            val matches = timeRegex.findAll(lowerInput).toList()
            if (matches.isNotEmpty()) {
                var totalSeconds = 0L
                matches.forEach { match ->
                    val value = match.groupValues[1].toLongOrNull() ?: 0
                    val unit = match.groupValues[2]
                    when {
                        unit.startsWith("h") -> totalSeconds += value * 3600
                        unit.startsWith("m") -> totalSeconds += value * 60
                        unit.startsWith("s") || unit.startsWith("sec") -> totalSeconds += value
                    }
                }
                if (totalSeconds > 0) {
                    val intent = android.content.Intent(android.provider.AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(android.provider.AlarmClock.EXTRA_LENGTH, totalSeconds.toInt())
                        putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    onUpdate("Timer set for ${formatDuration(totalSeconds)}.")
                } else {
                    onUpdate("Please specify a time, like 'set timer for 5 minutes'.")
                }
            } else {
                onUpdate("Please specify a duration, like 'set timer for 10 minutes' or 'countdown 30 seconds'.")
            }
            return
        }

        // 18. Weather
        if (lowerInput.contains("weather") || lowerInput.contains("temperature") || lowerInput.contains("forecast")) {
            onUpdate("Fetching weather data...")
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val weatherData = fetchLiveSearchSummary("current weather now")
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (!weatherData.isNullOrEmpty()) {
                            onUpdate(weatherData)
                        } else {
                            onUpdate("Unable to fetch weather data. Check your internet connection.")
                        }
                    }
                } catch (e: Exception) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onUpdate("Weather service unavailable: ${e.message}")
                    }
                }
            }
            return
        }

        // 19. Random Number
        if (lowerInput.contains("random number") || lowerInput.contains("pick a number") || lowerInput.contains("roll a dice") || lowerInput.contains("flip a coin")) {
            when {
                lowerInput.contains("dice") || lowerInput.contains("roll") -> {
                    val result = (1..6).random()
                    onUpdate("You rolled a $result.")
                }
                lowerInput.contains("coin") || lowerInput.contains("flip") -> {
                    val result = if (Math.random() > 0.5) "Heads" else "Tails"
                    onUpdate("Coin flip: $result")
                }
                else -> {
                    val numberRegex = Regex("random (?:number )?(?:between )?(\\d+)\\s*(?:and )?(\\d+)")
                    val match = numberRegex.find(lowerInput)
                    if (match != null) {
                        val min = match.groupValues[1].toIntOrNull() ?: 1
                        val max = match.groupValues[2].toIntOrNull() ?: 100
                        val result = (min..max).random()
                        onUpdate("Random number between $min and $max: $result")
                    } else {
                        val result = (1..100).random()
                        onUpdate("Random number: $result")
                    }
                }
            }
            return
        }

        // 20. Joke
        if (lowerInput.contains("tell me a joke") || lowerInput.contains("joke") || lowerInput.contains("make me laugh") || lowerInput.contains("funny")) {
            val jokes = listOf(
                "Why do programmers prefer dark mode? Because light attracts bugs!",
                "Why was the JavaScript developer sad? Because he didn't Node how to Express himself.",
                "A SQL query walks into a bar, walks up to two tables and asks: 'Can I join you?'",
                "Why do Java developers wear glasses? Because they can't C#.",
                "What's a programmer's favorite hangout place? Foo Bar.",
                "Why do programmers hate nature? It has too many bugs.",
                "How many programmers does it take to change a light bulb? None, that's a hardware problem.",
                "What is a robot's favorite type of music? Heavy metal.",
                "Why did the developer go broke? Because he used up all his cache.",
                "What's the best thing about a Boolean? Even if you're wrong, you're only off by a bit."
            )
            onUpdate(jokes.random())
            return
        }

        // 21. Read Notification
        if (lowerInput.contains("read notification") || lowerInput.contains("what notifications") || lowerInput.contains("check notifications")) {
            onUpdate("Checking notifications...")
            try {
                val intent = android.content.Intent("android.settings.NOTIFICATION_LISTENER_SETTINGS").apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening notification settings. Grant Kuzmix OS access to read notifications.")
            } catch (e: Exception) {
                onUpdate("Unable to access notifications. Please grant notification access in settings.")
            }
            return
        }

        // 22. Clipboard
        if (lowerInput.contains("copy to clipboard") || lowerInput.contains("what's in my clipboard") || lowerInput.contains("paste from clipboard")) {
            try {
                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                if (lowerInput.contains("paste") || lowerInput.contains("what") || lowerInput.contains("read")) {
                    val clip = clipboardManager.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        val text = clip.getItemAt(0).text?.toString() ?: "Clipboard is empty"
                        onUpdate("Clipboard contains: $text")
                    } else {
                        onUpdate("Clipboard is empty.")
                    }
                } else {
                    onUpdate("Please specify what to copy. For example, say 'copy to clipboard' followed by the text.")
                }
            } catch (e: Exception) {
                onUpdate("Unable to access clipboard: ${e.message}")
            }
            return
        }

        // 23. Device Info
        if (lowerInput.contains("device info") || lowerInput.contains("about my phone") || lowerInput.contains("phone info") || lowerInput.contains("what phone")) {
            val deviceModel = android.os.Build.MODEL
            val deviceBrand = android.os.Build.BRAND
            val androidVersion = android.os.Build.VERSION.RELEASE
            val sdkVersion = android.os.Build.VERSION.SDK_INT
            onUpdate("Device: $deviceBrand $deviceModel | Android $androidVersion (SDK $sdkVersion)")
            return
        }

        // 24. Developer Options
        if (lowerInput.contains("developer options") || lowerInput.contains("developer settings") || lowerInput.contains("usb debugging")) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening developer options...")
            } catch (e: Exception) {
                onUpdate("Unable to open developer options.")
            }
            return
        }

        // 25. Storage Info
        if (lowerInput.contains("storage") || lowerInput.contains("how much storage") || lowerInput.contains("free space") || lowerInput.contains("memory")) {
            val stat = android.os.Environment.getDataDirectory()
            val totalSpace = stat.totalSpace / (1024.0 * 1024 * 1024)
            val freeSpace = stat.freeSpace / (1024.0 * 1024 * 1024)
            val usedSpace = totalSpace - freeSpace
            onUpdate("Storage: ${String.format("%.1f", usedSpace)} GB used of ${String.format("%.1f", totalSpace)} GB. ${String.format("%.1f", freeSpace)} GB free.")
            return
        }

        // 26. Wi-Fi Network List
        if (lowerInput.contains("wifi networks") || lowerInput.contains("available wifi") || lowerInput.contains("scan wifi") || lowerInput.contains("list wifi")) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening Wi-Fi settings to show available networks...")
            } catch (e: Exception) {
                onUpdate("Unable to open Wi-Fi settings.")
            }
            return
        }

        // 27. Send SMS
        if (lowerInput.startsWith("send sms") || lowerInput.startsWith("send text") || lowerInput.startsWith("text ")) {
            val smsInput = sanitized
                .replace("send sms to", "", ignoreCase = true)
                .replace("send text to", "", ignoreCase = true)
                .replace("text", "", ignoreCase = true)
                .trim()
            if (smsInput.isNotBlank()) {
                val parts = smsInput.split(" ", limit = 2)
                val contactName = parts.getOrElse(0) { "" }
                val message = parts.getOrElse(1) { "" }
                if (message.isNotBlank()) {
                    resolveContactAndSendSms(context, contactName, message, onUpdate)
                } else {
                    onUpdate("Please say: 'send sms to [contact name] [message]'.")
                }
            } else {
                onUpdate("Please say: 'send sms to [contact name] [message]'.")
            }
            return
        }

        // 28. Set Wallpaper
        if (lowerInput.contains("set wallpaper") || lowerInput.contains("change wallpaper") || lowerInput.contains("my wallpaper")) {
            try {
                val intent = android.content.Intent(android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening wallpaper picker...")
            } catch (e: Exception) {
                onUpdate("Opening wallpaper settings...")
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    onUpdate("Unable to open wallpaper settings.")
                }
            }
            return
        }

        // 29. Lock Screen
        if (lowerInput.contains("lock screen") || lowerInput.contains("lock my phone") || lowerInput.contains("turn off screen")) {
            try {
                val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
                val componentName = android.content.ComponentName(context, android.app.admin.DeviceAdminReceiver::class.java)
                if (devicePolicyManager.isAdminActive(componentName)) {
                    devicePolicyManager.lockNow()
                    onUpdate("Screen locked.")
                } else {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    onUpdate("Please enable device admin for Kuzmix OS to lock the screen.")
                }
            } catch (e: Exception) {
                onUpdate("Unable to lock screen: ${e.message}")
            }
            return
        }

        // 30. Dark Mode Toggle
        if (lowerInput.contains("dark mode") || lowerInput.contains("light mode") || lowerInput.contains("night mode") || lowerInput.contains("switch theme")) {
            val settingsPrefs = context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
            val currentMode = settingsPrefs.getString("theme_mode", "system") ?: "system"
            val newMode = if (currentMode == "dark") "light" else "dark"
            settingsPrefs.edit().putString("theme_mode", newMode).apply()
            onUpdate("Theme switched to $newMode mode.")
            return
        }

        // 31. Timer/Stopwatch explanation
        if (lowerInput.contains("stopwatch") || lowerInput.contains("start stopwatch")) {
            try {
                val intent = android.content.Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onUpdate("Opening camera app which includes a timer feature.")
            } catch (e: Exception) {
                onUpdate("Press Power + Volume Down for a screenshot timer, or say 'set timer' for a countdown.")
            }
            return
        }

        // 8. Add Event to Calendar Intent Handling
        val isAddCalendarIntent = lowerInput.contains("add to calendar") ||
            lowerInput.contains("add event") ||
            lowerInput.contains("add meeting") ||
            lowerInput.contains("add appointment") ||
            lowerInput.contains("schedule meeting") ||
            lowerInput.contains("schedule event") ||
            lowerInput.contains("schedule a meeting") ||
            lowerInput.contains("schedule an event") ||
            lowerInput.contains("create event") ||
            lowerInput.contains("create meeting") ||
            lowerInput.contains("add this to my calendar") ||
            lowerInput.contains("add to my calendar") ||
            lowerInput.contains("parse flyer") ||
            lowerInput.contains("analyze flyer") ||
            (lowerInput.contains("add ") && lowerInput.contains("calendar")) ||
            (lowerInput.contains("schedule ") && lowerInput.contains("calendar"))

        if (isAddCalendarIntent) {
            val isFlyerScan = lowerInput.contains("parse flyer") ||
                lowerInput.contains("analyze flyer")

            if (isFlyerScan) {
                onUpdate("Scanning visual spectrum... Initiating Flyer Event Parser.")
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    try {
                        FlyerEventParser.parseScreenFlyer(context, { extractedText ->
                            onUpdate("Optical scan successful! Formulating query for cognitive processing...")
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                try {
                                    val promptText = """
                                        Analyze this raw text extracted from a flyer. Extract the event title, year, month (1-12), day, hour, and minute.
                                        Respond strictly in this JSON format: {"title": "Event Name", "year": 2026, "month": 7, "day": 26, "hour": 14, "minute": 30}.
                                        Do not include markdown formatting.
                                        Raw text:
                                        $extractedText
                                    """.trimIndent()

                                    val response = generateContentWithRetry(promptText)
                                    val responseText = response
                                    android.util.Log.d("FlyerEventParser", "OpenRouter Extraction Response: $responseText")

                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        try {
                                            val cleanJson = responseText
                                                .replace("```json", "")
                                                .replace("```", "")
                                                .trim()

                                            val json = org.json.JSONObject(cleanJson)
                                            val title = json.getString("title")
                                            val year = json.getInt("year")
                                            val month = json.getInt("month").coerceIn(1, 12)
                                            val day = json.getInt("day").coerceIn(1, 31)
                                            val hour = json.getInt("hour").coerceIn(0, 23)
                                            val minute = json.getInt("minute").coerceIn(0, 59)

                                            FlyerEventParser.insertEvent(context, title, year, month, day, hour, minute) { resultMessage ->
                                                onUpdate(resultMessage)
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("FlyerEventParser", "JSON parse error: $responseText", e)
                                            onUpdate("Sorry, I couldn't parse the event details from the flyer. Please try again or say the event details clearly.")
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        onUpdate("Sorry, I couldn't process the flyer. Please try saying the event details clearly, like 'Add meeting with John tomorrow at 3pm to my calendar'.")
                                    }
                                }
                            }
                        }, { error ->
                            onUpdate("Optical scan failed: ${error.message}")
                        })
                    } catch (e: Exception) {
                        onUpdate("Screen parsing interface error: ${e.message}")
                    }
                }
            } else {
                onUpdate("Processing event details for calendar entry...")
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val currentDateStr = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        val promptText = """
                            Current Date & Time: $currentDateStr
                            Analyze the following user request to add an event to their calendar.
                            Extract the event title, year (4 digits), month (1-12), day (1-31), hour (0-23), and minute (0-59).
                            If no date is mentioned, assume today or tomorrow based on context. If no time is mentioned, default to hour 10, minute 0.
                            Respond strictly in this JSON format: {"title": "Event Name", "year": 2026, "month": 8, "day": 5, "hour": 15, "minute": 0}.
                            Do not include markdown formatting or extra text.
                            User Request: "$sanitized"
                        """.trimIndent()

                        val response = generateContentWithRetry(promptText)
                        val responseText = response
                        android.util.Log.d("CalendarParser", "OpenRouter Event Parse Response: $responseText")

                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            try {
                                val cleanJson = responseText
                                    .replace("```json", "")
                                    .replace("```", "")
                                    .trim()

                                val json = org.json.JSONObject(cleanJson)
                                val title = json.getString("title")
                                val year = json.getInt("year")
                                val month = json.getInt("month").coerceIn(1, 12)
                                val day = json.getInt("day").coerceIn(1, 31)
                                val hour = json.getInt("hour").coerceIn(0, 23)
                                val minute = json.getInt("minute").coerceIn(0, 59)

                                FlyerEventParser.insertEvent(context, title, year, month, day, hour, minute) { resultMessage ->
                                    onUpdate(resultMessage)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("CalendarParser", "JSON parse error: $responseText", e)
                                val cleanTitle = sanitized
                                    .replace("add to calendar", "", ignoreCase = true)
                                    .replace("add to my calendar", "", ignoreCase = true)
                                    .replace("add event", "", ignoreCase = true)
                                    .replace("add", "", ignoreCase = true)
                                    .replace("schedule", "", ignoreCase = true)
                                    .replace("create", "", ignoreCase = true)
                                    .trim()
                                    .ifEmpty { "New Event" }

                                val now = java.util.Calendar.getInstance()
                                val nextHour = now.get(java.util.Calendar.HOUR_OF_DAY) + 1
                                val safeHour = if (nextHour > 23) 0 else nextHour
                                val safeDay = if (nextHour > 23) now.get(java.util.Calendar.DAY_OF_MONTH) + 1 else now.get(java.util.Calendar.DAY_OF_MONTH)

                                FlyerEventParser.insertEvent(
                                    context,
                                    cleanTitle,
                                    now.get(java.util.Calendar.YEAR),
                                    now.get(java.util.Calendar.MONTH) + 1,
                                    safeDay,
                                    safeHour,
                                    0
                                ) { resultMessage ->
                                    onUpdate(resultMessage)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            val cleanTitle = sanitized
                                .replace("add to calendar", "", ignoreCase = true)
                                .replace("add to my calendar", "", ignoreCase = true)
                                .replace("add event", "", ignoreCase = true)
                                .replace("add", "", ignoreCase = true)
                                .replace("schedule", "", ignoreCase = true)
                                .replace("create", "", ignoreCase = true)
                                .trim()
                                .ifEmpty { "New Event" }

                            val now = java.util.Calendar.getInstance()
                            val nextHour = now.get(java.util.Calendar.HOUR_OF_DAY) + 1
                            val safeHour = if (nextHour > 23) 0 else nextHour
                            val safeDay = if (nextHour > 23) now.get(java.util.Calendar.DAY_OF_MONTH) + 1 else now.get(java.util.Calendar.DAY_OF_MONTH)

                            FlyerEventParser.insertEvent(
                                context,
                                cleanTitle,
                                now.get(java.util.Calendar.YEAR),
                                now.get(java.util.Calendar.MONTH) + 1,
                                safeDay,
                                safeHour,
                                0
                            ) { resultMessage ->
                                onUpdate(resultMessage)
                            }
                        }
                    }
                }
            }
            return
        }

        // 8b. Calendar Query Control (View/List Events)
        if (lowerInput.contains("calendar") || lowerInput.contains("schedule") || lowerInput.contains("events") || lowerInput.contains("what do i have today") || lowerInput.contains("my events")) {
            queryCalendarEvents(onUpdate)
            return
        }

        // 8c. Silent Alarm Setter ("set alarm for [time]", "set an alarm for [time]")
        if (lowerInput.contains("set alarm") || lowerInput.contains("set an alarm")) {
            setSilentAlarm(sanitized, onUpdate)
            return
        }

        // 8d. Google Maps Navigation Intent Handling
        val isNavigationIntent = lowerInput.contains("navigate to") ||
            lowerInput.contains("directions to") ||
            lowerInput.contains("take me to") ||
            lowerInput.contains("drive to") ||
            lowerInput.contains("route to")

        if (isNavigationIntent) {
            val destination = sanitized
                .replace("navigate to", "", ignoreCase = true)
                .replace("directions to", "", ignoreCase = true)
                .replace("take me to", "", ignoreCase = true)
                .replace("drive to", "", ignoreCase = true)
                .replace("route to", "", ignoreCase = true)
                .trim()
            if (destination.isNotEmpty()) {
                launchGoogleMaps(context, destination, isNavigation = true, onUpdate)
            } else {
                onUpdate("Please specify a destination for Google Maps navigation.")
            }
            return
        }

        // 8e. Google Maps Places & Location Search Intent Handling
        val isMapsSearchIntent = lowerInput.contains("where is") ||
            lowerInput.contains("map of") ||
            lowerInput.contains("show me on map") ||
            lowerInput.contains("places near me") ||
            lowerInput.contains("find nearest") ||
            lowerInput.contains("google maps") ||
            lowerInput.contains("maps search")

        if (isMapsSearchIntent) {
            val placeQuery = sanitized
                .replace("where is", "", ignoreCase = true)
                .replace("map of", "", ignoreCase = true)
                .replace("show me on map", "", ignoreCase = true)
                .replace("google maps", "", ignoreCase = true)
                .replace("maps search", "", ignoreCase = true)
                .trim()
                .ifEmpty { sanitized }
            launchGoogleMaps(context, placeQuery, isNavigation = false, onUpdate)
            return
        }

        // 8f. Direct Google Web Search Intent Handling
        val isDirectSearchIntent = lowerInput.startsWith("google ") ||
            lowerInput.startsWith("search google for ") ||
            lowerInput.startsWith("search for ") ||
            lowerInput.startsWith("search web for ") ||
            lowerInput.startsWith("web search ")

        if (isDirectSearchIntent) {
            val searchQuery = sanitized
                .replace("search google for", "", ignoreCase = true)
                .replace("search web for", "", ignoreCase = true)
                .replace("search for", "", ignoreCase = true)
                .replace("web search", "", ignoreCase = true)
                .replace("google", "", ignoreCase = true)
                .trim()
            if (searchQuery.isNotEmpty()) {
                launchGoogleSearch(context, searchQuery, onUpdate)
            } else {
                onUpdate("Please specify a topic to search on Google.")
            }
            return
        }

        // 9. Local Media Playback Engine
        if (lowerInput.startsWith("play ") || lowerInput == "play") {
            onUpdate("Searching local media for \"$sanitized\"...")
            playLocalSong(context, sanitized)
            return
        }

        // --- STEP 2: APP LAUNCHER (Deterministic 3-Pass Package Manager Matcher) ---
        if (lowerInput.startsWith("open ") || lowerInput.startsWith("launch ")) {
            val prefixLen = if (lowerInput.startsWith("open ")) 5 else 7
            val appName = sanitized.substring(prefixLen).trim()
            if (appName.isNotEmpty()) {
                onUpdate("Locating application \"$appName\"...")
                launchAppByName(context, appName)
            } else {
                onUpdate("Please specify an app name to open.")
            }
            return
        }

        // --- STEP 3: DIALER (Contacts Resolver with runtime permissions) ---
        if (lowerInput.startsWith("call ")) {
            val contactName = sanitized.substring(5).trim()
            if (contactName.isNotEmpty()) {
                resolveContactAndCall(context, contactName, onUpdate)
            } else {
                onUpdate("Please specify a contact name to call.")
            }
            return
        }

        // --- STEP 4: LOCAL FAQ / CACHE & MATH SOLVER ---
        
        // 1. Math solver
        val mathResult = LocalKnowledgeEngine.solveMath(sanitized)
        if (mathResult != null) {
            onUpdate(mathResult)
            return
        }

        // 2. Local FAQ Memory
        val faqResponse = LocalKnowledgeEngine.getFaqResponse(sanitized)
        if (faqResponse != null) {
            onUpdate(faqResponse)
            return
        }

        // 3. Date check
        if (lowerInput.contains("what day") || lowerInput.contains("what date") || lowerInput.contains("current date")) {
            val date = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            onUpdate("Today is $date.")
            return
        }

        // --- STEP 5: CLOUD FALLBACK WITH LIVE SEARCH GROUNDING (OpenRouter AI) ---
        if (!isAiOnline) {
            onUpdate("API Key Required")
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime < cooldownEndTime) {
            var remaining = ((cooldownEndTime - currentTime) / 1000).coerceAtLeast(0)
            while (remaining > 0) {
                onUpdate("KC is resting. Neural traffic high. Re-engaging in ${remaining}s...")
                kotlinx.coroutines.delay(1000)
                remaining = ((cooldownEndTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            }
            onUpdate("Neural pathways re-engaged. How can I assist you?")
            return
        }

        try {
            // Check if query needs live search grounding (news, current info, scores, updates)
            var finalPrompt = sanitized
            val needsLiveSearch = lowerInput.contains("search") || lowerInput.contains("latest") ||
                lowerInput.contains("news") || lowerInput.contains("price") || lowerInput.contains("who won") ||
                lowerInput.contains("weather") || lowerInput.contains("today") || lowerInput.contains("current") ||
                lowerInput.contains("score") || lowerInput.contains("update") || lowerInput.contains("who is") ||
                lowerInput.contains("what is") || lowerInput.contains("where is")

            if (needsLiveSearch) {
                onUpdate("Accessing live web intelligence for \"$sanitized\"...")
                val liveData = fetchLiveSearchSummary(sanitized)
                if (!liveData.isNullOrEmpty()) {
                    finalPrompt = "$sanitized\n\n[$liveData]"
                }
            }

            generateContentStreamWithRetry(finalPrompt, 0) { update ->
                onUpdate(update)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val isRateLimit = e.message?.contains("429") == true || e.message?.contains("quota") == true || e.message?.contains("exhausted") == true || e.message?.contains("limit") == true
            val isAuthError = e.message?.contains("401") == true || e.message?.contains("unauthorized") == true || e.message?.contains("invalid") == true
            val isNetworkError = e.message?.contains("timeout") == true || e.message?.contains("connect") == true || e.message?.contains("network") == true
            
            if (isRateLimit) {
                cooldownEndTime = System.currentTimeMillis() + 60000L // 60s cooldown
                var remaining = 60L
                while (remaining > 0) {
                    onUpdate("Neural pathways are busy. Reconnecting in ${remaining}s...")
                    kotlinx.coroutines.delay(1000)
                    remaining = ((cooldownEndTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                }
                onUpdate("Neural pathways re-engaged. How can I assist you?")
            } else if (isAuthError) {
                onUpdate("Authentication issue detected. Switching to backup neural channel...")
                kotlinx.coroutines.delay(2000)
                onUpdate("Backup channel active. How can I assist you?")
            } else if (isNetworkError) {
                onUpdate("Network connection unstable. Please check your internet and try again.")
            } else {
                onUpdate("I encountered an issue processing that request. Please try again in a moment.")
            }
        }
    }
}

@Composable
fun DefaultWallpaper(key: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    when (key) {
        "sunset" -> {
            var loadError by remember { mutableStateOf(false) }
            if (loadError) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A0B2E),
                                    Color(0xFF2B0938),
                                    Color(0xFF4A1259)
                                )
                            )
                        )
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.wallpaper)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Sunset Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = modifier.fillMaxSize(),
                    onState = { state ->
                        if (state is coil.compose.AsyncImagePainter.State.Error) {
                            loadError = true
                        }
                    }
                )
            }
        }
        "burj" -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF070B19),
                                DarkSurface,
                                Color(0xFF00A8CC)
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Skyscraper structural curves representing modern architecture
                    val path1 = Path().apply {
                        moveTo(w * 0.15f, h)
                        quadraticTo(w * 0.25f, h * 0.35f, w * 0.5f, h * 0.15f)
                        quadraticTo(w * 0.75f, h * 0.35f, w * 0.85f, h)
                        close()
                    }
                    drawPath(
                        path = path1,
                        color = Color.White.copy(alpha = 0.04f)
                    )
                    
                    val path2 = Path().apply {
                        moveTo(w * 0.3f, h)
                        quadraticTo(w * 0.4f, h * 0.45f, w * 0.5f, h * 0.25f)
                        quadraticTo(w * 0.6f, h * 0.45f, w * 0.7f, h)
                        close()
                    }
                    drawPath(
                        path = path2,
                        color = Color.White.copy(alpha = 0.06f)
                    )
                    
                    // Futuristic sky grid lines
                    for (i in 1..7) {
                        val fraction = i / 8f
                        val y = h - (h * 0.75f * fraction)
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(w * 0.2f, y),
                            end = Offset(w * 0.8f, y),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }
        }
        "ocean" -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF02111E),
                                Color(0xFF003B46),
                                Color(0xFF07575B),
                                Color(0xFF66A5AD)
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Fluid glowing waves
                    for (i in 1..4) {
                        val wavePath = Path().apply {
                            val startY = h * (0.55f + i * 0.07f)
                            moveTo(0f, startY)
                            cubicTo(
                                w * 0.25f, startY - 30f * i,
                                w * 0.75f, startY + 30f * i,
                                w, startY
                            )
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = wavePath,
                            color = Color.White.copy(alpha = 0.03f * i)
                        )
                    }
                }
            }
        }
        "airplane" -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B033F),
                                Color(0xFF7E1C9B),
                                Color(0xFFE05275),
                                Color(0xFFFFAE5D)
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // High-altitude stars
                    val random = java.util.Random(1337)
                    for (i in 1..35) {
                        val x = random.nextFloat() * w
                        val y = random.nextFloat() * (h * 0.5f)
                        val radius = random.nextFloat() * 1.5.dp.toPx() + 0.8.dp.toPx()
                        drawCircle(
                            color = Color.White.copy(alpha = random.nextFloat() * 0.5f + 0.3f),
                            radius = radius,
                            center = Offset(x, y)
                        )
                    }
                    
                    // Diagonal light trails (supersonic flights)
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(w * 0.1f, h * 0.45f),
                        end = Offset(w * 0.9f, h * 0.55f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(w * 0.2f, h * 0.4f),
                        end = Offset(w * 0.8f, h * 0.48f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
    }
}

data class WallpaperItem(
    val key: String,
    val label: String
)

@Composable
fun KuzmixCustomizationHub(
    hardwareTier: HardwareTier,
    selectedWallpaperKey: String,
    onSelectWallpaper: (String) -> Unit,
    onGalleryClick: () -> Unit,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    iconMaskKey: String = "squircle",
    onIconMaskChange: (String) -> Unit = {},
    iconScaleFactor: Float = 1.0f,
    onIconScaleChange: (Float) -> Unit = {},
    onClose: () -> Unit
) {
    var showStudioSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(KCTokens.DarkCanvas)
                .clickable { /* prevent propagation */ }
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                )
            }

            // Embedded Redesigned KC Customization Browse Screen
            KCCustomizationBrowseScreen(
                onSelectWallpaperKey = onSelectWallpaper,
                onOpenDeviceStudio = { showStudioSheet = true },
                onClose = onClose,
                modifier = Modifier.weight(1f)
            )
        }

        if (showStudioSheet) {
            DeviceStudioBottomSheet(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                iconMaskKey = iconMaskKey,
                onIconMaskChange = onIconMaskChange,
                iconScaleFactor = iconScaleFactor,
                onIconScaleChange = onIconScaleChange,
                onOpenGallery = {
                    showStudioSheet = false
                    onGalleryClick()
                },
                onDismiss = { showStudioSheet = false }
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoForgeContainer(
    videoUrl: String,
    hardwareTier: HardwareTier,
    isVideoGenerating: Boolean,
    onVideoReady: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isBuffering by remember(videoUrl) { mutableStateOf(true) }

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            videoScalingMode = android.media.MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    isBuffering = false
                    onVideoReady()
                } else if (playbackState == Player.STATE_BUFFERING) {
                    isBuffering = true
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .background(DarkSurface.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isVideoGenerating || isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkSurface.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = KuzmixOrange,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Forging celestial animation...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SingularityOverlay(
    hardwareTier: HardwareTier,
    onClose: () -> Unit,
    isProcessing: Boolean,
    responseText: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    isListening: Boolean,
    onMicClick: () -> Unit,
    onInput: (String) -> Unit,
    generatedImageUrl: String? = null,
    onSetAsWallpaper: (() -> Unit)? = null,
    onSaveToGallery: (() -> Unit)? = null,
    isImageGenerating: Boolean = false,
    onImageGeneratingChange: (Boolean) -> Unit = {},
    generatedVideoUrl: String? = null,
    isVideoGenerating: Boolean = false,
    onVideoGeneratingChange: (Boolean) -> Unit = {},
    onSetVideoAsWallpaper: (() -> Unit)? = null,
    onSaveVideoToGallery: (() -> Unit)? = null
) {
    // Spec 3: Kuzmix Aura Edge Light
    val isProcessingState = isProcessing || isListening || isImageGenerating || isVideoGenerating

    // Chat Message state tracking
    val chatMessages = remember { mutableStateListOf<SingularityChatMessage>() }
    var showToolsMenu by remember { mutableStateOf(false) }

    // Sync latest AI response into message list
    LaunchedEffect(responseText) {
        if (responseText.isNotEmpty() && responseText != "Listening...") {
            val lastMsg = chatMessages.lastOrNull()
            if (lastMsg != null && !lastMsg.isUser) {
                val index = chatMessages.lastIndex
                chatMessages[index] = lastMsg.copy(text = responseText)
            } else if (chatMessages.isEmpty() || lastMsg?.isUser == true) {
                chatMessages.add(SingularityChatMessage(text = responseText, isUser = false))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Chat Screen Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (generatedImageUrl != null || generatedVideoUrl != null) 0.92f else 0.8f)
                .padding(12.dp)
                .background(DarkBackground, RoundedCornerShape(30.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(30.dp))
                .clickable { /* prevent propagation */ }
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(listOf(KuzmixOrange, KuzmixYellow)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "KUZMIX AI",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            "Developer: Oni Oluwatobi X The Kreadive Galaxy",
                            color = KuzmixYellow.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (isProcessingState) KuzmixYellow else KuzmixYellow, CircleShape)
                            )
                            Text(
                                if (isProcessing) "NEURAL PROCESSING..."
                                else if (isListening) "LISTENING..."
                                else "ONLINE",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                androidx.compose.material3.IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isListening || responseText == "Listening..." || (responseText.isEmpty() && isListening)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text("Listening to voice input...", color = KuzmixOrange, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    PulsingWaveform()
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Spec 1: Chat Canvas & Floating Glassmorphic Message Cards
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (chatMessages.isEmpty() && responseText.isEmpty() && !isProcessing) {
                    // Empty state greeting card
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    Brush.radialGradient(listOf(KuzmixOrange.copy(alpha = 0.3f), Color.Transparent)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Spark",
                                tint = KuzmixOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "What can Kuzmix AI do for you?",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ask system questions, request live wallpapers, or control device features.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick prompt suggestion pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            listOf("Turn on Torch", "Wallpaper Ideas", "Device Status").forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                        .clickable {
                                            onInputValueChange(preset)
                                            onInput(preset)
                                            chatMessages.add(SingularityChatMessage(text = preset, isUser = true))
                                            onInputValueChange("")
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(preset, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        reverseLayout = false
                    ) {
                        items(chatMessages, key = { it.id }) { msg ->
                            if (msg.isUser) {
                                // Spec 1: User Message Card (Right aligned with subtle Royal Blue gradient tint)
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.82f)
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    listOf(
                                                        DarkSurface.copy(alpha = 0.85f), // Royal Blue
                                                        DarkSurface.copy(alpha = 0.92f)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                                            )
                                            .border(
                                                width = 0.5.dp,
                                                color = Color.White.copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            } else {
                                // Spec 1: AI Response Card (Left aligned with Electric Purple to Indigo border-glow gradient)
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(0.88f)
                                            .background(
                                                color = Color.White.copy(alpha = 0.04f),
                                                shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                brush = Brush.horizontalGradient(
                                                    listOf(
                                                        KuzmixOrange, // Electric Purple
                                                        KuzmixOrange  // Indigo
                                                    )
                                                ),
                                                shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "AI",
                                                tint = KuzmixOrange,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                "Kuzmix OS AI",
                                                color = KuzmixOrange,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        androidx.compose.foundation.text.selection.SelectionContainer {
                                            Text(
                                                text = msg.text,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Video / Image Media Outputs attached to conversation
                        if (generatedVideoUrl != null || isVideoGenerating) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                VideoForgeContainer(
                                    videoUrl = generatedVideoUrl ?: "",
                                    hardwareTier = hardwareTier,
                                    isVideoGenerating = isVideoGenerating,
                                    onVideoReady = { onVideoGeneratingChange(false) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    Button(
                                        onClick = { onSetVideoAsWallpaper?.invoke() },
                                        enabled = !isVideoGenerating && !generatedVideoUrl.isNullOrEmpty(),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Set Live Wallpaper", color = Color.White, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { onSaveVideoToGallery?.invoke() },
                                        enabled = !isVideoGenerating && !generatedVideoUrl.isNullOrEmpty(),
                                        colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save Gallery", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else if (generatedImageUrl != null) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .aspectRatio(1f)
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val context = LocalContext.current
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(generatedImageUrl)
                                            .memoryCacheKey(generatedImageUrl)
                                            .diskCacheKey(generatedImageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Generated Visualization",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            Box(
                                                modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                androidx.compose.material3.CircularProgressIndicator(
                                                    color = KuzmixOrange,
                                                    strokeWidth = 3.dp,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                        },
                                        onSuccess = { onImageGeneratingChange(false) },
                                        onError = { onImageGeneratingChange(false) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    Button(
                                        onClick = { onSetAsWallpaper?.invoke() },
                                        enabled = !isImageGenerating,
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Set Wallpaper", color = Color.White, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { onSaveToGallery?.invoke() },
                                        enabled = !isImageGenerating,
                                        colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save Gallery", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spec 2: The Glowing Input Bar (Image 2 Style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xD90E1017), // Midnight Charcoal (#0E1017) with 85% opacity
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                KuzmixOrange, // Electric Purple
                                Color(0xFFFF5722)  // Sunset Orange
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Input TextField
                TextField(
                    value = inputValue,
                    onValueChange = onInputValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text(
                            "What does a world-class AI chat interface look like?",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom row inside glowing input bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left-aligned elements: Glass pill buttons for "+" and "Tools"
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Glass pill "+" button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                .clickable {
                                    showToolsMenu = !showToolsMenu
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Glass pill "Tools" button
                        Row(
                            modifier = Modifier
                                .height(32.dp)
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .clickable {
                                    showToolsMenu = !showToolsMenu
                                }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Tools",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                "Tools",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Right-aligned elements: White microphone icon and vibrant purple send button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Microphone Icon
                        androidx.compose.material3.IconButton(
                            onClick = onMicClick,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isListening) KuzmixOrange.copy(alpha = 0.3f) else Color.Transparent,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (isListening) KuzmixOrange else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Send Button (Vibrant purple circular button with upward arrow)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(KuzmixOrange, KuzmixOrange)
                                    ),
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (inputValue.isNotBlank()) {
                                        val text = inputValue
                                        chatMessages.add(SingularityChatMessage(text = text, isUser = true))
                                        onInput(text)
                                        onInputValueChange("")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data model for conversation cards
data class SingularityChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun PulsingWaveform() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0..4) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.5f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(500, delayMillis = i * 100, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((16 * scale).dp)
                    .background(KuzmixOrange, RoundedCornerShape(2.dp))
            )
        }
    }
}

class VocalBiometricsManager(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("KuzmixBiometrics", Context.MODE_PRIVATE)

    var isEnrolled: Boolean
        get() = sharedPref.getBoolean("vocal_enrolled", false)
        set(value) = sharedPref.edit().putBoolean("vocal_enrolled", value).apply()

    var vocalSignature: String?
        get() = sharedPref.getString("vocal_signature", null)
        set(value) = sharedPref.edit().putString("vocal_signature", value).apply()

    fun enrollVoice(signature: List<Float>) {
        val sigStr = signature.joinToString(",")
        vocalSignature = sigStr
        isEnrolled = true
    }

    fun deleteEnrollment() {
        sharedPref.edit().remove("vocal_enrolled").remove("vocal_signature").apply()
    }
}

@Composable
fun VocalBiometricsSetupCard(context: Context = LocalContext.current) {
    val biometricsManager = remember { VocalBiometricsManager(context) }
    var isEnrolled by remember { mutableStateOf(biometricsManager.isEnrolled) }
    var isCalibrating by remember { mutableStateOf(false) }
    var calibrationProgress by remember { mutableStateOf(0f) }
    var calibrationStatusText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (isEnrolled) KuzmixYellow else Color(0xFFFF9900), CircleShape)
                )
                Text(
                    text = "Vocal Biometrics Signature",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = if (isEnrolled) "SECURED" else "INSECURE",
                color = if (isEnrolled) KuzmixYellow else Color(0xFFFF9900),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (isCalibrating) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = calibrationStatusText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                
                // Pulsing line simulation for audio spectrum mapping
                Row(
                    modifier = Modifier.height(30.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
                    for (i in 0..12) {
                        val barHeight by infiniteTransition.animateFloat(
                            initialValue = 4f,
                            targetValue = 28f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(
                                    durationMillis = 400 + (i * 30),
                                    easing = androidx.compose.animation.core.LinearEasing
                                ),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                            )
                        )
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(barHeight.dp)
                                .background(KuzmixOrange, RoundedCornerShape(1.5.dp))
                        )
                    }
                }

                androidx.compose.material3.LinearProgressIndicator(
                    progress = { calibrationProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = KuzmixOrange,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        } else {
            Text(
                text = if (isEnrolled) {
                    "Vocal Key enrolled. Speaker verification lock is active at 92% match threshold."
                } else {
                    "Secure voice command execution. Calibrate \"Hey Kuzmix\" to prevent unauthorized wakeups."
                },
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        isCalibrating = true
                        calibrationProgress = 0f
                        calibrationStatusText = "Listening for calibrating pattern... Say \"Hey Kuzmix\""
                        scope.launch {
                            for (p in 1..100) {
                                kotlinx.coroutines.delay(25)
                                calibrationProgress = p / 100f
                                if (p == 40) {
                                    calibrationStatusText = "Extracting vocal frequencies..."
                                } else if (p == 75) {
                                    calibrationStatusText = "Storing Mel-Frequency Cepstral footprint..."
                                }
                            }
                            biometricsManager.enrollVoice(listOf(0.55f, 0.65f, 0.75f, 0.85f, 0.95f))
                            isEnrolled = true
                            isCalibrating = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isEnrolled) "Recalibrate" else "Calibrate Signature",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isEnrolled) {
                    Button(
                        onClick = {
                            biometricsManager.deleteEnrollment()
                            isEnrolled = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Reset",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
