package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkCard

data class PresetAsset(
    val title: String,
    val description: String,
    val url: String,
    val type: String // "image" or "video"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuzmixStudioOverlay(
    hardwareTier: HardwareTier,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val apiKey = remember {
        val keyManager = OpenRouterKeyManager(context)
        keyManager.getActiveKey()
    }

    // Tabs: 0 -> Image Studio, 1 -> Video Studio, 2 -> AI Vision & Analysis
    var selectedTab by remember { mutableStateOf(0) }

    // Preset Assets
    val presetImages = remember {
        listOf(
            PresetAsset("Quantum City", "Cyberpunk metropolis with holographic structures", "https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?w=600", "image"),
            PresetAsset("Neural Gateway", "Quantum brain interface radiating cosmic violet light", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600", "image"),
            PresetAsset("Nebula station", "An orbital sci-fi research outpost suspended in space", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600", "image")
        )
    }

    val presetVideos = remember {
        listOf(
            PresetAsset("Neon Orbit", "Mesmerizing spinning orbital technological rings", "https://assets.mixkit.co/videos/preview/mixkit-matrix-style-code-screen-background-32757-large.mp4", "video"),
            PresetAsset("Cosmic Storm", "Interstellar storm swirling inside a blue singularity", "https://assets.mixkit.co/videos/preview/mixkit-light-leaks-and-abstract-space-nebula-39659-large.mp4", "video")
        )
    }

    // Image Gen State
    var imgPrompt by remember { mutableStateOf("") }
    var imgAspectRatio by remember { mutableStateOf("16:9") }
    var imgModelQuality by remember { mutableStateOf("general") } // "general" or "studio"
    var generatedImageUrl by remember { mutableStateOf<String?>(null) }
    var isGeneratingImage by remember { mutableStateOf(false) }
    var imgStatusText by remember { mutableStateOf("") }

    // Video Gen State
    var videoPrompt by remember { mutableStateOf("") }
    var videoAspectRatio by remember { mutableStateOf("16:9") }
    var isImageToVideo by remember { mutableStateOf(false) }
    var selectedAnimImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAnimImageBase64 by remember { mutableStateOf<String?>(null) }
    var generatedVideoUrl by remember { mutableStateOf<String?>(null) }
    var isGeneratingVideo by remember { mutableStateOf(false) }
    var videoStatusText by remember { mutableStateOf("") }

    // Vision / Analysis State
    var analysisMode by remember { mutableStateOf("image") } // "image" or "video"
    var analysisPrompt by remember { mutableStateOf("Describe this media in detail, highlighting unique technical aspects.") }
    var selectedAnalysisMediaUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAnalysisMediaBase64 by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf("") }

    // Launcher for Custom Images (for Video Animation & Vision Analysis)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (selectedTab == 1) {
                selectedAnimImageUri = uri
                scope.launch(Dispatchers.IO) {
                    val base64 = uriToBase64(context, uri)
                    withContext(Dispatchers.Main) {
                        selectedAnimImageBase64 = base64
                    }
                }
            } else {
                selectedAnalysisMediaUri = uri
                scope.launch(Dispatchers.IO) {
                    val base64 = uriToBase64(context, uri)
                    withContext(Dispatchers.Main) {
                        selectedAnalysisMediaBase64 = base64
                    }
                }
            }
        }
    }

    // Launcher for Custom Videos (for Vision Analysis)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAnalysisMediaUri = uri
            scope.launch(Dispatchers.IO) {
                val base64 = uriToBase64(context, uri)
                withContext(Dispatchers.Main) {
                    selectedAnalysisMediaBase64 = base64
                }
            }
        }
    }

    // Main App Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040614),
                        Color(0xFF0A0E29),
                        Color(0xFF020308)
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(KuzmixOrange.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, KuzmixOrange.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MovieFilter,
                            contentDescription = "Kuzmix Studio",
                            tint = KuzmixOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kuzmix Studio",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Neural Content Engine • OpenRouter Image & Video AI",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable { onClose() }
                        .testTag("close_studio_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Elegant Custom Glass Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val tabTitles = listOf("Image Gen", "Video Studio", "AI Analyzer")
                val tabIcons = listOf(Icons.Default.Photo, Icons.Default.Videocam, Icons.Default.Analytics)

                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) KuzmixYellow.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isSelected) KuzmixYellow.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedTab = index
                                keyboardController?.hide()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                tabIcons[index],
                                contentDescription = title,
                                tint = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Main Content Area with Dynamic Swapping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                when (selectedTab) {
                    0 -> ImageGenTab(
                        prompt = imgPrompt,
                        onPromptChange = { imgPrompt = it },
                        aspectRatio = imgAspectRatio,
                        onAspectRatioChange = { imgAspectRatio = it },
                        quality = imgModelQuality,
                        onQualityChange = { imgModelQuality = it },
                        generatedImageUrl = generatedImageUrl,
                        isGenerating = isGeneratingImage,
                        statusText = imgStatusText,
                        onGenerate = {
                            keyboardController?.hide()
                            isGeneratingImage = true
                            imgStatusText = "Synthesizing cosmic particles..."
                            generatedImageUrl = null
                            scope.launch {
                                try {
                                    val imgQuality = if (imgModelQuality == "studio") "high" else "low"
                                    val result = generateImageWithOpenRouter(context, apiKey, imgPrompt, imgAspectRatio, imgQuality)
                                    if (result != null) {
                                        generatedImageUrl = result
                                        imgStatusText = "Synthesis successful!"
                                    } else {
                                        imgStatusText = "Image generation failed. Please try again."
                                    }
                                } catch (e: Exception) {
                                    imgStatusText = "Error: ${e.localizedMessage}"
                                } finally {
                                    isGeneratingImage = false
                                }
                            }
                        },
                        onSaveToGallery = { url ->
                            saveImageToGallery(context, url) { status ->
                                Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                            }
                        },
                        onSetAsWallpaper = { url ->
                            setAsWallpaper(context, url) { status ->
                                Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    1 -> VideoStudioTab(
                        prompt = videoPrompt,
                        onPromptChange = { videoPrompt = it },
                        aspectRatio = videoAspectRatio,
                        onAspectRatioChange = { videoAspectRatio = it },
                        isImageToVideo = isImageToVideo,
                        onIsImageToVideoChange = {
                            isImageToVideo = it
                            if (it) {
                                if (selectedAnimImageUri == null && presetImages.isNotEmpty()) {
                                    selectedAnimImageUri = Uri.parse(presetImages[0].url)
                                }
                            }
                        },
                        selectedImageUri = selectedAnimImageUri,
                        onSelectImageClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        presetImages = presetImages,
                        onSelectPresetImage = { asset ->
                            selectedAnimImageUri = Uri.parse(asset.url)
                            selectedAnimImageBase64 = null
                        },
                        generatedVideoUrl = generatedVideoUrl,
                        isGenerating = isGeneratingVideo,
                        statusText = videoStatusText,
                        onGenerate = {
                            keyboardController?.hide()
                            isGeneratingVideo = true
                            videoStatusText = "Forging cinematic fluid dimensions..."
                            generatedVideoUrl = null
                            scope.launch {
                                try {
                                    videoStatusText = "Submitting video generation request..."
                                    val result = generateVideoWithOpenRouter(context, apiKey, videoPrompt, videoAspectRatio, selectedAnimImageBase64)
                                    if (result != null) {
                                        generatedVideoUrl = result
                                        videoStatusText = "Video forged successfully!"
                                    } else {
                                        videoStatusText = "Video generation failed. Please try again."
                                    }
                                } catch (e: Exception) {
                                    videoStatusText = "Error: ${e.localizedMessage}"
                                } finally {
                                    isGeneratingVideo = false
                                }
                            }
                        },
                        onSaveVideoToGallery = { url ->
                            saveVideoToGallery(context, url) { status ->
                                Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                            }
                        },
                        onSetLiveWallpaper = { url ->
                            setLiveWallpaper(context, url) { status ->
                                Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    2 -> AnalyzerTab(
                        mode = analysisMode,
                        onModeChange = {
                            analysisMode = it
                            selectedAnalysisMediaUri = null
                            selectedAnalysisMediaBase64 = null
                            analysisResult = ""
                        },
                        prompt = analysisPrompt,
                        onPromptChange = { analysisPrompt = it },
                        selectedMediaUri = selectedAnalysisMediaUri,
                        onSelectMediaClick = {
                            if (analysisMode == "image") {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                videoPickerLauncher.launch("video/*")
                            }
                        },
                        presetAssets = if (analysisMode == "image") presetImages else presetVideos.map { PresetAsset(it.title, it.description, it.url, "video") },
                        onSelectPreset = { asset ->
                            selectedAnalysisMediaUri = Uri.parse(asset.url)
                            selectedAnalysisMediaBase64 = null
                        },
                        isAnalyzing = isAnalyzing,
                        analysisResult = analysisResult,
                        onAnalyze = {
                            keyboardController?.hide()
                            isAnalyzing = true
                            analysisResult = "Decrypting media metadata via OpenRouter..."
                            scope.launch {
                                try {
                                    val result = analyzeMediaWithOpenRouter(
                                        context,
                                        apiKey,
                                        analysisPrompt,
                                        selectedAnalysisMediaUri,
                                        selectedAnalysisMediaBase64,
                                        analysisMode
                                    )
                                    analysisResult = result ?: "Failed to extract key metadata from selected media."
                                } catch (e: Exception) {
                                    analysisResult = "Spectral analysis failed: ${e.localizedMessage}"
                                } finally {
                                    isAnalyzing = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- Image Studio Composable ---
@Composable
fun ImageGenTab(
    prompt: String,
    onPromptChange: (String) -> Unit,
    aspectRatio: String,
    onAspectRatioChange: (String) -> Unit,
    quality: String,
    onQualityChange: (String) -> Unit,
    generatedImageUrl: String?,
    isGenerating: Boolean,
    statusText: String,
    onGenerate: () -> Unit,
    onSaveToGallery: (String) -> Unit,
    onSetAsWallpaper: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prompt Input
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("image_prompt_input"),
            label = { Text("What do you want to generate?", color = Color.White.copy(alpha = 0.6f)) },
            placeholder = { Text("e.g. A gorgeous cyberpunk space station overlooking a violet nebula", color = Color.White.copy(alpha = 0.3f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = KuzmixYellow,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedContainerColor = DarkCard.copy(alpha = 0.6f),
                unfocusedContainerColor = DarkCard.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp),
            maxLines = 4
        )

        // Aspect Ratio Selector
        Text("Select Aspect Ratio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val aspectRatios = listOf("1:1", "2:3", "3:2", "3:4", "4:3", "9:16", "16:9", "21:9")
            aspectRatios.forEach { ratio ->
                val isSelected = aspectRatio == ratio
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.08f))
                        .clickable { onAspectRatioChange(ratio) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("aspect_ratio_$ratio"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ratio,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quality Engine Selector
        Text("Model Engine Quality", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val engines = listOf("general" to "General (Flash 3.1)", "studio" to "Studio (Pro 3.0)")
            engines.forEach { (engineKey, engineLabel) ->
                val isSelected = quality == engineKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) KuzmixOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                        .border(1.dp, if (isSelected) KuzmixOrange else Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                        .clickable { onQualityChange(engineKey) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = engineLabel,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Generate Button
        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_image_button"),
            colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow),
            shape = RoundedCornerShape(16.dp),
            enabled = prompt.isNotBlank() && !isGenerating
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(statusText, color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Cosmic Asset", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Viewport Result
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (generatedImageUrl != null) {
                AsyncImage(
                    model = generatedImageUrl,
                    contentDescription = "Generated Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (isGenerating) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = KuzmixYellow)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Synthesizing dimension...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Default.Brush, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Canvas Empty", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Your futuristic image will be forged here.", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }

        // Action Drawer
        if (generatedImageUrl != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onSaveToGallery(generatedImageUrl) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_image_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Asset", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { onSetAsWallpaper(generatedImageUrl) },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("set_wallpaper_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, KuzmixYellow)
                ) {
                    Icon(Icons.Default.Wallpaper, contentDescription = null, tint = KuzmixYellow)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set Wallpaper", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

// --- Video Studio Composable ---
@Composable
fun VideoStudioTab(
    prompt: String,
    onPromptChange: (String) -> Unit,
    aspectRatio: String,
    onAspectRatioChange: (String) -> Unit,
    isImageToVideo: Boolean,
    onIsImageToVideoChange: (Boolean) -> Unit,
    selectedImageUri: Uri?,
    onSelectImageClick: () -> Unit,
    presetImages: List<PresetAsset>,
    onSelectPresetImage: (PresetAsset) -> Unit,
    generatedVideoUrl: String?,
    isGenerating: Boolean,
    statusText: String,
    onGenerate: () -> Unit,
    onSaveVideoToGallery: (String) -> Unit,
    onSetLiveWallpaper: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isImageToVideo) KuzmixOrange.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onIsImageToVideoChange(false) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Text-to-Video (Veo 3)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isImageToVideo) KuzmixOrange.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onIsImageToVideoChange(true) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Animate Image (Veo 3)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Image to Video Selection Area
        if (isImageToVideo) {
            Text("Select Image to Animate", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            
            // Chosen preview image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Swap Image", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = KuzmixOrange, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Upload Custom Photo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Pick from your device gallery", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    }
                }
            }

            // Quick Presets Selector
            Text("Or Select Tech Presets", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetImages.forEach { asset ->
                    val isSelected = selectedImageUri?.toString() == asset.url
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, if (isSelected) KuzmixOrange else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { onSelectPresetImage(asset) }
                    ) {
                        AsyncImage(
                            model = asset.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Motion Description / Prompt
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("video_prompt_input"),
            label = { Text("Describe the video motion / direction", color = Color.White.copy(alpha = 0.6f)) },
            placeholder = { Text("e.g. Dynamic camera panning left, glowing particles swirling forward", color = Color.White.copy(alpha = 0.3f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = KuzmixYellow,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedContainerColor = DarkCard.copy(alpha = 0.6f),
                unfocusedContainerColor = DarkCard.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp),
            maxLines = 3
        )

        // Aspect Ratio Selector (16:9 or 9:16)
        Text("Veo Video Aspect Ratio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("16:9" to "Landscape (16:9)", "9:16" to "Portrait (9:16)").forEach { (ratio, label) ->
                val isSelected = aspectRatio == ratio
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) KuzmixYellow.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                        .border(1.dp, if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                        .clickable { onAspectRatioChange(ratio) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Generate Video Button
        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_video_button"),
            colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange),
            shape = RoundedCornerShape(16.dp),
            enabled = prompt.isNotBlank() && !isGenerating
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(statusText, color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Forge Veo 3 Dimension", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Video Player Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (aspectRatio == "9:16") 0.5625f else 1.777f)
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (generatedVideoUrl != null) {
                KuzmixStudioVideoPlayer(videoUrl = generatedVideoUrl)
            } else if (isGenerating) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = KuzmixOrange)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Rendering spacetime...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dimension Empty", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Generated Veo 3 animation will stream here.", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }

        // Action Drawer
        if (generatedVideoUrl != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onSaveVideoToGallery(generatedVideoUrl) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_video_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Video", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { onSetLiveWallpaper(generatedVideoUrl) },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("set_live_wallpaper_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, KuzmixOrange)
                ) {
                    Icon(Icons.Default.OfflineBolt, contentDescription = null, tint = KuzmixOrange)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set Live Wallpaper", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

// --- Vision / Analyzer Composable ---
@Composable
fun AnalyzerTab(
    mode: String,
    onModeChange: (String) -> Unit,
    prompt: String,
    onPromptChange: (String) -> Unit,
    selectedMediaUri: Uri?,
    onSelectMediaClick: () -> Unit,
    presetAssets: List<PresetAsset>,
    onSelectPreset: (PresetAsset) -> Unit,
    isAnalyzing: Boolean,
    analysisResult: String,
    onAnalyze: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector (Image or Video)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (mode == "image") KuzmixYellow.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onModeChange("image") }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Analyze Photo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (mode == "video") KuzmixYellow.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onModeChange("video") }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Analyze Video", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Media Selector
        Text("Select Media to Analyze", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { onSelectMediaClick() },
            contentAlignment = Alignment.Center
        ) {
            if (selectedMediaUri != null) {
                if (mode == "image") {
                    AsyncImage(
                        model = selectedMediaUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(KuzmixYellow.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(48.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Swap Media", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (mode == "image") Icons.Default.AddPhotoAlternate else Icons.Default.VideoCall,
                        contentDescription = null,
                        tint = KuzmixYellow,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (mode == "image") "Upload Custom Photo" else "Upload Custom Video",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Pick file from device storage", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                }
            }
        }

        // Quick Presets
        Text("Or Select High-Tech Presets", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetAssets.forEach { asset ->
                val isSelected = selectedMediaUri?.toString() == asset.url
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, if (isSelected) KuzmixYellow else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { onSelectPreset(asset) }
                ) {
                    if (asset.type == "image") {
                        AsyncImage(
                            model = asset.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        // Analysis Question / Prompt
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("analysis_prompt_input"),
            label = { Text("What details do you want to extract?", color = Color.White.copy(alpha = 0.6f)) },
            placeholder = { Text("e.g. Identify all visible elements and summarize key trends.", color = Color.White.copy(alpha = 0.3f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = KuzmixYellow,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedContainerColor = DarkCard.copy(alpha = 0.6f),
                unfocusedContainerColor = DarkCard.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp),
            maxLines = 3
        )

        // Analyze Button
        Button(
            onClick = onAnalyze,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("analyze_button"),
            colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow),
            shape = RoundedCornerShape(16.dp),
            enabled = selectedMediaUri != null && !isAnalyzing
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Analyzing spacetime fabric...", color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze with OpenRouter Vision AI", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Results Card
        if (analysisResult.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, KuzmixYellow.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extraction Report", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Text(
                        text = analysisResult,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// --- Inline Video Player ---
@Composable
fun KuzmixStudioVideoPlayer(
    videoUrl: String,
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
        modifier = modifier.fillMaxSize(),
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

        if (isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KuzmixOrange)
            }
        }
    }
}

// --- Helper Functions for REST API ---

suspend fun generateImageWithOpenRouter(
    context: Context,
    apiKey: String,
    prompt: String,
    aspectRatio: String,
    quality: String = "high"
): String? = withContext(Dispatchers.IO) {
    val effectiveKey = if (apiKey.isEmpty() || apiKey == "MY_OPENROUTER_API_KEY") {
        BuildConfig.OPENROUTER_API_KEY.takeIf { it.isNotEmpty() && it != "MY_OPENROUTER_API_KEY" }
    } else apiKey

    if (effectiveKey.isNullOrEmpty()) return@withContext null
    
    android.util.Log.d("KuzmixStudio", "Using API key: ${effectiveKey.substring(0, minOf(10, effectiveKey.length))}...")
try {
        val client = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val requestJson = JSONObject().apply {
            put("model", "openai/gpt-image-1")
            put("prompt", prompt)
            put("n", 1)
            put("aspect_ratio", aspectRatio)
            put("quality", quality)
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/images")
            .addHeader("Authorization", "Bearer $effectiveKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://kuzmixos.com")
            .addHeader("X-Title", "Kuzmix OS")
            .post(body)
            .build()

        val response = client.newCall(request).execute().also { ensureActive() }
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            android.util.Log.e("KuzmixStudio", "OpenRouter Image API error ${response.code}: $responseBody")
            return@withContext null
        }

        val jsonResponse = JSONObject(responseBody)
        val dataArray = jsonResponse.optJSONArray("data")
        if (dataArray != null && dataArray.length() > 0) {
            val imageData = dataArray.getJSONObject(0)
            val b64Json = imageData.optString("b64_json", "")
            if (b64Json.isNotEmpty()) {
                val bytes = android.util.Base64.decode(b64Json, android.util.Base64.DEFAULT)
                val file = java.io.File(context.cacheDir, "kuzmix_img_${System.currentTimeMillis()}.png")
                file.writeBytes(bytes)
                return@withContext android.net.Uri.fromFile(file).toString()
            }
        }
        null
    } catch (e: Exception) {
        android.util.Log.e("KuzmixStudio", "Image generation failed", e)
        null
    }
}

suspend fun generateVideoWithOpenRouter(
    context: Context,
    apiKey: String,
    prompt: String,
    aspectRatio: String,
    imageBase64: String?
): String? = withContext(Dispatchers.IO) {
    val effectiveKey = if (apiKey.isEmpty() || apiKey == "MY_OPENROUTER_API_KEY") {
        BuildConfig.OPENROUTER_API_KEY.takeIf { it.isNotEmpty() && it != "MY_OPENROUTER_API_KEY" }
    } else apiKey

    if (effectiveKey.isNullOrEmpty()) return@withContext null
    
    android.util.Log.d("KuzmixStudio", "Using API key for video: ${effectiveKey.substring(0, minOf(10, effectiveKey.length))}...")
try {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val requestJson = JSONObject().apply {
            put("model", "google/veo-3.1-fast")
            put("prompt", prompt)
            put("aspect_ratio", aspectRatio)
            put("duration", 8)
            put("resolution", "720p")
            put("generate_audio", true)
            if (imageBase64 != null) {
                put("frame_images", JSONArray().apply {
                    put(JSONObject().apply {
                        put("frame_type", "first_frame")
                        put("image_url", "data:image/jpeg;base64,$imageBase64")
                    })
                })
            }
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/videos")
            .addHeader("Authorization", "Bearer $effectiveKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://kuzmixos.com")
            .addHeader("X-Title", "Kuzmix OS")
            .post(body)
            .build()

        val response = client.newCall(request).execute().also { ensureActive() }
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            android.util.Log.e("KuzmixStudio", "OpenRouter Video API error ${response.code}: $responseBody")
            return@withContext null
        }

        val jsonResponse = JSONObject(responseBody)
        val jobId = jsonResponse.optString("id", "")
        val pollingUrl = jsonResponse.optString("polling_url", "")

        if (jobId.isEmpty() || pollingUrl.isEmpty()) {
            android.util.Log.e("KuzmixStudio", "No job ID or polling URL returned")
            return@withContext null
        }

        val fullPollingUrl = "https://openrouter.ai$pollingUrl"
        var pollCount = 0
        val maxPolls = 60

        while (pollCount < maxPolls) {
            ensureActive()
            delay(5000)
            pollCount++

            val pollRequest = Request.Builder()
                .url(fullPollingUrl)
                .addHeader("Authorization", "Bearer $effectiveKey")
                .addHeader("HTTP-Referer", "https://kuzmixos.com")
                .addHeader("X-Title", "Kuzmix OS")
                .get()
                .build()

            val pollResponse = client.newCall(pollRequest).execute()
            val pollResponseBody = pollResponse.body?.string() ?: ""

            if (!pollResponse.isSuccessful) {
                android.util.Log.e("KuzmixStudio", "Polling error ${pollResponse.code}: $pollResponseBody")
                continue
            }

            val pollJson = JSONObject(pollResponseBody)
            val status = pollJson.optString("status", "")

            when (status) {
                "completed" -> {
                    val unsignedUrls = pollJson.optJSONArray("unsigned_urls")
                    if (unsignedUrls != null && unsignedUrls.length() > 0) {
                        val videoUrl = unsignedUrls.getString(0)
                        return@withContext videoUrl
                    }
                    return@withContext null
                }
                "failed" -> {
                    val error = pollJson.optString("error", "Unknown error")
                    android.util.Log.e("KuzmixStudio", "Video generation failed: $error")
                    return@withContext null
                }
                "cancelled" -> {
                    android.util.Log.e("KuzmixStudio", "Video generation cancelled")
                    return@withContext null
                }
                "expired" -> {
                    android.util.Log.e("KuzmixStudio", "Video generation expired")
                    return@withContext null
                }
                else -> {
                    android.util.Log.d("KuzmixStudio", "Video generation status: $status (poll $pollCount/$maxPolls)")
                }
            }
        }

        android.util.Log.e("KuzmixStudio", "Video generation timed out after $maxPolls polls")
        null
    } catch (e: Exception) {
        android.util.Log.e("KuzmixStudio", "Video generation failed", e)
        null
    }
}

suspend fun analyzeMediaWithOpenRouter(
    context: Context,
    apiKey: String,
    prompt: String,
    mediaUri: Uri?,
    mediaBase64: String?,
    mode: String
): String? = withContext(Dispatchers.IO) {
    val keyToUse = if (apiKey.isNotEmpty() && apiKey != "MY_OPENROUTER_API_KEY") apiKey else BuildConfig.OPENROUTER_API_KEY
    if (keyToUse.isEmpty() || keyToUse == "MY_OPENROUTER_API_KEY") {
        return@withContext "API Key Required. Please set your OpenRouter key in the AI Studio Secrets panel."
    }
    try {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        // Use OpenRouter with OpenAI-compatible format
        val url = "https://openrouter.ai/api/v1/chat/completions"

        val mimeType = if (mode == "image") "image/jpeg" else "video/mp4"
        val resolvedBase64 = mediaBase64 ?: if (mediaUri != null) uriToBase64(context, mediaUri) else null

        if (resolvedBase64 == null) {
            return@withContext "Selected media payload could not be extracted."
        }

        val messagesArray = JSONArray()
        val messageObj = JSONObject().apply {
            put("role", "user")
            put("content", JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "text")
                    put("text", prompt)
                })
                put(JSONObject().apply {
                    put("type", "image_url")
                    put("image_url", JSONObject().apply {
                        put("url", "data:$mimeType;base64,$resolvedBase64")
                    })
                })
            })
        }
        messagesArray.put(messageObj)

        val requestJson = JSONObject().apply {
            put("model", "google/gemini-2.0-flash-001")
            put("messages", messagesArray)
            put("max_tokens", 1024)
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $keyToUse")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://kuzmixos.com")
            .addHeader("X-Title", "Kuzmix OS")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseString = response.body?.string() ?: ""

        if (response.isSuccessful) {
            val json = JSONObject(responseString)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                return@withContext message.getString("content")
            }
        }
        "Extraction Service Timeout. Response Code: ${response.code}\nRaw Payload: $responseString"
    } catch (e: Exception) {
        e.printStackTrace()
        "Connection failure during extraction: ${e.localizedMessage}"
    }
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveImageToGallery(context: Context, url: String, onStatus: (String) -> Unit) {
    val coroutineScope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main)
    coroutineScope.launch {
        onStatus("Downloading dimension... Please wait.")
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
                    withContext(Dispatchers.IO) {
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
                            resolver.openOutputStream(imageUri)?.use { out ->
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                contentValues.clear()
                                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                                resolver.update(imageUri, contentValues, null, null)
                            }
                            withContext(Dispatchers.Main) {
                                onStatus("Asset downloaded successfully to Gallery under Pictures/KuzmixForge!")
                            }
                        }
                    }
                } else {
                    onStatus("Invalid image format.")
                }
            } else {
                onStatus("Failed to obtain image payload.")
            }
        } catch (e: Exception) {
            onStatus("Save failed: ${e.message}")
        }
    }
}

fun setAsWallpaper(context: Context, url: String, onStatus: (String) -> Unit) {
    val coroutineScope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main)
    coroutineScope.launch {
        onStatus("Applying dynamic background...")
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
                    withContext(Dispatchers.IO) {
                        val wm = android.app.WallpaperManager.getInstance(context)
                        wm.setBitmap(bitmap)
                    }
                    onStatus("Workspace wallpaper updated with forged visualization!")
                } else {
                    onStatus("Unable to parse background payload.")
                }
            } else {
                onStatus("Failed to download wallpaper source.")
            }
        } catch (e: Exception) {
            onStatus("Wallpaper setup error: ${e.message}")
        }
    }
}

fun saveVideoToGallery(context: Context, url: String, onStatus: (String) -> Unit) {
    try {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        val fileName = "kuzmix_anim_${System.currentTimeMillis()}.mp4"
        val request = android.app.DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("Kuzmix Animator Video")
            setDescription("Forging celestial animation")
            setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_MOVIES, "KuzmixAnimator/$fileName")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        dm.enqueue(request)
        onStatus("Downloading dimension stream... check your notifications panel.")
    } catch (e: Exception) {
        onStatus("Download queue error: ${e.message}")
    }
}

fun setLiveWallpaper(context: Context, url: String, onStatus: (String) -> Unit) {
    val coroutineScope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main)
    coroutineScope.launch {
        onStatus("Downloading live wallpaper animation payload...")
        try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val req = Request.Builder().url(url).build()
                val resp = client.newCall(req).execute()
                val bytes = resp.body?.bytes()
                if (bytes != null && bytes.isNotEmpty()) {
                    val file = File(context.filesDir, "kuzmix_wallpaper.mp4")
                    file.writeBytes(bytes)
                    val sharedPref = context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("live_wallpaper_video_uri", Uri.fromFile(file).toString()).apply()

                    val intent = android.content.Intent(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(
                            android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            android.content.ComponentName(context, KuzmixVideoWallpaperService::class.java)
                        )
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    withContext(Dispatchers.Main) {
                        onStatus("Live Wallpaper configuration opened!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onStatus("Animation payload returned empty stream.")
                    }
                }
            }
        } catch (e: Exception) {
            onStatus("Spacetime live setup failed: ${e.message}")
        }
    }
}
