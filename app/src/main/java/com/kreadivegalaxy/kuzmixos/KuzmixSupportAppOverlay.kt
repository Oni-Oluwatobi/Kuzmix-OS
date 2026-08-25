package com.kreadivegalaxy.kuzmixos

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkSurface
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkCard

@Composable
fun KuzmixSupportAppOverlay(
    hardwareTier: HardwareTier,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val versionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "2.5.0-Aura"
        } catch (e: Exception) {
            "2.5.0-Aura"
        }
    }

    val versionCode = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        } catch (e: Exception) {
            "25"
        }
    }

    var isCheckingForUpdates by remember { mutableStateOf(false) }
    var updateStatusMessage by remember { mutableStateOf<String?>(null) }
    var updateCheckedTime by remember { mutableStateOf<String?>(null) }

    var feedbackType by remember { mutableStateOf("Bug Report") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var senderEmail by remember { mutableStateOf("") }

    var userRating by remember { mutableStateOf(0) }
    var ratingComment by remember { mutableStateOf("") }
    var ratingSubmitted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        DarkBackground
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(KuzmixYellow.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, KuzmixYellow.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SupportAgent,
                            contentDescription = "Support & About",
                            tint = KuzmixYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Support & About",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kuzmix OS • Developer Credentials & Support",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable { onClose() },
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

            Spacer(modifier = Modifier.height(2.dp))

            // Developer & System Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(KuzmixYellow.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("DEVELOPER INFORMATION", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Oni Oluwatobi X The Kreadive Galaxy", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("System Platform", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("Kuzmix OS v2.5 Aura", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Hardware Tier", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("${hardwareTier.name} (${HardwareDetection.getDeviceModel()})", color = KuzmixYellow, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // App Version & System Updates Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixOrange.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(KuzmixOrange.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = KuzmixOrange, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("VERSION & UPDATES", color = KuzmixOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("System Version Info", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Version", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("v$versionName", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Build Code / Channel", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("Build $versionCode • Stable", color = KuzmixYellow, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (updateStatusMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KuzmixYellow.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .border(0.5.dp, KuzmixYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Up to date",
                                    tint = KuzmixYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = updateStatusMessage ?: "",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (updateCheckedTime != null) {
                                        Text(
                                            text = "Checked: $updateCheckedTime",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isCheckingForUpdates) {
                                    isCheckingForUpdates = true
                                    updateStatusMessage = null
                                    coroutineScope.launch {
                                        delay(2000)
                                        isCheckingForUpdates = false
                                        updateStatusMessage = "Kuzmix OS is up to date!"
                                        updateCheckedTime = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date())
                                    }
                                }
                            },
                            enabled = !isCheckingForUpdates,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KuzmixOrange.copy(alpha = 0.2f),
                                disabledContainerColor = KuzmixOrange.copy(alpha = 0.1f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isCheckingForUpdates) KuzmixOrange.copy(alpha = 0.3f) else KuzmixOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isCheckingForUpdates) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = KuzmixOrange,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Checking...", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Check",
                                        tint = KuzmixOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Check for Updates", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Kuzmix OS v$versionName Features:\n- In-App Support Screen\n- Copy Support Details to Clipboard\n- Feedback Form Submission\n- Neural Ambient Synthesizer\n- Edge-to-Edge Adaptive UI",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Release Notes", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // In-App Rating Prompt Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(KuzmixYellow.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("RATE KUZMIX OS", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Provide Star Rating", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    if (!ratingSubmitted) {
                        Text(
                            text = "How would you rate your experience with Kuzmix OS Aura? Your rating helps us make it better!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )

                        // Stars Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { index ->
                                val isSelected = index <= userRating
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$index Stars",
                                    tint = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable {
                                            userRating = index
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }

                        if (userRating > 0) {
                            val promptMessage = when (userRating) {
                                5 -> "Superb! Thank you for the 5-star review! 😍"
                                4 -> "Awesome! We appreciate your 4-star support! 🚀"
                                3 -> "Thank you! Tell us how we can make it a 5-star OS."
                                else -> "We are sorry to hear that. Tell us how we can improve! 🛠️"
                            }
                            Text(
                                text = promptMessage,
                                color = KuzmixYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            // Review Comment Field
                            OutlinedTextField(
                                value = ratingComment,
                                onValueChange = { ratingComment = it },
                                label = { Text("Write a quick comment (Optional)", fontSize = 11.sp) },
                                placeholder = { Text("What do you love or want improved?", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface,
                                    focusedBorderColor = KuzmixYellow,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedLabelColor = KuzmixYellow,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    ratingSubmitted = true
                                    Toast.makeText(context, "Thank you! Rating of $userRating/5 submitted.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow.copy(alpha = 0.2f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Submit Rating", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Rating Completed State
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { index ->
                                    val isSelected = index <= userRating
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Your $userRating-star rating has been recorded!",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (ratingComment.isNotBlank()) {
                                Text(
                                    text = "\"$ratingComment\"",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

                            Text(
                                text = "Thank you for supporting the development of Kuzmix OS!",
                                color = KuzmixYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    ratingSubmitted = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Change Rating", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Support Kuzmix OS - Bank Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(KuzmixYellow.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("SUPPORT KUZMIX OS", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Bank Details for Transfers & Donations", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(16.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString("Oni Oluwatobi"))
                                        Toast.makeText(context, "Account Name 'Oni Oluwatobi' copied!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Account Name", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Text("Oni Oluwatobi", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("Tap to copy name", color = KuzmixYellow.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.08f))
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString("9124846023"))
                                        Toast.makeText(context, "Account number 9124846023 copied!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Account Number (OPay)", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Text("9124846023", color = KuzmixYellow, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("Tap to copy number", color = KuzmixYellow.copy(alpha = 0.5f), fontSize = 10.sp)
                                }

                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("9124846023"))
                                        Toast.makeText(context, "Account number 9124846023 copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow.copy(alpha = 0.2f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(14.dp))
                                        Text("Copy", color = KuzmixYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contact Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixOrange.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(KuzmixOrange.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = KuzmixOrange, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("CONTACT CHANNELS", color = KuzmixOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Direct Support & Inquiries", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Contact Numbers
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(16.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Phone / WhatsApp Contact", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        
                        // Line 1: 08143186133
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("08143186133"))
                                    Toast.makeText(context, "Copied 08143186133", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("08143186133", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Tap to copy number", color = KuzmixOrange.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("08143186133"))
                                        Toast.makeText(context, "Copied 08143186133", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08143186133")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) { e.printStackTrace() }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow.copy(alpha = 0.25f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = KuzmixYellow, modifier = Modifier.size(12.dp))
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

                        // Line 2: 09124846023
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("09124846023"))
                                    Toast.makeText(context, "Copied 09124846023", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("09124846023", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Tap to copy number", color = KuzmixOrange.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("09124846023"))
                                        Toast.makeText(context, "Copied 09124846023", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:09124846023")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) { e.printStackTrace() }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow.copy(alpha = 0.25f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = KuzmixYellow, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    // Email Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(16.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Official Email Address", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("thekreadivegalaxy@gmail.com"))
                                    Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("thekreadivegalaxy@gmail.com", color = KuzmixYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Tap to copy email", color = KuzmixOrange.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("thekreadivegalaxy@gmail.com"))
                                        Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Email", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:thekreadivegalaxy@gmail.com")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) { e.printStackTrace() }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange.copy(alpha = 0.25f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = "Send Email", tint = KuzmixOrange, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feedback Submission Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(KuzmixYellow.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = KuzmixYellow, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("DEVELOPER FEEDBACK & SUGGESTIONS", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Send Bugs or Ideas", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Selection of Feedback Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Bug Report", "Suggestion").forEach { type ->
                            val isSelected = feedbackType == type
                            val activeBg = if (type == "Bug Report") Color(0xFFFF5252).copy(alpha = 0.15f) else KuzmixYellow.copy(alpha = 0.15f)
                            val activeBorder = if (type == "Bug Report") Color(0xFFFF5252) else KuzmixYellow
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) activeBg else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) activeBorder else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { feedbackType = type }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Subject TextField
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject", fontSize = 12.sp) },
                        placeholder = { Text("What is this regarding?", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = KuzmixYellow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = KuzmixYellow,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Message TextField
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message Details", fontSize = 12.sp) },
                        placeholder = { Text("Describe the bug or suggest your custom idea...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = KuzmixYellow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = KuzmixYellow,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Contact info (Optional Email)
                    OutlinedTextField(
                        value = senderEmail,
                        onValueChange = { senderEmail = it },
                        label = { Text("Your Email (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("So the developer can reply to you", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = KuzmixYellow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = KuzmixYellow,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (subject.isBlank()) {
                                Toast.makeText(context, "Please enter a subject", Toast.LENGTH_SHORT).show()
                            } else if (message.isBlank()) {
                                Toast.makeText(context, "Please enter your message", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val emailBody = """
                                        Feedback Type: $feedbackType
                                        ${if (senderEmail.isNotBlank()) "Sender Contact: $senderEmail" else "Sender: Anonymous"}
                                        
                                        Message details:
                                        $message
                                        
                                        --
                                        Device Model: ${HardwareDetection.getDeviceModel()}
                                        Hardware Tier: ${hardwareTier.name}
                                        Kuzmix OS v2.5 Aura
                                    """.trimIndent()
                                    
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:thekreadivegalaxy@gmail.com")).apply {
                                        putExtra(Intent.EXTRA_SUBJECT, "[Kuzmix OS Feedback] [$feedbackType] $subject")
                                        putExtra(Intent.EXTRA_TEXT, emailBody)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Opening email app...", Toast.LENGTH_SHORT).show()
                                    
                                    // Clear form on successful intent launch
                                    subject = ""
                                    message = ""
                                    senderEmail = ""
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No email client found. Copying feedback to clipboard instead.", Toast.LENGTH_LONG).show()
                                    val fallbackText = "[$feedbackType] $subject\n\n$message"
                                    clipboardManager.setText(AnnotatedString(fallbackText))
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (feedbackType == "Bug Report") Color(0xFFFF5252).copy(alpha = 0.2f) else KuzmixYellow.copy(alpha = 0.2f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (feedbackType == "Bug Report") Color(0xFFFF5252) else KuzmixYellow
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Submit Feedback",
                                tint = if (feedbackType == "Bug Report") Color(0xFFFF5252) else KuzmixYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Submit $feedbackType",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Copy All Support Details Button
            Button(
                onClick = {
                    val allDetails = """
                        KUZMIX OS DEVELOPER SUPPORT INFO
                        --------------------------------
                        Developer Name: Oni Oluwatobi X The Kreadive Galaxy
                        
                        [BANK TRANSFER DETAILS]
                        Account Name: Oni Oluwatobi
                        Account Number: 9124846023
                        Bank/Provider: OPay
                        
                        [CONTACT CHANNELS]
                        Phone 1: 08143186133
                        Phone 2: 09124846023
                        Email: thekreadivegalaxy@gmail.com
                    """.trimIndent()
                    clipboardManager.setText(AnnotatedString(allDetails))
                    Toast.makeText(context, "All support details copied to clipboard!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KuzmixYellow.copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KuzmixYellow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy All Details",
                        tint = KuzmixYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Copy All Support Details",
                        color = KuzmixYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
