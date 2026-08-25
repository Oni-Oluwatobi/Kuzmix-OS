package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kreadivegalaxy.kuzmixos.ui.theme.ElectricPurple
import com.kreadivegalaxy.kuzmixos.ui.theme.MyApplicationTheme
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkSurface
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkCard

class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun onStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class KuzmixAuraOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private val handler = Handler(Looper.getMainLooper())

    var isAuraActive by mutableStateOf(false)
    var isProcessing by mutableStateOf(false)
    var isListening by mutableStateOf(false)
    var isLowkeyMode by mutableStateOf(false)

    var showAssistantPanel by mutableStateOf(false)
    var showForgeCard by mutableStateOf(false)
    var assistantInputText by mutableStateOf("")
    var assistantResponseText by mutableStateOf("")

    var forgePrompt by mutableStateOf("")
    var forgeImageUrl by mutableStateOf<String?>(null)
    var forgeVideoUrl by mutableStateOf<String?>(null)
    var isForgeLoading by mutableStateOf(false)

    val overlayMessages = mutableStateListOf<SingularityChatMessage>()
    var onUserSendInput: ((String) -> Unit)? = null
    var onMicRequested: (() -> Unit)? = null
    var onStopRequested: (() -> Unit)? = null

    val hardwareTier = try {
        HardwareDetection.detectTier(context)
    } catch (unused: Throwable) {
        HardwareTier.NOVA
    }

    fun showOverlay() {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(context)) return

        val owner = OverlayLifecycleOwner()
        owner.onCreate()
        owner.onStart()
        owner.onResume()
        lifecycleOwner = owner

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)

            setContent {
                MyApplicationTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Perimeter Edge Glow (Aura)
                        PerimeterAuraGlow(
                            isActive = isAuraActive,
                            isProcessingOrListening = isProcessing || isListening
                        )

                        // 2. Standard Mode Floating Web3 Assistant Panel Overlay
                        if (!isLowkeyMode && showAssistantPanel) {
                        Web3AssistantOverlayPanel(
                            hardwareTier = hardwareTier,
                            isProcessing = isProcessing,
                            isListening = isListening,
                            responseText = assistantResponseText,
                            inputValue = assistantInputText,
                            onInputValueChange = {
                                assistantInputText = it
                                setFocusable(it.isNotEmpty())
                            },
                            messages = overlayMessages,
                            onMicClick = { onMicRequested?.invoke() },
                            onStopClick = { onStopRequested?.invoke() },
                            onSendInput = { input ->
                                if (input.isNotBlank()) {
                                    overlayMessages.add(SingularityChatMessage(text = input, isUser = true))
                                    onUserSendInput?.invoke(input)
                                    assistantInputText = ""
                                    setFocusable(false)
                                }
                            },
                            onClose = {
                                showAssistantPanel = false
                                deactivateAura()
                                setFocusable(false)
                            }
                        )
                        }

                        // 3. Galaxy Forge Card Overlay (Standard Mode visual generation)
                        if (!isLowkeyMode && showForgeCard) {
                            AnimatedVisibility(
                                visible = showForgeCard,
                                enter = slideEnter,
                                exit = slideExit,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .background(DarkBackground, RoundedCornerShape(28.dp))
                                        .border(1.dp, KuzmixOrange.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkBackground)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.ic_kuzmix_logo),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "GALAXY FORGE",
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.5.sp
                                                )
                                            }

                                            IconButton(
                                                onClick = { hideForge() },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "\"$forgePrompt\"",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (isForgeLoading) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(Color.Black.copy(alpha = 0.3f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    CircularProgressIndicator(
                                                        color = ElectricPurple,
                                                        strokeWidth = 3.dp,
                                                        modifier = Modifier.size(40.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Text(
                                                        text = "Synthesizing neural imagery...",
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        } else if (forgeImageUrl != null) {
                                            val context = LocalContext.current
                                            SubcomposeAsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(forgeImageUrl)
                                                    .memoryCacheKey(forgeImageUrl)
                                                    .diskCacheKey(forgeImageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = forgePrompt,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(260.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                                                loading = {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(
                                                            color = ElectricPurple,
                                                            strokeWidth = 3.dp,
                                                            modifier = Modifier.size(40.dp)
                                                        )
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

        try {
            windowManager.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setFocusable(focusable: Boolean) {
        overlayView?.let { view ->
            try {
                val params = view.layoutParams as? WindowManager.LayoutParams ?: return
                if (focusable) {
                    params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                } else {
                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                }
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun activateAuraLowkey() {
        showOverlay()
        isLowkeyMode = true
        showAssistantPanel = false
        showForgeCard = false
        isAuraActive = true
    }

    fun activateAuraStandard() {
        showOverlay()
        isLowkeyMode = false
        showAssistantPanel = true
        isAuraActive = true
    }

    fun activateAura(autoDismissMs: Long = 4000) {
        showOverlay()
        isAuraActive = true
        if (autoDismissMs > 0) {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (!showForgeCard && !showAssistantPanel && !isProcessing) {
                    isAuraActive = false
                }
            }, autoDismissMs)
        }
    }

    fun deactivateAura() {
        isAuraActive = false
        isProcessing = false
        isListening = false
        showAssistantPanel = false
        isLowkeyMode = false
        setFocusable(false)
    }

    fun addAiResponse(responseText: String) {
        assistantResponseText = responseText
        if (responseText.isNotBlank() && responseText != "Listening...") {
            val lastMsg = overlayMessages.lastOrNull()
            if (lastMsg != null && !lastMsg.isUser) {
                val index = overlayMessages.lastIndex
                overlayMessages[index] = lastMsg.copy(text = responseText)
            } else {
                overlayMessages.add(SingularityChatMessage(text = responseText, isUser = false))
            }
        }
    }

    fun showForge(prompt: String, imageUrl: String?) {
        showOverlay()
        isLowkeyMode = false
        activateAura(0)
        forgePrompt = prompt
        showForgeCard = true
        if (imageUrl != null) {
            forgeImageUrl = imageUrl
            isForgeLoading = false
        } else {
            isForgeLoading = true
            forgeImageUrl = null
        }
    }

    fun showVideoForge(prompt: String, videoUrl: String) {
        showOverlay()
        isLowkeyMode = false
        activateAura(0)
        forgePrompt = prompt
        showForgeCard = true
        forgeVideoUrl = videoUrl
        forgeImageUrl = null
        isForgeLoading = true
    }

    fun updateForgeImage(imageUrl: String) {
        forgeImageUrl = imageUrl
        isForgeLoading = false
    }

    fun hideForge() {
        showForgeCard = false
        isForgeLoading = false
        forgeImageUrl = null
        forgeVideoUrl = null
        handler.postDelayed({
            if (!showAssistantPanel) {
                isAuraActive = false
            }
        }, 1200)
    }

    fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }
}

// 1.5mm Kuzmix Aura Perimeter Edge Glow Composable - static, no infinite animation
@Composable
fun PerimeterAuraGlow(
    isActive: Boolean,
    isProcessingOrListening: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(if (isProcessingOrListening) 1.0f else 0.7f)
    )
}

// Floating Web3 Assistant Panel Overlay Composable
@Composable
fun Web3AssistantOverlayPanel(
    hardwareTier: HardwareTier = HardwareTier.NOVA,
    isProcessing: Boolean,
    isListening: Boolean,
    responseText: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    messages: List<SingularityChatMessage>,
    onMicClick: () -> Unit,
    onStopClick: (() -> Unit)? = null,
    onSendInput: (String) -> Unit,
    onClose: () -> Unit
) {
    val slideEnter: AnimationSpec<IntOffset> = if (hardwareTier == HardwareTier.TITAN) slideInVertically { it } else { EnterTransition.None }
    val slideExit: AnimationSpec<IntOffset> = if (hardwareTier == HardwareTier.TITAN) slideOutVertically { it } else { ExitTransition.None }
    var showToolsMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .padding(12.dp)
                .navigationBarsPadding()
                .imePadding()
                .background(DarkBackground, RoundedCornerShape(28.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                .clickable { /* prevent propagation */ }
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(KuzmixOrange, KuzmixYellow)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_kuzmix_logo),
                            contentDescription = "AI",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            "KUZMIX OS AI",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (isProcessing || isListening) KuzmixYellow else KuzmixYellow, CircleShape)
                            )
                            Text(
                                if (isProcessing) "TALKING / PROCESSING..."
                                else if (isListening) "LISTENING..."
                                else "ACTIVE SENTINEL",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onStopClick?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("STOP", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Action Suggestion Chips
            val suggestionChips = listOf(
                "⚙️ Voice Settings",
                "🎙️ Calibrate Voice",
                "🛑 Stop",
                "💬 Tell me more",
                "📝 Summarize",
                "💡 Explain simply",
                "🎨 Create Image",
                "🔄 Clear Chat"
            )
            var showCalibrationModal by remember { mutableStateOf(false) }
            var showVoiceSettingsModal by remember { mutableStateOf(false) }

            if (showVoiceSettingsModal) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showVoiceSettingsModal = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .fillMaxHeight(0.9f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground)
                    ) {
                        KuzmixWakeWordSettingsScreen(
                            onClose = { showVoiceSettingsModal = false }
                        )
                    }
                }
            }

            if (showCalibrationModal) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showCalibrationModal = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    KuzmixVoiceCalibrationWizard(
                        context = LocalContext.current,
                        onComplete = { showCalibrationModal = false },
                        onDismiss = { showCalibrationModal = false }
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestionChips) { chip ->
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable {
                                when (chip) {
                                    "⚙️ Voice Settings" -> showVoiceSettingsModal = true
                                    "🎙️ Calibrate Voice" -> showCalibrationModal = true
                                    "🛑 Stop" -> onStopClick?.invoke()
                                    "🔄 Clear Chat" -> onSendInput("clear chat")
                                    "💬 Tell me more" -> onSendInput("Tell me more about that")
                                    "📝 Summarize" -> onSendInput("Summarize that concisely")
                                    "💡 Explain simply" -> onSendInput("Explain that in simple terms")
                                    "🎨 Create Image" -> onSendInput("Create an image of futuristic AI city")
                                    else -> onSendInput(chip)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(chip, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Chat Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && responseText.isEmpty() && !isProcessing) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_kuzmix_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Kuzmix OS Global Assistant",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Speak or type system commands, controls, or questions.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            if (msg.isUser) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    listOf(DarkSurface.copy(alpha = 0.85f), DarkSurface.copy(alpha = 0.92f))
                                                ),
                                                shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                                            )
                                            .border(0.5.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(msg.text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(0.88f)
                                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                                            .border(
                                                1.dp,
                                                Brush.horizontalGradient(listOf(KuzmixOrange, KuzmixOrange)),
                                                RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_kuzmix_logo),
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text("Kuzmix AI", color = KuzmixOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(msg.text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The Glowing Input Bar (Image 2 Style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xD90E1017), // Midnight Charcoal 85% opacity
                        shape = RoundedCornerShape(22.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(KuzmixOrange, Color(0xFFFF5722))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
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
                    placeholder = { Text("Ask KC OS...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                .clickable { showToolsMenu = !showToolsMenu },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        Row(
                            modifier = Modifier
                                .height(30.dp)
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(15.dp))
                                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(15.dp))
                                .clickable { showToolsMenu = !showToolsMenu }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(12.dp))
                            Text("Tools", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onMicClick,
                            modifier = Modifier
                                .size(34.dp)
                                .background(if (isListening) KuzmixOrange.copy(alpha = 0.3f) else Color.Transparent, CircleShape)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    Brush.linearGradient(listOf(KuzmixOrange, KuzmixOrange)),
                                    CircleShape
                                )
                                .clickable { onSendInput(inputValue) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

