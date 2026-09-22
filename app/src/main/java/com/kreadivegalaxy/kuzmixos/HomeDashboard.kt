package com.kreadivegalaxy.kuzmixos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixWhite
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkSurface
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkCard
import com.kreadivegalaxy.kuzmixos.ui.theme.GlassTextColor
import com.kreadivegalaxy.kuzmixos.ui.theme.GlassSubTextColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import android.content.Context
import java.util.Date
import java.util.Locale
import com.kreadivegalaxy.kuzmixos.generateVideoWithOpenRouter
import com.kreadivegalaxy.kuzmixos.OpenRouterKeyManager

data class AppGridPosition(val row: Int, val col: Int, val label: String, val packageName: String)

fun safeGetSystemDrawable(context: Context, resId: Int): Drawable {
    return try {
        androidx.core.content.ContextCompat.getDrawable(context, resId)
    } catch (_: Throwable) {
        null
    } ?: android.graphics.drawable.ColorDrawable(android.graphics.Color.rgb(44, 44, 46))
}

fun Drawable?.toImageBitmap(): ImageBitmap {
    if (this == null) {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    return try {
        if (this is BitmapDrawable && this.bitmap != null && !this.bitmap.isRecycled && this.bitmap.config != Bitmap.Config.HARDWARE) {
            this.bitmap.asImageBitmap()
        } else {
            val w = intrinsicWidth.takeIf { it in 1..1024 } ?: 96
            val h = intrinsicHeight.takeIf { it in 1..1024 } ?: 96
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bitmap.asImageBitmap()
        }
    } catch (e: Throwable) {
        try {
            Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888).asImageBitmap()
        } catch (_: Throwable) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap()
        }
    }
}

