package com.kreadivegalaxy.kuzmixos

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun KuzmixGoldenEmblem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF59E0B),
                        Color(0xFFD97706),
                        Color(0xFFB45309)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0x60FFE082),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize(0.64f)) {
            val w = size.width
            val h = size.height

            // Main K body path
            val kPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.22f, h * 0.12f)
                lineTo(w * 0.22f, h * 0.88f)
                lineTo(w * 0.38f, h * 0.88f)
                lineTo(w * 0.38f, h * 0.54f)
                lineTo(w * 0.68f, h * 0.88f)
                lineTo(w * 0.88f, h * 0.88f)
                lineTo(w * 0.52f, h * 0.48f)
                lineTo(w * 0.85f, h * 0.12f)
                lineTo(w * 0.65f, h * 0.12f)
                lineTo(w * 0.40f, h * 0.40f)
                lineTo(w * 0.38f, h * 0.43f)
                lineTo(w * 0.38f, h * 0.12f)
                close()
            }
            drawPath(path = kPath, color = Color.White)

            // Dynamic swoosh accent stroke on left
            val swoosh = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.14f, h * 0.14f)
                cubicTo(w * 0.14f, h * 0.14f, w * 0.19f, h * 0.42f, w * 0.20f, h * 0.52f)
                cubicTo(w * 0.21f, h * 0.65f, w * 0.18f, h * 0.76f, w * 0.20f, h * 0.86f)
            }
            drawPath(
                path = swoosh,
                color = Color.White,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.8.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}

/**
 * Custom Input Text Field styled to match the user's exact dark-mode specification.
 */
@Composable
fun KuzmixAuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    val activeBorderColor = if (isFocused) Color(0xFFFFA000) else Color(0xFF232536)
    val iconTint = if (isFocused) Color(0xFFFFA000) else Color(0xFF6F7487)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF6F7487),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Passcode Visibility",
                        tint = if (passwordVisible) Color(0xFFFFA000) else Color(0xFF6F7487),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onNext = { onImeAction() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF181924),
            unfocusedContainerColor = Color(0xFF181924),
            focusedBorderColor = activeBorderColor,
            unfocusedBorderColor = activeBorderColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFFFFA000)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
    )
}

/**
 * Pixel-accurate implementation of the user's custom designed Sign In UI for Kuzmix OS.
 * Features:
 * - Circular Golden Emblem with Stylized White Kuzmix K
 * - Bold "KUZMIX OS" + Amber "Neural Gateway" branding
 * - Elevated Dark Glass Sign-In Card (#12131C)
 * - Custom Username/Email & Passcode Fields with trailing eye toggle
 * - Vivid Gradient "Enter OS ->" Action Button (#FF7A00 to #8000FF)
 * - "Guest" (Cyan Bolt) & "Biometric" (Amber Fingerprint) Actions
 * - "The Kreadive Galaxy • Oni Oluwatobi" Centered Footer
 */
@Composable
fun KuzmixLoginScreen(
    hardwareTier: HardwareTier,
    onLoginSuccess: () -> Unit,
    viewModel: KuzmixLoginViewModel = viewModel(
        factory = KuzmixLoginViewModel.Factory(LocalContext.current.applicationContext)
    )
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun handleCredentialsSubmit() {
        keyboardController?.hide()
        viewModel.signInWithCredentials(onLoginSuccess)
    }

    fun handleGuestSubmit() {
        keyboardController?.hide()
        viewModel.signInAsGuest(onLoginSuccess)
    }

    fun handleBiometricSubmit() {
        keyboardController?.hide()
        viewModel.signInWithBiometrics(onLoginSuccess)
    }

    val context = LocalContext.current
    var showDiagnosticsViewer by remember { mutableStateOf(false) }
    var hasCrashOccurred by remember {
        mutableStateOf(com.kreadivegalaxy.kuzmixos.diagnostics.KuzmixCrashLoggingService.hasUnhandledCrashOccurred(context))
    }

    androidx.activity.compose.BackHandler(enabled = true) {
        if (showDiagnosticsViewer) {
            showDiagnosticsViewer = false
        }
        // Consume back gesture so tapping back on lock/login screen does not close the launcher
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .drawBehind {
                // Subtle ambient dusk aura behind card
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8A2BE2).copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.75f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.45f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF9500).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.65f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.25f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 1. BRAND HEADER (Exact User Design)
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Golden Emblem with Stylized White Kuzmix K
                KuzmixGoldenEmblem(
                    modifier = Modifier.size(92.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Title
                Text(
                    text = "KUZMIX OS",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = "Neural Gateway",
                    color = Color(0xFFFFA000),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Crash Recovery Alert Banner (shown when unexpected exit occurred)
            AnimatedVisibility(visible = hasCrashOccurred) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { showDiagnosticsViewer = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1417)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF453A).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF453A),
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Recovered From Crash",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Diagnostic logs stored in Room DB. Tap to view.",
                                color = Color(0xFFE5E5E7),
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFFF453A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ==========================================
            // 2. SIGN IN CARD (Exact User Design)
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12131C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222434))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Inside Card
                    Text(
                        text = "Sign In",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Field 1: Username or Email
                    KuzmixAuthInputField(
                        value = uiState.username,
                        onValueChange = { viewModel.onUsernameChanged(it) },
                        placeholder = "Username or Email",
                        leadingIcon = Icons.Default.Person,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 2: Passcode
                    KuzmixAuthInputField(
                        value = uiState.passcode,
                        onValueChange = { viewModel.onPasscodeChanged(it) },
                        placeholder = "Passcode",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        onImeAction = { handleCredentialsSubmit() }
                    )

                    // Error Message Banner
                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.errorMessage?.let { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .background(Color(0xFF330D1B), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFFF4560), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF4560),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    color = Color(0xFFFF8093),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Button: "Enter OS ->" (Gradient Orange to Electric Purple)
                    Button(
                        onClick = { handleCredentialsSubmit() },
                        enabled = !uiState.isAuthenticating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF7A00),
                                        Color(0xFF8000FF)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (uiState.isAuthenticating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Authenticating...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            } else {
                                Text(
                                    text = "Enter OS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Action Row: "Guest" & "Biometric"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Guest Button
                        OutlinedButton(
                            onClick = { handleGuestSubmit() },
                            enabled = !uiState.isAuthenticating,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF282B3E)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF181924)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Guest",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Biometric Button
                        OutlinedButton(
                            onClick = { handleBiometricSubmit() },
                            enabled = !uiState.isAuthenticating,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF282B3E)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF181924)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Biometric",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // 3. MINIMAL FOOTER (Exact User Design)
            // ==========================================
            Text(
                text = "The Kreadive Galaxy • Oni Oluwatobi",
                color = Color(0xFF5A5E70),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Diagnostic Logs Access Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showDiagnosticsViewer = true }
                    .background(Color(0xFF1C1C1E).copy(alpha = 0.8f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "Diagnostic Logs",
                    tint = Color(0xFF0A84FF),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "System Diagnostic Logs",
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Fullscreen Diagnostic Logs Overlay
        AnimatedVisibility(
            visible = showDiagnosticsViewer,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            com.kreadivegalaxy.kuzmixos.diagnostics.DiagnosticLogsViewerScreen(
                onClose = {
                    showDiagnosticsViewer = false
                    hasCrashOccurred = com.kreadivegalaxy.kuzmixos.diagnostics.KuzmixCrashLoggingService.hasUnhandledCrashOccurred(context)
                }
            )
        }
    }
}