fun getDockApps(context: Context, allApps: List<AppItem>): List<AppItem> {
    val pm = context.packageManager
    
    // 1. Phone (Dialer)
    val dialerIntent = android.content.Intent(android.content.Intent.ACTION_DIAL)
    val dialerPackage = pm.resolveActivity(dialerIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    
    // 2. Messages (SMS)
    val smsIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:"))
    val smsPackage = pm.resolveActivity(smsIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    
    // 3. Camera
    val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
    val cameraPackage = pm.resolveActivity(cameraIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    
    // 4. Play Store (Market)
    val storeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
        addCategory(android.content.Intent.CATEGORY_APP_MARKET)
    }
    val storePackage = pm.resolveActivity(storeIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName

    val dockApps = mutableListOf<AppItem>()
    val matchedPackages = mutableSetOf<String>()

    fun addBestMatch(pkgName: String?, keywords: List<String>, fallbackLabel: String, fallbackIntent: android.content.Intent) {
        var found = if (pkgName != null) allApps.firstOrNull { it.packageName == pkgName && !matchedPackages.contains(it.packageName) } else null
        
        if (found == null) {
            found = allApps.firstOrNull { app ->
                !matchedPackages.contains(app.packageName) && keywords.any { kw -> 
                    app.packageName.lowercase().contains(kw) || app.label.lowercase().contains(kw) 
                }
            }
        }
        
        if (found != null) {
            dockApps.add(found)
            matchedPackages.add(found.packageName)
        } else {
            val fallbackDrawable: Drawable = safeGetSystemDrawable(
                context,
                when (fallbackLabel.lowercase()) {
                    "phone" -> android.R.drawable.ic_menu_call
                    "messages" -> android.R.drawable.ic_menu_send
                    "camera" -> android.R.drawable.ic_menu_camera
                    else -> android.R.drawable.ic_menu_compass
                }
            )
val fallbackApp = AppItem(
                label = fallbackLabel,
                packageName = "com.aistudio.fallback.${fallbackLabel.lowercase()}",
                icon = fallbackDrawable,
                intent = fallbackIntent
            )
            dockApps.add(fallbackApp)
        }
    }

    addBestMatch(dialerPackage, listOf("dialer", "phone"), "Phone", dialerIntent)
    addBestMatch(smsPackage, listOf("messaging", "message", "sms", "mms"), "Messages", smsIntent)
    addBestMatch(cameraPackage, listOf("camera"), "Camera", cameraIntent)
    addBestMatch(storePackage, listOf("vending", "play", "store", "market"), "Play Store", storeIntent)

    return dockApps.take(4)
}

@Composable
fun HomeDashboard(
    hardwareTier: HardwareTier,
    appLoader: AppLoader,
    themeMode: String = "system",
    onThemeModeChange: (String) -> Unit = {},
    onLockOS: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val sensorState = rememberAeroGlassSensorState(context)
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    val isSupernovaMode by remember { mutableStateOf(hardwareTier == HardwareTier.TITAN) }

    // Disable slide animations on NOVA devices to prevent lag
    val slideEnter: EnterTransition = if (hardwareTier == HardwareTier.TITAN) slideInVertically { it } else { EnterTransition.None }
    val slideExit: ExitTransition = if (hardwareTier == HardwareTier.TITAN) slideOutVertically { it } else { ExitTransition.None }
    val slideEnterReverse: EnterTransition = if (hardwareTier == HardwareTier.TITAN) slideInVertically { -it } else { EnterTransition.None }
    val slideExitReverse: ExitTransition = if (hardwareTier == HardwareTier.TITAN) slideOutVertically { -it } else { ExitTransition.None }
var showAppLibrary by remember { mutableStateOf(false) }
    var showControlCenter by remember { mutableStateOf(false) }
    var showWallpaperHub by remember { mutableStateOf(false) }
    var showSingularityOverlay by remember { mutableStateOf(false) }
    var showQrScannerOverlay by remember { mutableStateOf(false) }
    var showVoiceSettingsOverlay by remember { mutableStateOf(false) }
    var showUtilitiesOverlay by remember { mutableStateOf(false) }
    var showSupportOverlay by remember { mutableStateOf(false) }
    var showStudioOverlay by remember { mutableStateOf(false) }
    var showDiagnosticsOverlay by remember { mutableStateOf(false) }
LaunchedEffect(showAppLibrary) {
        if (showAppLibrary) {
            try {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(showControlCenter) {
        if (showControlCenter) {
            try {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(showWallpaperHub) {
        if (showWallpaperHub) {
            try {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(showSingularityOverlay) {
        if (showSingularityOverlay) {
            try {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(showStudioOverlay) {
        if (showStudioOverlay) {
            try {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (_: Exception) {}
        }
    }
    val settingsPrefs = remember { context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE) }
    var showWakeWordOnboarding by remember {
        mutableStateOf(!settingsPrefs.getBoolean("has_seen_wake_word_onboarding", false))
    }
    var isAiProcessing by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf("") }
    var biometricNotification by remember { mutableStateOf<String?>(null) }

    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            val ttsEngine = android.speech.tts.TextToSpeech(context) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    // Initialized successfully
                }
            }
            tts = ttsEngine
        } catch (e: Throwable) {
            android.util.Log.w("KuzmixOS", "TTS engine init failed: ${e.message}")
        }
}

    LaunchedEffect(Unit) {
        try {
            val prefs = context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("battery_opt_prompted", true).apply()
} catch (_: Exception) {}
    }

    DisposableEffect(tts) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    if (biometricNotification != null) {
        LaunchedEffect(biometricNotification) {
            delay(2500)
            biometricNotification = null
        }
    }

    androidx.activity.compose.BackHandler(enabled = true) {
        when {
            showAppLibrary -> showAppLibrary = false
            showControlCenter -> showControlCenter = false
            showWallpaperHub -> showWallpaperHub = false
            showSingularityOverlay -> showSingularityOverlay = false
            showQrScannerOverlay -> showQrScannerOverlay = false
            showVoiceSettingsOverlay -> showVoiceSettingsOverlay = false
            showUtilitiesOverlay -> showUtilitiesOverlay = false
            showSupportOverlay -> showSupportOverlay = false
            showStudioOverlay -> showStudioOverlay = false
            showDiagnosticsOverlay -> showDiagnosticsOverlay = false
showWakeWordOnboarding -> {
                showWakeWordOnboarding = false
                settingsPrefs.edit().putBoolean("has_seen_wake_word_onboarding", true).apply()
            }
            else -> {
                // Consume the event. Do absolutely nothing to prevent exiting the launcher
            }
        }
    }
    
    val sharedPref = remember { context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE) }
    val homePrefs = remember { context.getSharedPreferences("kuzmix_home_prefs", Context.MODE_PRIVATE) }
    var wallpaperUri by remember { mutableStateOf<String?>(null) }
    var defaultWallpaperKey by remember { mutableStateOf("sunset") }
    var iconMaskKey by remember { mutableStateOf(homePrefs.getString("icon_mask_shape", "squircle") ?: "squircle") }
    var iconScaleFactor by remember { mutableStateOf(homePrefs.getFloat("icon_scale_factor", 1.0f)) }

    LaunchedEffect(sharedPref) {
        withContext(Dispatchers.IO) {
            val uri = sharedPref.getString("wallpaper_uri", null)
            val key = sharedPref.getString("default_wallpaper_key", sharedPref.getString("wallpaper_key", "sunset")) ?: "sunset"
            withContext(Dispatchers.Main) {
                wallpaperUri = uri
                defaultWallpaperKey = key
            }
        }
    }

    DisposableEffect(sharedPref) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "wallpaper_uri" || key == "default_wallpaper_key" || key == "wallpaper_key") {
                wallpaperUri = prefs.getString("wallpaper_uri", null)
                defaultWallpaperKey = prefs.getString("default_wallpaper_key", prefs.getString("wallpaper_key", "sunset")) ?: "sunset"
            } else if (key == "theme_mode") {
                val newMode = prefs.getString("theme_mode", "system") ?: "system"
                onThemeModeChange(newMode)
            } else if (key == "icon_mask_shape") {
                iconMaskKey = prefs.getString("icon_mask_shape", "squircle") ?: "squircle"
            } else if (key == "icon_scale_factor") {
                iconScaleFactor = prefs.getFloat("icon_scale_factor", 1.0f)
            }
        }
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    DisposableEffect(homePrefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "icon_mask_shape") {
                iconMaskKey = prefs.getString("icon_mask_shape", "squircle") ?: "squircle"
            } else if (key == "icon_scale_factor") {
                iconScaleFactor = prefs.getFloat("icon_scale_factor", 1.0f)
            }
        }
        homePrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            homePrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            wallpaperUri = uri.toString()
            sharedPref.edit().putString("wallpaper_uri", uri.toString()).apply()
            showWallpaperHub = false
        }
    }
    
    var generatedImageUrl by remember { mutableStateOf<String?>(null) }
    var isGeneratingImage by remember { mutableStateOf(false) }
    var generatedVideoUrl by remember { mutableStateOf<String?>(null) }
    var isGeneratingVideo by remember { mutableStateOf(false) }

    val intentEngine = remember { 
        KuzmixIntentEngine(context).apply {
            onImageGenerated = { imageUrl, prompt ->
                generatedImageUrl = imageUrl
                generatedVideoUrl = null
                isGeneratingImage = true
                isGeneratingVideo = false
            }
            onVideoGenerated = { videoUrl, prompt ->
                generatedVideoUrl = videoUrl
                generatedImageUrl = null
                isGeneratingVideo = true
                isGeneratingImage = false
            }
        }
    }
    val scope = rememberCoroutineScope()

    var isListening by remember { mutableStateOf(false) }
    var singularityInput by remember { mutableStateOf("") }

var speakTextAloud: (String, String) -> Unit = { _, _ -> }

    val processVoiceOrTextInput = { input: String ->
        showSingularityOverlay = true
        isAiProcessing = true
        aiResponse = ""
        generatedImageUrl = null
        generatedVideoUrl = null
        
        val videoUrlParsed = playGeneratedVideo(input)
        val isVideoCmd = videoUrlParsed != null && videoUrlParsed.startsWith("__OPENROUTER_VIDEO__:")
        
        if (isVideoCmd) {
            isGeneratingVideo = true
            isGeneratingImage = false
            val videoPrompt = videoUrlParsed.removePrefix("__OPENROUTER_VIDEO__:")
            scope.launch {
                aiResponse = "Generating video via OpenRouter... This may take up to 2 minutes."
                try {
                    val keyManager = OpenRouterKeyManager(context)
                    val apiKey = keyManager.getActiveKey()
                    val result = generateVideoWithOpenRouter(
                        context, apiKey, videoPrompt, "16:9", null
                    )
                    if (result != null) {
                        generatedVideoUrl = result
                        aiResponse = "Video forged successfully!"
                    } else {
                        aiResponse = "Video generation failed. Please try again."
                    }
                } catch (e: Exception) {
                    aiResponse = "Video generation error: ${e.message}"
                } finally {
                    isGeneratingVideo = false
                    isAiProcessing = false
                }
            }
        } else {
            val sanitized = input.lowercase().replace(Regex("[,.?!]"), "").trim()
            val isImageCmd = sanitized.startsWith("generate ") || 
                             sanitized.startsWith("draw ") || 
                             sanitized.startsWith("create an image of ") || 
                             sanitized.startsWith("paint ") ||
                             sanitized.startsWith("make an image of ") ||
                             sanitized.startsWith("hey kuzmix generate ") ||
                             sanitized.startsWith("hey kuzmix draw ") ||
                             sanitized.startsWith("hey kuzmix create an image of ") ||
                             sanitized.startsWith("hey kuzmix paint ") ||
                             sanitized.startsWith("hey kuzmix make an image of ") ||
                             sanitized.startsWith("kuzmix generate ") ||
                             sanitized.startsWith("kuzmix draw ") ||
                             sanitized.startsWith("kuzmix create an image of ") ||
                             sanitized.startsWith("kuzmix paint ") ||
                             sanitized.startsWith("kuzmix make an image of ")
            isGeneratingImage = isImageCmd
            isGeneratingVideo = false
        }

        scope.launch {
            intentEngine.processIntent(input) { update ->
                aiResponse = update
                if (update.isNotEmpty() && update != "Listening...") {
                    isAiProcessing = false
                }
            }
            isAiProcessing = false
            speakTextAloud(aiResponse, input)
        }
    }

    val setVideoAsWallpaper = {
        val url = generatedVideoUrl
        if (!url.isNullOrEmpty()) {
            scope.launch {
                aiResponse = "Downloading animation stream for Live Wallpaper... Please wait."
                try {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        val client = okhttp3.OkHttpClient()
                        val req = okhttp3.Request.Builder().url(url).build()
                        val resp = client.newCall(req).execute()
                        val bytes = resp.body?.bytes()
                        if (bytes != null && bytes.isNotEmpty()) {
                            val file = java.io.File(context.filesDir, "kuzmix_wallpaper.mp4")
                            file.writeBytes(bytes)
                            val sharedPref = context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
                            sharedPref.edit().putString("live_wallpaper_video_uri", android.net.Uri.fromFile(file).toString()).apply()

                            val intent = android.content.Intent(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                putExtra(
                                    android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                    android.content.ComponentName(context, KuzmixVideoWallpaperService::class.java)
                                )
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            aiResponse = "Kuzmix Live Wallpaper configured successfully!"
                        } else {
                            aiResponse = "Failed to download animation payload."
                        }
                    }
                } catch (e: Exception) {
                    aiResponse = "Wallpaper setup error: ${e.message}"
                }
            }
        }
    }

    val saveVideoToGallery = {
        val url = generatedVideoUrl
        if (!url.isNullOrEmpty()) {
            try {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                val fileName = "kuzmix_anim_${System.currentTimeMillis()}.mp4"
                val request = android.app.DownloadManager.Request(android.net.Uri.parse(url)).apply {
                    setTitle("Kuzmix Animator Video")
                    setDescription("Forging celestial animation")
                    setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_MOVIES, "KuzmixAnimator/$fileName")
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                }
                dm.enqueue(request)
                aiResponse = "Saving animation to Movies/KuzmixAnimator/$fileName..."
            } catch (e: Exception) {
                aiResponse = "Save to gallery failed: ${e.message}"
            }
        }
    }

    val setGeneratedAsWallpaper = {
        val url = generatedImageUrl
        if (url != null) {
            scope.launch {
                aiResponse = "Downloading and configuring wallpaper... Please wait."
                try {
                    val loader = coil.Coil.imageLoader(context)
                    val req = coil.request.ImageRequest.Builder(context)
                        .data(url)
                        .build()
                    val result = loader.execute(req)
                    if (result is coil.request.SuccessResult) {
                        val drawable = result.drawable
                        val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        if (bitmap != null) {
                            val wm = android.app.WallpaperManager.getInstance(context)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                wm.setBitmap(bitmap)
                            }
                            aiResponse = "Wallpaper updated successfully with forged visualization!"
                        } else {
                            aiResponse = "Failed to process image: invalid format."
                        }
                    } else {
                        aiResponse = "Failed to download forged visualization."
                    }
                } catch (e: Exception) {
                    aiResponse = "Wallpaper update failed: ${e.message}"
                }
            }
        }
    }

    val saveGeneratedToGallery = {
        val url = generatedImageUrl
        if (url != null) {
            scope.launch {
                aiResponse = "Saving visualization to your gallery... Please wait."
                try {
                    val loader = coil.Coil.imageLoader(context)
                    val req = coil.request.ImageRequest.Builder(context)
                        .data(url)
                        .build()
                    val result = loader.execute(req)
                    if (result is coil.request.SuccessResult) {
                        val drawable = result.drawable
                        val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        if (bitmap != null) {
                            var savedUri: Uri? = null
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                val resolver = context.contentResolver
                                val contentValues = android.content.ContentValues().apply {
                                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "KuzmixForge_${System.currentTimeMillis()}.png")
                                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/KuzmixForge")
                                        put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                                    }
                                }
                                val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                                if (imageUri != null) {
                                    try {
                                        resolver.openOutputStream(imageUri)?.use { outStream ->
                                            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outStream)
                                        }
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                            contentValues.clear()
                                            contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                                            resolver.update(imageUri, contentValues, null, null)
                                        }
                                        savedUri = imageUri
                                    } catch (ex: Exception) {
                                        resolver.delete(imageUri, null, null)
                                        throw ex
                                    }
                                }
                            }
                            if (savedUri != null) {
                                aiResponse = "Visualization successfully saved to Gallery!"
                            } else {
                                aiResponse = "Failed to save visualization."
                            }
                        } else {
                            aiResponse = "Failed to process image: invalid format."
                        }
                    } else {
                        aiResponse = "Failed to download image."
                    }
                } catch (e: Exception) {
                    aiResponse = "Save failed: ${e.message}"
                }
            }
        }
    }

    val setVoiceServiceListening = { pause: Boolean ->
        try {
            val intent = android.content.Intent(context, KuzmixVoiceService::class.java).apply {
                if (pause) {
                    putExtra("EXTRA_PAUSE_LISTENING", true)
                } else {
                    putExtra("EXTRA_RESUME_LISTENING", true)
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("KuzmixOS", "Failed to update voice service state: ${e.message}")
        }
    }

    val voiceRecognitionHelper = remember {
        VoiceRecognitionHelper(
            context = context,
            onResult = { text ->
                isListening = false
                if (text.isNotEmpty()) {
                    singularityInput = text
                    processVoiceOrTextInput(text)
                } else if (aiResponse == "Listening...") {
                    aiResponse = "No speech detected. Tap mic to speak."
                }
                setVoiceServiceListening(false)
            },
            onError = { errorMsg, errorCode ->
                isListening = false
                if (aiResponse == "Listening..." || aiResponse.isBlank()) {
                    if (errorCode == android.speech.SpeechRecognizer.ERROR_NO_MATCH || errorCode == android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        aiResponse = "No speech detected. Tap the mic button to try speaking again."
                    } else if (errorCode == android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                        aiResponse = "Microphone permission required. Please grant RECORD_AUDIO permission."
                    } else {
                        aiResponse = "Voice input paused ($errorMsg). Tap mic or type query."
                    }
                }
                setVoiceServiceListening(false)
            },
            onListeningStateChange = { listening ->
                isListening = listening
                if (listening && (aiResponse.isBlank() || aiResponse.contains("error", ignoreCase = true) || aiResponse == "Listening...")) {
                    aiResponse = "Listening..."
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceRecognitionHelper.destroy()
        }
    }

    val startSpeechRecognizer = {
        val activity = context as? android.app.Activity
        if (!voiceRecognitionHelper.hasMicrophonePermission()) {
            aiResponse = "Microphone permission required. Requesting access..."
            voiceRecognitionHelper.requestMicrophonePermission(activity)
        } else {
            setVoiceServiceListening(true)
            showSingularityOverlay = true
            scope.launch {
                kotlinx.coroutines.delay(200)
                val started = voiceRecognitionHelper.startListening(activity)
                if (!started) {
                    setVoiceServiceListening(false)
                }
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognizer()
        }
    }

    val triggerSpeechRecognition = {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            startSpeechRecognizer()
        } else {
            recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        // Immediately start listening for audio commands upon entering HomeDashboard after login
        kotlinx.coroutines.delay(400)
        triggerSpeechRecognition()
    }

speakTextAloud = { text: String, query: String ->
        val lowerQuery = query.lowercase()
        val lowerText = text.lowercase()
        val isQuiet = lowerQuery.contains("quietly") || lowerQuery.contains("silently") || lowerQuery.contains("shh") || lowerQuery.contains("don't read") ||
                lowerText.contains("quiet") || lowerText.contains("shh") || lowerText.contains("silent") || lowerText.contains("mute")
        
        if (tts != null && !isQuiet) {
            val params = android.os.Bundle().apply {
                putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "kuzmix_tts_utterance")
            }
            tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "kuzmix_tts_utterance")
        }
    }

LaunchedEffect(Unit) {
        apps = appLoader.loadApps().sortedBy { it.label }
    }

    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance().time
        }
    }

    var isFinalPageSelected by remember { mutableStateOf(false) }
    var isAiActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    try {
                        val intent = android.content.Intent().apply {
                            component = android.content.ComponentName(context, "com.kreadivegalaxy.kuzmixos.KCCustomizationActivity")
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
            }
    ) {
        val blurRadius = if (hardwareTier == HardwareTier.TITAN && (showAppLibrary || showWallpaperHub || showSingularityOverlay || showControlCenter || isFinalPageSelected)) {
            60.dp
        } else {
            0.dp
        }
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1015))) {
            if (wallpaperUri != null) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(wallpaperUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                DefaultWallpaper(
                    key = defaultWallpaperKey,
                    modifier = Modifier.fillMaxSize()
                )
            }

            KuzmixHomeScreen(
                hardwareTier = hardwareTier,
                apps = apps,
                currentTime = currentTime,
                onSearchClick = { showAppLibrary = true },
                onMicClick = {
                    isAiActive = !isAiActive
                    triggerSpeechRecognition()
                },
                onPageSelected = { isFinalPage ->
                    isFinalPageSelected = isFinalPage
                },
                onAiChatClick = { showSingularityOverlay = true },
                onControlCenterClick = { showControlCenter = true },
                onThemesClick = { showWallpaperHub = true },
                onQrScannerClick = { showQrScannerOverlay = true },
                onVoiceSettingsClick = { showVoiceSettingsOverlay = true },
                onUtilitiesClick = { showUtilitiesOverlay = true },
                onSupportClick = { showSupportOverlay = true },
                onStudioClick = { showStudioOverlay = true },
                onDiagnosticsClick = { showDiagnosticsOverlay = true }
)
        }

        // Dedicated status bar / gesture pull down area at the top of the screen.
        // Swiping down from top-right opens Control Center. Swiping anywhere else works perfectly for grid/list scrolling.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        if (change.position.x > size.width / 2f && dragAmount > 15f) {
                            showControlCenter = true
                        }
                    }
                }
        )
        
        AnimatedVisibility(
            visible = showAppLibrary,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            AlphabeticalAppLibraryOverlay(
                hardwareTier = hardwareTier,
                apps = apps,
                onClose = { showAppLibrary = false },
                onAiChatClick = { showSingularityOverlay = true },
                onControlCenterClick = { showControlCenter = true },
                onThemesClick = { showWallpaperHub = true },
                onQrScannerClick = { showQrScannerOverlay = true },
                onVoiceSettingsClick = { showVoiceSettingsOverlay = true },
                onUtilitiesClick = { showUtilitiesOverlay = true },
                onSupportClick = { showSupportOverlay = true },
                onStudioClick = { showStudioOverlay = true },
                onDiagnosticsClick = { showDiagnosticsOverlay = true }
)
        }
        
        AnimatedVisibility(
            visible = showControlCenter,
            enter = slideEnterReverse,
            exit = slideExitReverse,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            ControlCenter(
                hardwareTier = hardwareTier,
                intentEngine = intentEngine,
                onClose = { showControlCenter = false },
                onLockOS = onLockOS
            )
        }

        AnimatedVisibility(
            visible = showWallpaperHub,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            KuzmixCustomizationHub(
                hardwareTier = hardwareTier,
                selectedWallpaperKey = defaultWallpaperKey,
                onSelectWallpaper = { key ->
                    wallpaperUri = null
                    defaultWallpaperKey = key
                    sharedPref.edit()
                        .putString("default_wallpaper_key", key)
                        .remove("wallpaper_uri")
                        .apply()
                },
                onGalleryClick = {
                    galleryLauncher.launch("image/*")
                },
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                iconMaskKey = iconMaskKey,
                onIconMaskChange = { newMask ->
                    iconMaskKey = newMask
                    homePrefs.edit().putString("icon_mask_shape", newMask).apply()
                    sharedPref.edit().putString("icon_mask_shape", newMask).apply()
                },
                iconScaleFactor = iconScaleFactor,
                onIconScaleChange = { newScale ->
                    iconScaleFactor = newScale
                    homePrefs.edit().putFloat("icon_scale_factor", newScale).apply()
                    sharedPref.edit().putFloat("icon_scale_factor", newScale).apply()
                },
                onClose = { showWallpaperHub = false }
            )
        }

        AnimatedVisibility(
            visible = showSingularityOverlay,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            SingularityOverlay(
                hardwareTier = hardwareTier,
                onClose = { showSingularityOverlay = false },
                isProcessing = isAiProcessing,
                responseText = aiResponse,
                inputValue = singularityInput,
                onInputValueChange = { singularityInput = it },
                isListening = isListening,
                onMicClick = { triggerSpeechRecognition() },
                onInput = { input ->
                    processVoiceOrTextInput(input)
                },
                generatedImageUrl = generatedImageUrl,
                onSetAsWallpaper = { setGeneratedAsWallpaper() },
                onSaveToGallery = { saveGeneratedToGallery() },
                isImageGenerating = isGeneratingImage,
                onImageGeneratingChange = { isGeneratingImage = it },
                generatedVideoUrl = generatedVideoUrl,
                isVideoGenerating = isGeneratingVideo,
                onVideoGeneratingChange = { isGeneratingVideo = it },
                onSetVideoAsWallpaper = { setVideoAsWallpaper() },
                onSaveVideoToGallery = { saveVideoToGallery() }
            )
        }

        AnimatedVisibility(
            visible = showQrScannerOverlay,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            QrScannerAppOverlay(
                hardwareTier = hardwareTier,
                onClose = { showQrScannerOverlay = false }
            )
        }

        AnimatedVisibility(
            visible = showVoiceSettingsOverlay,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            KuzmixWakeWordSettingsScreen(
                onClose = { showVoiceSettingsOverlay = false }
            )
        }

        AnimatedVisibility(
            visible = showUtilitiesOverlay,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            KuzmixUtilitiesAppOverlay(
                hardwareTier = hardwareTier,
                onClose = { showUtilitiesOverlay = false },
                onOpenDiagnosticLogs = { showDiagnosticsOverlay = true }
            )
        }

        AnimatedVisibility(
            visible = showDiagnosticsOverlay,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            com.kreadivegalaxy.kuzmixos.diagnostics.DiagnosticLogsViewerScreen(
                onClose = { showDiagnosticsOverlay = false }
)
        }

        AnimatedVisibility(
            visible = showSupportOverlay,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            KuzmixSupportAppOverlay(
                hardwareTier = hardwareTier,
                onClose = { showSupportOverlay = false }
            )
        }

        AnimatedVisibility(
            visible = showStudioOverlay,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            KuzmixStudioOverlay(
                hardwareTier = hardwareTier,
                onClose = { showStudioOverlay = false }
            )
        }

        AnimatedVisibility(
            visible = biometricNotification != null,
            enter = slideEnterReverse,
            exit = slideExitReverse,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp, start = 16.dp, end = 16.dp)
        ) {
            val text = biometricNotification ?: ""
            val isDenied = text.contains("Denied")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDenied) Color(0xEB420000) else Color(0xEB0A1128),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        0.5.dp,
                        if (isDenied) Color.Red.copy(alpha = 0.5f) else KuzmixOrange.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isDenied) Icons.Default.Lock else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isDenied) Color.Red else KuzmixYellow,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        AnimatedVisibility(
            visible = showWakeWordOnboarding,
            enter = slideEnter,
            exit = slideExit,
            modifier = Modifier.graphicsLayer { clip = true }
        ) {
            KuzmixWakeWordOnboarding(
                onDismiss = {
                    showWakeWordOnboarding = false
                    settingsPrefs.edit().putBoolean("has_seen_wake_word_onboarding", true).apply()
                }
            )
        }
    }
}

@Composable
fun KuzmixHomeScreen(
    hardwareTier: HardwareTier,
    apps: List<AppItem>,
    currentTime: Date,
    onSearchClick: () -> Unit,
    onMicClick: () -> Unit,
    onPageSelected: (Boolean) -> Unit,
    onAiChatClick: () -> Unit = {},
    onControlCenterClick: () -> Unit = {},
    onThemesClick: () -> Unit = {},
    onQrScannerClick: () -> Unit = {},
    onVoiceSettingsClick: () -> Unit = {},
    onUtilitiesClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onStudioClick: () -> Unit = {},
    onDiagnosticsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val weatherManager = remember(context) { WeatherManager(context) }

    val prefs = remember(context) { context.getSharedPreferences("kuzmix_home_prefs", Context.MODE_PRIVATE) }
    var removedPackageNames by remember {
        mutableStateOf(prefs.getStringSet("removed_packages", emptySet()) ?: emptySet())
    }
    var customAppLabels by remember {
        mutableStateOf<Map<String, String>>(
            prefs.all.filterKeys { it.startsWith("custom_label_") }
                .mapKeys { it.key.removePrefix("custom_label_") }
                .mapValues { it.value.toString() }
        )
    }

    var isEditMode by remember { mutableStateOf(false) }
    var focusedApp by remember { mutableStateOf<AppItem?>(null) }
    var appToRename by remember { mutableStateOf<AppItem?>(null) }
    var renameInputText by remember { mutableStateOf("") }

    val virtualApps = remember(context) {
        listOf(
            AppItem(
                label = "Kuzmix AI",
                packageName = "com.kreadivegalaxy.kuzmixos.aichat",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_menu_help),
intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_AI_CHAT")
            ),
            AppItem(
                label = "Control Center",
                packageName = "com.kreadivegalaxy.kuzmixos.controlcenter",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_menu_manage),
intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_CONTROL_CENTER")
            ),
            AppItem(
                label = "Themes",
                packageName = "com.kreadivegalaxy.kuzmixos.themes",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_menu_gallery),
intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_THEMES")
            ),
            AppItem(
                label = "QR Scanner & Generator",
                packageName = "com.kreadivegalaxy.kuzmixos.qrscanner",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_menu_camera),
intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_QR_SCANNER")
            ),
            AppItem(
                label = "Voice Settings",
                packageName = "com.kreadivegalaxy.kuzmixos.voicesettings",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_btn_speak_now),
intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_VOICE_SETTINGS")
            ),
            AppItem(
                label = "Utilities",
                packageName = "com.kreadivegalaxy.kuzmixos.utilities",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_menu_preferences),
intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_UTILITIES")
            ),
            AppItem(
                label = "Support & About",
                packageName = "com.kreadivegalaxy.kuzmixos.support",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_menu_help),
intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_SUPPORT")
            ),
            AppItem(
                label = "Kuzmix Studio",
                packageName = "com.kreadivegalaxy.kuzmixos.studio",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_menu_slideshow),
                intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_STUDIO")
            ),
            AppItem(
                label = "Diagnostic Logs",
                packageName = "com.kreadivegalaxy.kuzmixos.diagnostics",
                icon = safeGetSystemDrawable(context, android.R.drawable.ic_menu_info_details),
                intent = android.content.Intent("com.kreadivegalaxy.kuzmixos.ACTION_DIAGNOSTICS")
)
        )
    }

    val allAvailableApps = remember(apps, virtualApps) {
        val existingPkgs = apps.map { it.packageName }.toSet()
        val missingVirtuals = virtualApps.filterNot { existingPkgs.contains(it.packageName) }
        missingVirtuals + apps
    }

    var dockedPackageNamesString by remember {
        mutableStateOf(prefs.getString("dock_packages_list", null))
    }

    val dockApps = remember(allAvailableApps, dockedPackageNamesString) {
        val packageList = if (dockedPackageNamesString != null) {
            val listStr = dockedPackageNamesString.orEmpty()
            if (listStr.isEmpty()) emptyList() else listStr.split(",")
} else {
            getDockApps(context, allAvailableApps).map { it.packageName }
        }
        
        val appsMap = allAvailableApps.associateBy { it.packageName }
        packageList.mapNotNull { appsMap[it] }
    }

    val dockPackageNames = remember(dockApps) { dockApps.map { it.packageName }.toSet() }

    val homeApps = remember(allAvailableApps, removedPackageNames, dockPackageNames) {
        allAvailableApps.filterNot { 
            it.packageName == context.packageName || 
            dockPackageNames.contains(it.packageName) ||
            removedPackageNames.contains(it.packageName)
        }
    }

    val launchApp = { app: AppItem ->
        when (app.packageName) {
            "com.kreadivegalaxy.kuzmixos.aichat" -> onAiChatClick()
            "com.kreadivegalaxy.kuzmixos.controlcenter" -> onControlCenterClick()
            "com.kreadivegalaxy.kuzmixos.themes" -> onThemesClick()
            "com.kreadivegalaxy.kuzmixos.qrscanner" -> onQrScannerClick()
            "com.kreadivegalaxy.kuzmixos.voicesettings" -> onVoiceSettingsClick()
            "com.kreadivegalaxy.kuzmixos.utilities" -> onUtilitiesClick()
            "com.kreadivegalaxy.kuzmixos.support" -> onSupportClick()
            "com.kreadivegalaxy.kuzmixos.studio" -> onStudioClick()
            "com.kreadivegalaxy.kuzmixos.diagnostics" -> onDiagnosticsClick()
else -> {
                try { context.startActivity(app.intent) } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val columns = 4
    val rows = 6
    val maxAppsPerPage = columns * rows

    val page0GridPositions = remember {
        listOf(
            AppGridPosition(0, 0, "Settings L...", "com.android.settings"),
            AppGridPosition(0, 1, "Weather", "com.android.weather"),
            AppGridPosition(0, 2, "Calendar", "com.android.calendar"),
            AppGridPosition(0, 3, "Photos", "com.android.photos"),
            
            AppGridPosition(1, 0, "Calculator", "com.android.calculator"),
            AppGridPosition(1, 1, "Clock", "com.android.clock"),
            AppGridPosition(1, 2, "Themes", "com.kreadivegalaxy.kuzmixos.themes"),
            AppGridPosition(1, 3, "Google", "com.google.android.apps"),
            
            AppGridPosition(2, 0, "WhatsApp", "com.whatsapp"),
            AppGridPosition(2, 1, "Kuzmix AI", "com.kreadivegalaxy.kuzmixos.aichat"),
            AppGridPosition(2, 2, "Control Ce...", "com.kreadivegalaxy.kuzmixos.controlcenter"),
            AppGridPosition(2, 3, "InShot", "com.inshot"),
            
            AppGridPosition(3, 0, "Facebook", "com.facebook.katana"),
            AppGridPosition(3, 1, "QR Scanner & Gen", "com.kreadivegalaxy.kuzmixos.qrscanner"),
            AppGridPosition(3, 2, "Snapchat", "com.snapchat.android"),
            AppGridPosition(3, 3, "CapCut", "com.capcut"),
            
            AppGridPosition(4, 0, "Support", "com.kreadivegalaxy.kuzmixos.support"),
            AppGridPosition(4, 1, "OPay", "com.opay"),
            AppGridPosition(4, 2, "MT Manager", "com.mtmanager"),
            AppGridPosition(4, 3, "Utilities", "com.kreadivegalaxy.kuzmixos.utilities")
        )
    }

    val page0PackageNames = remember(page0GridPositions) {
        page0GridPositions.map { it.packageName }.toSet()
    }

    val pages = remember(homeApps, page0PackageNames) {
        val list = mutableListOf<List<AppItem>>()
        val otherHomeApps = homeApps.filterNot { page0PackageNames.contains(it.packageName) }
        
        list.add(emptyList()) // Page 0 is custom
        if (otherHomeApps.isNotEmpty()) {
            list.addAll(otherHomeApps.chunked(maxAppsPerPage))
        }
        list
    }

    val pagerState = rememberPagerState(pageCount = { maxOf(1, pages.size + 1) })

    LaunchedEffect(pagerState.currentPage) {
        onPageSelected(pagerState.currentPage == pages.size)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (focusedApp == null) {
                            try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                            isEditMode = true
                        }
                    },
                    onTap = {
                        if (isEditMode) {
                            isEditMode = false
                        } else if (focusedApp != null) {
                            focusedApp = null
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Edit Mode Bar Header (Left button Edit, Right button Done)
AnimatedVisibility(
                visible = isEditMode,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Edit",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF0A84FF))
                            .clickable { isEditMode = false }
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Done",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
}
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) { page ->
                if (page < pages.size) {
                    val pageApps = pages.getOrNull(page) ?: emptyList()
                    val fallbackDrawable = remember(context) {
                        safeGetSystemDrawable(context, android.R.drawable.sym_def_app_icon)
}
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        verticalArrangement = if (page == 0) Arrangement.Top else Arrangement.spacedBy(16.dp, Alignment.Top)
                    ) {
                        if (page == 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // TOP WIDGET CONTAINER (Side-by-side dual 2x2 cards)
Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(154.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(Color(0xFF1C1C1E))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp)),
                                        contentAlignment = Alignment.Center
) {
                                        SpatialGlassClock(currentTime)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Clock",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(154.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(Color(0xFF1C1C1E))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp)),
                                        contentAlignment = Alignment.Center
) {
                                        CalendarWidget(currentTime)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Calendar",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))

                            // Custom exact Page 0 apps layout
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                for (row in 0..4) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (col in 0..3) {
                                            val positionSpec = page0GridPositions.firstOrNull { it.row == row && it.col == col }
                                            if (positionSpec != null) {
                                                val realApp = homeApps.firstOrNull { it.packageName == positionSpec.packageName }
                                                val displayApp = realApp ?: AppItem(
                                                    label = positionSpec.label,
                                                    packageName = positionSpec.packageName,
                                                    icon = fallbackDrawable,
                                                    intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
                                                )

                                                val isFocusedThis = focusedApp?.packageName == displayApp.packageName
                                                val isDimmedThis = focusedApp != null && !isFocusedThis

                                                Box(modifier = Modifier.width(68.dp), contentAlignment = Alignment.Center) {
                                                    GlassAppIcon(
                                                        app = displayApp,
                                                        animationDelay = (row * columns + col) * 30,
                                                        isEditMode = isEditMode,
                                                        isFocused = isFocusedThis,
                                                        isDimmed = isDimmedThis,
                                                        customLabel = customAppLabels[displayApp.packageName] ?: positionSpec.label,
                                                        onRemoveClick = {
                                                            val newSet = removedPackageNames + displayApp.packageName
                                                            removedPackageNames = newSet
                                                            prefs.edit().putStringSet("removed_packages", newSet).apply()
                                                        },
                                                        onClick = {
                                                            launchApp(displayApp)
                                                        },
                                                        onLongClick = {
                                                            try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                                                            focusedApp = displayApp
                                                        }
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(68.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Paginated 4x6 Application Grid for subsequent pages (page > 0)
                            val pageRows = pageApps.chunked(columns)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                pageRows.forEachIndexed { rowIndex, rowApps ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for ((appIndex, app) in rowApps.withIndex()) {
                                            val isFocusedThis = focusedApp?.packageName == app.packageName
                                            val isDimmedThis = focusedApp != null && !isFocusedThis

                                            Box(modifier = Modifier.width(68.dp), contentAlignment = Alignment.Center) {
                                                GlassAppIcon(
                                                    app = app,
                                                    animationDelay = (rowIndex * columns + appIndex) * 30,
                                                    isEditMode = isEditMode,
                                                    isFocused = isFocusedThis,
                                                    isDimmed = isDimmedThis,
                                                    customLabel = customAppLabels[app.packageName],
                                                    onRemoveClick = {
                                                        val newSet = removedPackageNames + app.packageName
                                                        removedPackageNames = newSet
                                                        prefs.edit().putStringSet("removed_packages", newSet).apply()
                                                    },
                                                    onClick = {
                                                        launchApp(app)
                                                    },
                                                    onLongClick = {
                                                        try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                                                        focusedApp = app
                                                    }
                                                )
                                            }
                                        }
                                        if (rowApps.size < columns) {
                                            repeat(columns - rowApps.size) {
                                                Spacer(modifier = Modifier.width(68.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    CategorizedAppLibraryPage(
                        hardwareTier = hardwareTier,
                        apps = apps,
                        onSearchClick = onSearchClick
)
                }
            }

            // 3. FLOATING SEARCH PILL (Centered horizontally above the page dots)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSearchClick() }
                    .padding(horizontal = 14.dp, vertical = 5.dp),
contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔍", fontSize = 11.sp)
                    Text(
                        text = "Search",
                        color = Color(0xFFEBEBF5),
                        fontSize = 12.sp,
fontWeight = FontWeight.Medium
                    )
                }
            }

            // 4. PAGE INDICATOR (Standard subtle horizontal dots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val isCurrent = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) Color.White else Color.White.copy(alpha = 0.35f))
                            .size(if (isCurrent) 6.5.dp else 5.5.dp)
                    )
                }
            }

            // 5. FLOATING DOCK (Translucent frosted glass container at bottom)
            if (dockApps.isNotEmpty()) {
                FloatingLiquidDock(
                    dockApps = dockApps.take(4),
hardwareTier = hardwareTier,
                    onClick = { app ->
                        launchApp(app)
                    },
                    onLongClick = { app ->
                        try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                        focusedApp = app
                    }
                )
            }
}

        // Long Press Focused Context Menu Modal Overlay
        AnimatedVisibility(
            visible = focusedApp != null,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            val targetApp = focusedApp
            if (targetApp != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.68f))
                        .clickable { focusedApp = null },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .width(260.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF252528))
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(16.dp)
.clickable(enabled = false) {}
                    ) {
                        GlassAppIcon(
                            app = targetApp,
                            isFocused = true,
                            customLabel = customAppLabels[targetApp.packageName],
                            onClick = {
                                launchApp(targetApp)
                                focusedApp = null
                            }
                        )

                        Text(
                            text = customAppLabels[targetApp.packageName] ?: targetApp.label,
                            color = Color.White,
                            fontSize = 15.sp,
fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color.White.copy(alpha = 0.16f))
)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. App info
                            ContextMenuItem(
                                icon = Icons.Default.Info,
                                label = "App info",
color = Color.White,
                                onClick = {
                                    try {
                                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = android.net.Uri.fromParts("package", targetApp.packageName, null)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) { e.printStackTrace() }
                                    focusedApp = null
                                }
                            )

                            // 2. Select
                            ContextMenuItem(
                                icon = Icons.Default.CheckCircle,
                                label = "Select",
                                color = Color.White,
                                onClick = {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (_: Exception) {}
                                    focusedApp = null
                                }
                            )

                            // 3. Hide App
                            ContextMenuItem(
                                icon = Icons.Default.VisibilityOff,
                                label = "Hide App",
                                color = Color.White,
                                onClick = {
                                    val currentHidden = prefs.getStringSet("hidden_packages", emptySet<String>()) ?: emptySet<String>()
                                    val updatedHidden = currentHidden + targetApp.packageName
                                    prefs.edit().putStringSet("hidden_packages", updatedHidden).apply()
val newSet = removedPackageNames + targetApp.packageName
                                    removedPackageNames = newSet
                                    prefs.edit().putStringSet("removed_packages", newSet).apply()
                                    focusedApp = null
                                }
                            )

                            // 4. Edit Homescreen
                            ContextMenuItem(
                                icon = Icons.Default.Edit,
                                label = "Edit Homescreen",
                                color = Color.White,
                                onClick = {
                                    isEditMode = true
                                    focusedApp = null
                                }
                            )

                            // 5. Remove App (accented in #FF453A)
                            ContextMenuItem(
                                icon = Icons.Default.Delete,
                                label = "Remove App",
                                color = Color(0xFFFF453A),
                                onClick = {
                                    val newSet = removedPackageNames + targetApp.packageName
                                    removedPackageNames = newSet
                                    prefs.edit().putStringSet("removed_packages", newSet).apply()
focusedApp = null
                                }
                            )
                        }
                    }
                }
            }
        }

        // Custom Label Dialog
        if (appToRename != null) {
            val targetForRename = appToRename
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { appToRename = null },
                title = { Text("Rename App", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter a custom label for ${targetForRename?.label}:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        androidx.compose.material3.OutlinedTextField(
                            value = renameInputText,
                            onValueChange = { renameInputText = it },
                            singleLine = true,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = KuzmixYellow,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                            )
                        )
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val targetPkg = targetForRename?.packageName
                            if (targetPkg != null) {
                                val newMap = customAppLabels.toMutableMap()
                                if (renameInputText.isNotBlank()) {
                                    newMap[targetPkg] = renameInputText.trim()
                                    prefs.edit().putString("custom_label_$targetPkg", renameInputText.trim()).apply()
                                } else {
                                    newMap.remove(targetPkg)
                                    prefs.edit().remove("custom_label_$targetPkg").apply()
                                }
                                customAppLabels = newMap
                            }
                            appToRename = null
                        }
                    ) {
                        Text("Save", color = KuzmixYellow, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { appToRename = null }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                containerColor = DarkCard,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

@Composable
fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text = label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlphabeticalAppLibraryOverlay(
    hardwareTier: HardwareTier,
    apps: List<AppItem>,
    onClose: () -> Unit,
    onAiChatClick: () -> Unit = {},
    onControlCenterClick: () -> Unit = {},
    onThemesClick: () -> Unit = {},
    onQrScannerClick: () -> Unit = {},
    onVoiceSettingsClick: () -> Unit = {},
    onUtilitiesClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onStudioClick: () -> Unit = {},
    onDiagnosticsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val safeApps = apps.filterNot { it.packageName == context.packageName }
    
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredApps = remember(safeApps, searchQuery) {
        if (searchQuery.isEmpty()) {
            safeApps
        } else {
            safeApps.filter { 
                it.label.contains(searchQuery, ignoreCase = true) || 
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val groupedApps = remember(filteredApps) {
        filteredApps.groupBy { it.label.firstOrNull()?.uppercaseChar() ?: '#' }.toSortedMap()
    }
    
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#"
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { clip = true }
            .background(if (hardwareTier == HardwareTier.NOVA) DarkBackground else DarkBackground)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Glass Search Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(DarkSurface, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Search Kuzmix Apps...",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { searchQuery = "" }
                                    )
                                }
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onClose() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Responsive Grid
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val currentWidth = this.maxWidth
                val columns = maxOf(4, (currentWidth / 85.dp).toInt())
                
                val letterToGridIndex = remember(groupedApps, columns) {
                    val map = mutableMapOf<Char, Int>()
                    var currentIndex = 0
                    groupedApps.forEach { (letter, appList) ->
                        map[letter] = currentIndex
                        currentIndex += 1
                        currentIndex += appList.size
                    }
                    map
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 40.dp)
                        .graphicsLayer { clip = true },
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    groupedApps.forEach { (letter, appList) ->
                        item(
                            span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) },
                            key = "header_$letter"
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 8.dp)
                            ) {
                                Text(
                                    text = letter.toString(),
                                    color = KuzmixOrange,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }
                        
                        items(appList, key = { it.packageName }) { app ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GlassAppIcon(
                                    app = app,
                                    animationDelay = 0,
                                    onClick = {
                                        when (app.packageName) {
                                            "com.kreadivegalaxy.kuzmixos.aichat" -> {
                                                onClose()
                                                onAiChatClick()
                                            }
                                            "com.kreadivegalaxy.kuzmixos.controlcenter" -> {
                                                onClose()
                                                onControlCenterClick()
                                            }
                                            "com.kreadivegalaxy.kuzmixos.themes" -> {
                                                onClose()
                                                onThemesClick()
                                            }
                                            "com.kreadivegalaxy.kuzmixos.qrscanner" -> {
                                                onClose()
                                                onQrScannerClick()
                                            }
                                            "com.kreadivegalaxy.kuzmixos.voicesettings" -> {
                                                onClose()
                                                onVoiceSettingsClick()
                                            }
                                            "com.kreadivegalaxy.kuzmixos.utilities" -> {
                                                onClose()
                                                onUtilitiesClick()
                                            }
                                            "com.kreadivegalaxy.kuzmixos.support" -> {
                                                onClose()
                                                onSupportClick()
                                            }
                                            "com.kreadivegalaxy.kuzmixos.studio" -> {
                                                onClose()
                                                onStudioClick()
                                            }
                                            "com.kreadivegalaxy.kuzmixos.diagnostics" -> {
                                                onClose()
                                                onDiagnosticsClick()
                                            }
else -> {
                                                try { context.startActivity(app.intent) } catch (e: Exception) { e.printStackTrace() }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Vertical Searchable Index Sidebar
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(36.dp)
                        .padding(end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    alphabet.forEach { char ->
                        val hasAppsForLetter = groupedApps.containsKey(char)
                        Text(
                            text = char.toString(),
                            color = if (hasAppsForLetter) KuzmixOrange else Color.White.copy(alpha = 0.35f),
                            fontSize = if (hasAppsForLetter) 12.sp else 9.sp,
                            fontWeight = if (hasAppsForLetter) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = hasAppsForLetter) {
                                    val targetIndex = letterToGridIndex[char]
                                    if (targetIndex != null) {
                                        coroutineScope.launch {
                                            gridState.animateScrollToItem(targetIndex)
                                        }
                                    }
                                }
                                .padding(vertical = 1.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlCenter(
    hardwareTier: HardwareTier,
    intentEngine: KuzmixIntentEngine,
    onClose: () -> Unit,
    onLockOS: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager }
    var wifiEnabled by remember { mutableStateOf(false) }

    val bluetoothAdapter = remember { android.bluetooth.BluetoothAdapter.getDefaultAdapter() }
    var bluetoothEnabled by remember { mutableStateOf(false) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager }
    var volumeLevel by remember {
        mutableStateOf(
            audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat() /
            maxOf(1, audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)).toFloat()
        )
    }

    var brightnessLevel by remember {
        val brightness = try {
            android.provider.Settings.System.getInt(context.contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            128
        }
        mutableStateOf(brightness.toFloat() / 255f)
    }

    var flashlightOn by remember { mutableStateOf(false) }
    var isRotationLocked by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            @Suppress("DEPRECATION")
            wifiEnabled = wifiManager.isWifiEnabled
        } catch (e: Exception) { e.printStackTrace() }
        try {
            @android.annotation.SuppressLint("MissingPermission")
            val isEnabled = bluetoothAdapter?.isEnabled == true
            bluetoothEnabled = isEnabled
        } catch (e: Exception) { e.printStackTrace() }
    }

    val scrollState = androidx.compose.foundation.rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface
                    )
                )
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -15f) {
                        onClose()
                    }
                }
            }
            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { onClose() }
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var showVoiceCalibrationDialog by remember { mutableStateOf(false) }
            var showVoiceSettingsDialogInControlCenter by remember { mutableStateOf(false) }

            if (showVoiceSettingsDialogInControlCenter) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showVoiceSettingsDialogInControlCenter = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .fillMaxHeight(0.9f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = DarkBackground)
                    ) {
                        KuzmixWakeWordSettingsScreen(
                            onClose = { showVoiceSettingsDialogInControlCenter = false }
                        )
                    }
                }
            }

            if (showVoiceCalibrationDialog) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showVoiceCalibrationDialog = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    KuzmixVoiceCalibrationWizard(
                        context = LocalContext.current,
                        onComplete = { showVoiceCalibrationDialog = false },
                        onDismiss = { showVoiceCalibrationDialog = false }
                    )
                }
            }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Control Center",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KuzmixYellow.copy(alpha = 0.25f))
                            .border(0.5.dp, KuzmixYellow, RoundedCornerShape(16.dp))
                            .clickable {
                                showVoiceSettingsDialogInControlCenter = true
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = "Voice Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Voice Settings", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KuzmixYellow.copy(alpha = 0.25f))
                            .border(0.5.dp, KuzmixYellow, RoundedCornerShape(16.dp))
                            .clickable {
                                showVoiceCalibrationDialog = true
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = "Calibrate Voice", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Calibrate Voice", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (onLockOS != null) {
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(KuzmixOrange.copy(alpha = 0.35f))
                                .border(0.5.dp, KuzmixOrange, RoundedCornerShape(16.dp))
                                .clickable {
                                    onClose()
                                    onLockOS()
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = "Lock OS", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Lock OS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .clickable {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) { e.printStackTrace() }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Top Section (Dual Column: Quick Connections & Media Player) Image 5
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Connections Block
                GlassmorphicContainer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            CCCircleButton(Icons.Default.AirplanemodeActive, active = false) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                            CCCircleButton(Icons.Default.SignalCellular4Bar, active = false) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            CCCircleButton(Icons.Default.Wifi, active = wifiEnabled) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                    val panelIntent = android.content.Intent(android.provider.Settings.Panel.ACTION_WIFI)
                                    panelIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(panelIntent)
                                } else {
                                    try {
                                        @Suppress("DEPRECATION")
                                        val newState = !wifiManager.isWifiEnabled
                                        @Suppress("DEPRECATION")
                                        wifiManager.isWifiEnabled = newState
                                        wifiEnabled = newState
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            }
                            CCCircleButton(Icons.Default.Bluetooth, active = bluetoothEnabled) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    }
                }

                // Media Player Card (Image 5 & Image 3)
                GlassmorphicContainer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Not Playing",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        // Playback Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(22.dp))
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(28.dp))
                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // Middle Section: Side-by-side vertical sliders and quick action pills (Image 5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Rotation Lock, Night Mode, Screen Timeout Pill
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Rotation Lock
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(if (isRotationLocked) Color.White else Color.White.copy(alpha = 0.1f), CircleShape)
                                .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .clickable { isRotationLocked = !isRotationLocked },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ScreenRotation,
                                contentDescription = "Rotation Lock",
                                tint = if (isRotationLocked) Color.DarkGray else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Dark Mode Toggle
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(if (isDarkMode) Color.White else Color.White.copy(alpha = 0.1f), CircleShape)
                                .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .clickable { isDarkMode = !isDarkMode },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NightsStay,
                                contentDescription = "Dark Mode",
                                tint = if (isDarkMode) Color.DarkGray else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Screen Timeout Pill (Image 5)
                    GlassmorphicContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        cornerRadius = 26.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("30m", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Screen Timeout", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Vertical Expansion Brightness Slider Pill (Image 5)
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                val change = -dragAmount / size.height
                                brightnessLevel = (brightnessLevel + change).coerceIn(0f, 1f)
                                try {
                                    if (android.provider.Settings.System.canWrite(context)) {
                                        val brightnessInt = (brightnessLevel * 255).toInt()
                                        android.provider.Settings.System.putInt(
                                            context.contentResolver,
                                            android.provider.Settings.System.SCREEN_BRIGHTNESS,
                                            brightnessInt
                                        )
                                    }
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(brightnessLevel)
                            .background(Color.White)
                    )
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = "Brightness",
                        tint = if (brightnessLevel > 0.35f) DarkCard else Color.White,
                        modifier = Modifier.padding(bottom = 16.dp).size(24.dp)
                    )
                }

                // Vertical Expansion Volume Slider Pill (Image 5)
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                val change = -dragAmount / size.height
                                volumeLevel = (volumeLevel + change).coerceIn(0f, 1f)
                                try {
                                    val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                                    val targetVol = (volumeLevel * maxVol).toInt()
                                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetVol, 0)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(volumeLevel)
                            .background(Color.White)
                    )
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = if (volumeLevel > 0.35f) DarkCard else Color.White,
                        modifier = Modifier.padding(bottom = 16.dp).size(24.dp)
                    )
                }
            }

            // Bottom Section: Grid of Circular Quick Action Buttons (Image 5)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CCCircleButton(Icons.Default.FlashlightOn, active = flashlightOn) {
                        flashlightOn = !flashlightOn
                        intentEngine.toggleFlashlight(flashlightOn)
                    }
                    CCCircleButton(Icons.Default.Timer, active = false) {
                        try {
                            val intent = android.content.Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    CCCircleButton(Icons.Default.Calculate, active = false) {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                                addCategory(android.content.Intent.CATEGORY_APP_CALCULATOR)
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    CCCircleButton(Icons.Default.CameraAlt, active = false) {
                        try {
                            val intent = android.content.Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CCCircleButton(Icons.Default.FiberManualRecord, active = false)
                    CCCircleButton(Icons.Default.Contrast, active = false)
                    CCCircleButton(Icons.Default.BatterySaver, active = false)
                    CCCircleButton(Icons.Default.Mic, active = false)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CCCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(if (active) Color.White else Color.White.copy(alpha = 0.1f), CircleShape)
            .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (active) DarkCard else Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun SpatialGlassClock(currentTime: Date) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            try {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2f

                // Draw solid white clock dial
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.95f,
                    center = center
                )

                // Draw modern, subtle silver bezel outline
                drawCircle(
                    color = Color(0xFFD1D1D6),
                    radius = radius * 0.95f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                )

                // Hour Numerals 1 to 12
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#1C1C1E")
                    textSize = 10.5.dp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                }

                val keyNumbers = mapOf(
                    0 to "12", 1 to "1", 2 to "2", 3 to "3", 4 to "4", 5 to "5",
                    6 to "6", 7 to "7", 8 to "8", 9 to "9", 10 to "10", 11 to "11"
                )

                for (i in 0..11) {
                    val angle = Math.PI * i / 6.0
                    val numRadius = radius * 0.72f
                    val x = center.x + numRadius * kotlin.math.sin(angle).toFloat()
                    val y = center.y - numRadius * kotlin.math.cos(angle).toFloat() + 3.8.dp.toPx()
                    drawContext.canvas.nativeCanvas.drawText(keyNumbers[i] ?: "$i", x, y, textPaint)
}

                // Tiny tick marks
                for (minute in 0..59) {
                    if (minute % 5 != 0) {
                        val angle = Math.PI * minute / 30.0
                        val startR = radius * 0.88f
                        val endR = radius * 0.92f
                        val startX = center.x + startR * kotlin.math.sin(angle).toFloat()
                        val startY = center.y - startR * kotlin.math.cos(angle).toFloat()
                        val endX = center.x + endR * kotlin.math.sin(angle).toFloat()
                        val endY = center.y - endR * kotlin.math.cos(angle).toFloat()
                        drawLine(
                            color = Color(0xFF8E8E93),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 0.5.dp.toPx()
                        )
                    }
                }

                val calendar = Calendar.getInstance().apply { time = currentTime }
                val hours = calendar.get(Calendar.HOUR)
                val minutes = calendar.get(Calendar.MINUTE)
                val seconds = calendar.get(Calendar.SECOND)

                val hourAngle = Math.PI * (hours + minutes / 60.0) / 6.0
                val minAngle = Math.PI * (minutes + seconds / 60.0) / 30.0
                val secAngle = Math.PI * seconds / 30.0

                // Hour hand (thick, dark)
drawLine(
                    color = Color(0xFF1C1C1E),
                    start = center,
                    end = Offset(
                        center.x + (radius * 0.48f) * kotlin.math.sin(hourAngle).toFloat(),
                        center.y - (radius * 0.48f) * kotlin.math.cos(hourAngle).toFloat()
                    ),
                    strokeWidth = 3.2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Minute hand (thinner, dark, longer)
drawLine(
                    color = Color(0xFF1C1C1E),
                    start = center,
                    end = Offset(
                        center.x + (radius * 0.70f) * kotlin.math.sin(minAngle).toFloat(),
                        center.y - (radius * 0.70f) * kotlin.math.cos(minAngle).toFloat()
                    ),
                    strokeWidth = 2.0.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Second hand (thin, red, long)
                drawLine(
                    color = Color(0xFFFF3B30),
start = center,
                    end = Offset(
                        center.x + (radius * 0.82f) * kotlin.math.sin(secAngle).toFloat(),
                        center.y - (radius * 0.82f) * kotlin.math.cos(secAngle).toFloat()
                    ),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Center pin
                drawCircle(Color(0xFFFF3B30), radius = 3.5.dp.toPx(), center = center)
drawCircle(Color(0xFF1C1C1E), radius = 1.0.dp.toPx(), center = center)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun CalendarWidget(currentTime: Date) {
    val calendar = remember(currentTime) {
        Calendar.getInstance().apply { time = currentTime }
    }
    val headerText = remember(currentTime) {
        val monthStr = java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(currentTime).uppercase()
        val yearStr = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(currentTime)
        "$monthStr $yearStr"
}
    val todayDay = calendar.get(Calendar.DAY_OF_MONTH)

    val firstDayOffset = remember(currentTime) {
        val cal = Calendar.getInstance().apply {
            time = currentTime
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    val daysInMonth = remember(currentTime) {
        val cal = Calendar.getInstance().apply { time = currentTime }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val daysList = remember(firstDayOffset, daysInMonth) {
        val emptySlots = List(firstDayOffset) { "" }
        val dayNumbers = (1..daysInMonth).map { it.toString() }
        (emptySlots + dayNumbers).chunked(7)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
modifier = Modifier.padding(start = 2.dp)
            )
        }

        val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        daysList.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { dayStr ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayStr.isNotEmpty()) {
                            val isToday = dayStr.toIntOrNull() == todayDay
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(0xFFFF3B30), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayStr,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    text = dayStr,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(20.dp))
                        }
                    }
                }
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherWidget(weatherManager: WeatherManager) {
    val context = LocalContext.current
    val weatherData by weatherManager.weatherState.collectAsState()
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        scope.launch {
            weatherManager.fetchCurrentWeather()
        }
    }

    LaunchedEffect(Unit) {
        weatherManager.fetchCurrentWeather()
    }

    val weatherIcon = when (weatherData.weatherCode) {
        0, 1 -> Icons.Default.WbSunny
        2, 3 -> Icons.Default.Cloud
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Default.Grain
        71, 73, 75, 85, 86 -> Icons.Default.AcUnit
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.WbCloudy
    }

    val iconColor = when (weatherData.weatherCode) {
        0, 1 -> Color(0xFFFFB300)
        2, 3 -> Color(0xFF81D4FA)
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Color(0xFF29B6F6)
        71, 73, 75, 85, 86 -> Color(0xFFE0F7FA)
        95, 96, 99 -> Color(0xFFFF7043)
        else -> Color(0xFFFFCA28)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (!hasFine) {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    scope.launch {
                        weatherManager.fetchCurrentWeather()
                    }
                }
            }
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        // Location Pin + City Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFFFF453A),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = weatherData.cityName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (weatherData.isFetching) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    color = Color.White,
                    strokeWidth = 1.2.dp
                )
            }
        }

        // Temp + Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = weatherData.displayTemp,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable {
                        weatherManager.toggleTempUnit()
                    }
                )
                Text(
                    text = weatherData.condition,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconColor.copy(alpha = 0.22f), CircleShape)
                    .border(0.5.dp, iconColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = weatherIcon,
                    contentDescription = weatherData.condition,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Humidity & Wind Speed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hum: ${weatherData.humidity}%",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${weatherData.windSpeedKmH.toInt()} km/h",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FloatingLiquidDock(
    dockApps: List<AppItem>,
    hardwareTier: HardwareTier,
    onClick: (AppItem) -> Unit,
    onLongClick: ((AppItem) -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
            .height(88.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.28f),
shape = RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockApps.take(4).forEachIndexed { index, app ->
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
) {
                    GlassAppIcon(
                        app = app,
                        isDock = true,
                        animationDelay = index * 40,
onClick = { onClick(app) },
                        onLongClick = { onLongClick?.invoke(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawCustomIcon(packageName: String, scaleFactor: Float, maskShape: Shape) {
    val pkg = packageName.lowercase()

    val todayDayOfWeek = remember {
        val cal = java.util.Calendar.getInstance()
        val format = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        format.format(cal.time).uppercase()
    }
    val todayDayStr = remember {
        val cal = java.util.Calendar.getInstance()
        cal.get(java.util.Calendar.DAY_OF_MONTH).toString()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            pkg.contains("settings") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFFE53935), Color(0xFFC62828))), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("weather") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF29B6F6), Color(0xFF0288D1))), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("calendar") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color.White, CircleShape)
                        .border(0.5.dp, Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = todayDayOfWeek,
                            color = Color(0xFFFF3B30),
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 7.sp
                        )
                        Text(
                            text = todayDayStr,
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
            pkg.contains("photos") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color.White, CircleShape)
                        .border(0.5.dp, Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(18.dp)) {
                        val r = size.minDimension / 2f
                        val colors = listOf(
                            Color(0xFFFF2A68), Color(0xFFFF5E3A), Color(0xFFFFCD02), Color(0xFF4CD964),
                            Color(0xFF5AC8FA), Color(0xFF007AFF), Color(0xFF5856D6), Color(0xFFC644FC)
                        )
                        for (i in 0..7) {
                            val angle = i * 45f
                            rotate(angle) {
                                drawOval(
                                    color = colors[i],
                                    topLeft = Offset(center.x - r * 0.3f, center.y - r * 0.9f),
                                    size = androidx.compose.ui.geometry.Size(r * 0.6f, r * 1.0f),
                                    alpha = 0.85f
                                )
                            }
                        }
                    }
                }
            }
            pkg.contains("calculator") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFF333333), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("clock") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFF1C1C1E), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val r = size.minDimension / 2f
                        drawCircle(Color(0xFF2C2C2E), radius = r)
                        drawCircle(Color.White.copy(alpha = 0.3f), radius = r, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5.dp.toPx()))
                        val hourAngle = Math.PI * 10 / 6.0
                        val minAngle = Math.PI * 2 / 6.0
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = Offset(center.x + r * 0.5f * kotlin.math.sin(hourAngle).toFloat(), center.y - r * 0.5f * kotlin.math.cos(hourAngle).toFloat()),
                            strokeWidth = 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = Offset(center.x + r * 0.75f * kotlin.math.sin(minAngle).toFloat(), center.y - r * 0.75f * kotlin.math.cos(minAngle).toFloat()),
                            strokeWidth = 1.0.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(KuzmixOrange, radius = 1.dp.toPx())
                    }
                }
            }
            pkg.contains("themes") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF8E24AA), Color(0xFF5E35B1))), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            pkg.contains("apps") || pkg.contains("google") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(3.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                            Box(modifier = Modifier.size(11.dp).background(Color(0xFFEA4335), CircleShape))
                            Box(modifier = Modifier.size(11.dp).background(Color(0xFFFF0000), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(5.dp))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                            Box(modifier = Modifier.size(11.dp).background(Color(0xFF34A853), CircleShape))
                            Box(modifier = Modifier.size(11.dp).background(Color(0xFF4285F4), CircleShape))
                        }
                    }
                }
            }
            pkg.contains("whatsapp") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFF25D366), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("aichat") -> {
                Box(
                    modifier = Modifier.size(33.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KuzmixBrandLogo(
modifier = Modifier.fillMaxSize()
                    )
                }
            }
            pkg.contains("controlcenter") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFF455A64), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsInputComponent,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("inshot") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFFE91E63), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("facebook") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFF1877F2), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "f",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            pkg.contains("snapchat") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFFFFFC00), CircleShape)
                        .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfied,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("capcut") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color.Black, CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("pinterest") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFFE60023), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
            pkg.contains("opay") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color(0xFF00B074), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp)) {
                        drawCircle(Color.White, radius = size.minDimension / 2f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx()))
                    }
                }
            }
            pkg.contains("mtmanager") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color.White, CircleShape)
                        .border(0.5.dp, Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Color(0xFF7CB342),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("utilities") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(KuzmixYellow, KuzmixYellow)), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("support") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(KuzmixOrange, KuzmixYellow)), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("xender") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(KuzmixYellow, KuzmixYellow)), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapCalls,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("dialer") || pkg.contains("phone") || pkg.contains("contacts") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("messaging") || pkg.contains("mms") || pkg.contains("message") || pkg.contains("sms") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF2196F3), Color(0xFF1E88E5))), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            pkg.contains("camera") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF757575), Color(0xFF212121))), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            pkg.contains("vending") || pkg.contains("play") || pkg.contains("market") -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Color.White, CircleShape)
                        .border(0.5.dp, Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF00ACC1),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF607D8B), Color(0xFF37474F))), CircleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassAppIcon(
    app: AppItem,
    isDock: Boolean = false,
    animationDelay: Int = 0,
    isEditMode: Boolean = false,
    isFocused: Boolean = false,
    isDimmed: Boolean = false,
    customLabel: String? = null,
    iconMaskKey: String? = null,
    iconScaleFactor: Float? = null,
    onRemoveClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val effectiveMaskKey = remember(iconMaskKey) {
        iconMaskKey ?: context.getSharedPreferences("kuzmix_home_prefs", Context.MODE_PRIVATE)
            .getString("icon_mask_shape", "squircle") ?: "squircle"
    }
    val effectiveScaleFactor = remember(iconScaleFactor) {
        iconScaleFactor ?: context.getSharedPreferences("kuzmix_home_prefs", Context.MODE_PRIVATE)
            .getFloat("icon_scale_factor", 1.0f)
    }
    val maskShape = remember(effectiveMaskKey) { getIconMaskShape(effectiveMaskKey) }

    val displayLabel = customLabel ?: app.label
    val imageBitmap = remember(app.icon) { app.icon.toImageBitmap() }
    val size = if (isDock) 52.dp else 56.dp

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(app.packageName) {
        if (animationDelay > 0) {
            delay(animationDelay.toLong())
        }
        visible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val seed = remember(app.packageName) { 
        val h = app.packageName.hashCode()
        if (h < 0) -h else h
    }
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = if (seed % 2 == 0) -1.8f else 1.8f,
        targetValue = if (seed % 2 == 0) 1.8f else -1.8f,
animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 110 + (seed % 35), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotationAngle"
    )

    val translationYAnim by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 130 + (seed % 25), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translationY"
    )

    val currentRotation = if (isEditMode && !isDock) rotationAngle else 0f
    val currentTranslationY = if (isEditMode && !isDock) translationYAnim else 0f

    val scale by animateFloatAsState(
        targetValue = when {
            isFocused -> 1.25f
            isDimmed -> 0.85f
            visible -> 1f
            else -> 0.4f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = when {
            isDimmed -> 0.35f
            visible -> 1f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 250),
        label = "alpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "offsetY"
    )

    Box(
        contentAlignment = Alignment.TopStart,
modifier = Modifier.padding(top = if (isEditMode && !isDock) 4.dp else 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha,
                    rotationZ = currentRotation,
                    translationY = with(LocalDensity.current) { (offsetY.toPx() + currentTranslationY.dp.toPx()) }
                )
                .combinedClickable(
                    onClick = {
                        try {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                        } catch (_: Exception) {}
                        if (isEditMode && !isDock) {
                            onRemoveClick?.invoke()
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = {
                        onLongClick?.invoke()
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(maskShape)
                    .background(
                        if (isFocused)
                            Color.White.copy(alpha = 0.25f)
                        else
                            Color(0xAA1C1C1E) // Premium dark glassmorphism backing
                    )
                    .border(
                        width = if (isFocused) 1.2.dp else 0.5.dp,
                        brush = if (isFocused)
                            Brush.linearGradient(listOf(KuzmixYellow, KuzmixOrange))
                        else
                            SolidColor(Color.White.copy(alpha = 0.16f)), // Fine, high-contrast silver border
                        shape = maskShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val isCustomVirtual = remember(app.packageName) {
                    val pkg = app.packageName.lowercase()
                    pkg.contains("settings") ||
                    pkg.contains("weather") ||
                    pkg.contains("calendar") ||
                    pkg.contains("photos") ||
                    pkg.contains("calculator") ||
                    pkg.contains("clock") ||
                    pkg.contains("themes") ||
                    pkg.contains("apps") ||
                    pkg.contains("google") ||
                    pkg.contains("whatsapp") ||
                    pkg.contains("aichat") ||
                    pkg.contains("controlcenter") ||
                    pkg.contains("inshot") ||
                    pkg.contains("facebook") ||
                    pkg.contains("snapchat") ||
                    pkg.contains("capcut") ||
                    pkg.contains("pinterest") ||
                    pkg.contains("opay") ||
                    pkg.contains("mtmanager") ||
                    pkg.contains("xender") ||
                    pkg.contains("dialer") ||
                    pkg.contains("phone") ||
                    pkg.contains("contacts") ||
                    pkg.contains("messaging") ||
                    pkg.contains("mms") ||
                    pkg.contains("message") ||
                    pkg.contains("sms") ||
                    pkg.contains("camera") ||
                    pkg.contains("vending") ||
                    pkg.contains("play") ||
                    pkg.contains("market")
                }

                if (isCustomVirtual) {
                    DrawCustomIcon(packageName = app.packageName, scaleFactor = effectiveScaleFactor, maskShape = maskShape)
                } else {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = displayLabel,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = effectiveScaleFactor
                                scaleY = effectiveScaleFactor
                            }
                            .clip(maskShape)
                            .padding(4.dp)
                    )
                }
            }

            if (!isDock) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = displayLabel,
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), blurRadius = 4f)
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }

        if (isEditMode && !isDock && onRemoveClick != null) {
            Box(
                modifier = Modifier
                    .offset(x = (-4).dp, y = (-6).dp)
.size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
                    .border(1.dp, Color.White, CircleShape)
                    .clickable { onRemoveClick() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(2.dp)
                        .background(Color.White, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Composable
fun CategoryFolderCard(
    categoryName: String,
    apps: List<AppItem>,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val iconMaskKey = remember {
        context.getSharedPreferences("kuzmix_home_prefs", Context.MODE_PRIVATE)
            .getString("icon_mask_shape", "squircle") ?: "squircle"
    }
    val maskShape = remember(iconMaskKey) { getIconMaskShape(iconMaskKey) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF1C1C1E))
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val miniApps = apps.take(4)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { index ->
                        val app = miniApps.getOrNull(index)
                        if (app != null) {
                            val img = remember(app.icon) { app.icon.toImageBitmap() }
                            Image(
                                bitmap = img,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(44.dp)
.clip(maskShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(maskShape)
                                    .background(Color.White.copy(alpha = 0.06f))
)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { index ->
                        val app = miniApps.getOrNull(index + 2)
                        if (app != null) {
                            val img = remember(app.icon) { app.icon.toImageBitmap() }
                            Image(
                                bitmap = img,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(44.dp)
.clip(maskShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(maskShape)
                                    .background(Color.White.copy(alpha = 0.06f))
)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = categoryName,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategorizedAppLibraryPage(
    hardwareTier: HardwareTier,
    apps: List<AppItem>,
    onSearchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val homePrefs = remember { context.getSharedPreferences("kuzmix_home_prefs", Context.MODE_PRIVATE) }
    val cleanApps = remember(apps) { apps.filterNot { it.packageName == context.packageName } }
    
    val categories = remember(cleanApps) {
        val socialKeywords = listOf("social", "chat", "message", "whatsapp", "telegram", "facebook", "insta", "snap", "twitter", "x", "discord", "tiktok", "wechat", "line", "signal", "sms", "mms")
        val productivityKeywords = listOf("mail", "calendar", "clock", "doc", "sheet", "slide", "keep", "note", "task", "drive", "calculator", "chrome", "browser", "office", "adobe", "pdf", "file", "download")
        val gamesKeywords = listOf("game", "play", "puzzle", "arcade", "race", "rpg", "quest", "board", "chess")
        val entertainmentKeywords = listOf("music", "audio", "sound", "spotify", "ytmusic", "recorder", "podcasts", "video", "movie", "youtube", "netflix", "player", "tv", "camera", "photos", "gallery")

        val social = cleanApps.filter { app ->
            val pkg = app.packageName.lowercase()
            val lbl = app.label.lowercase()
            socialKeywords.any { pkg.contains(it) || lbl.contains(it) }
        }
val prod = cleanApps.filter { app ->
            val pkg = app.packageName.lowercase()
            val lbl = app.label.lowercase()
            productivityKeywords.any { pkg.contains(it) || lbl.contains(it) } && app !in social
        }

        val games = cleanApps.filter { app ->
            val pkg = app.packageName.lowercase()
            val lbl = app.label.lowercase()
            gamesKeywords.any { pkg.contains(it) || lbl.contains(it) } && app !in social && app !in prod
        }

        val media = cleanApps.filter { app ->
            val pkg = app.packageName.lowercase()
            val lbl = app.label.lowercase()
            entertainmentKeywords.any { pkg.contains(it) || lbl.contains(it) } && app !in social && app !in prod && app !in games
        }

        val hiddenPackages = homePrefs.getStringSet("hidden_packages", emptySet<String>()) ?: emptySet<String>()
        val hiddenApps = cleanApps.filter { it.packageName in hiddenPackages }

        val allCategorized = (social + prod + games + media + hiddenApps).map { it.packageName }.toSet()
        val other = cleanApps.filter { it.packageName !in allCategorized }

        val list = mutableListOf<Pair<String, List<AppItem>>>()
        if (social.isNotEmpty()) list.add("Social" to social)
        if (prod.isNotEmpty()) list.add("Productivity" to prod)
        if (games.isNotEmpty()) list.add("Games" to games)
        if (media.isNotEmpty()) list.add("Entertainment" to media)
        if (hiddenApps.isNotEmpty()) list.add("Hidden" to hiddenApps)
        if (other.isNotEmpty()) list.add("Utilities & Other" to other)
        
        if (list.isEmpty()) {
            list.add("All Apps" to cleanApps)
        }
        list
}

    var activeFolderCategory by remember { mutableStateOf<Pair<String, List<AppItem>>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top search pill: "🔍 App Library"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSearchClick() }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔍", fontSize = 13.sp)
                    Text(
                        text = "App Library",
                        color = Color(0xFFEBEBF5).copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2-column grid of 2x2 folder categories (100% ad-free)
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    CategoryFolderCard(
                        categoryName = cat.first,
                        apps = cat.second,
                        onClick = {
                            activeFolderCategory = cat
                        }
                    )
}
            }
        }

        activeFolderCategory?.let { folder ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
.clickable { activeFolderCategory = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.68f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1C1C1E))
                        .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                        .clickable(enabled = false) {}
                        .padding(20.dp),
horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = folder.first,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        androidx.compose.material3.IconButton(
                            onClick = { activeFolderCategory = null }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (folder.second.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No apps in this category",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
modifier = Modifier.fillMaxSize()
                        ) {
                            val list = folder.second
                            items(list.size) { index ->
                                val app = list[index]
                                Box(
                                    modifier = Modifier.padding(4.dp),
contentAlignment = Alignment.Center
                                ) {
                                    GlassAppIcon(
                                        app = app,
                                        animationDelay = index * 30,
onClick = {
                                            try {
                                                context.startActivity(app.intent)
                                                activeFolderCategory = null
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassmorphicContainer(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    glassColor: Color? = null,
    borderColor: Color? = null,
    sensorState: AeroGlassSensorState? = null,
    content: @Composable () -> Unit
) {
    val glassAlpha = sensorState?.glassAlpha ?: 0.10f
    val borderAlpha = sensorState?.borderAlpha ?: 0.30f
    val specX = sensorState?.specularX ?: 0.5f
    val specY = sensorState?.specularY ?: 0.2f

    val bgBrush = if (glassColor != null) {
        SolidColor(glassColor)
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (glassAlpha * 1.3f).coerceAtMost(0.40f)),
                Color.White.copy(alpha = (glassAlpha * 0.5f).coerceAtLeast(0.03f))
            )
        )
    }

    val borderBrush = if (borderColor != null) {
        SolidColor(borderColor)
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (borderAlpha * 1.2f).coerceAtMost(0.85f)),
                Color.White.copy(alpha = (borderAlpha * 0.3f).coerceAtLeast(0.06f))
            ),
            start = androidx.compose.ui.geometry.Offset(specX * 250f, specY * 250f),
            end = androidx.compose.ui.geometry.Offset((1f - specX) * 250f, (1f - specY) * 250f)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = bgBrush,
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = 0.8.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun FailsafeWidgetContainer(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable () -> Unit
) {
    GlassmorphicContainer(
        modifier = modifier,
        cornerRadius = cornerRadius
    ) {
        content()
    }
}

