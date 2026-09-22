package com.kreadivegalaxy.kuzmixos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkCard

enum class QrAppTab {
    SCANNER,
    GENERATOR,
    HISTORY
}

data class QrScanRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val type: String,
    val timestamp: String
)

@Composable
fun QrScannerAppOverlay(
    hardwareTier: HardwareTier,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(QrAppTab.SCANNER) }
    var flashEnabled by remember { mutableStateOf(false) }
    var isScanningActive by remember { mutableStateOf(true) }
    var scannedResult by remember { mutableStateOf<QrScanRecord?>(null) }

    val prefs = remember { context.getSharedPreferences("KuzmixQrHistory", Context.MODE_PRIVATE) }
    var historyList by remember {
        mutableStateOf<List<QrScanRecord>>(loadQrHistory(prefs))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(KuzmixYellow.copy(alpha = 0.2f))
                            .border(1.dp, KuzmixYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "QR Scanner App",
                            tint = KuzmixYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "KUZMIX QR VISION",
                            color = KuzmixYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Developer: Oni Oluwatobi X The Kreadive Galaxy",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close Scanner",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Support line info strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(0.5.dp, KuzmixYellow.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Support Line: thekreadivegalaxy@gmail.com | +234 814 318 6133",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    QrAppTab.SCANNER to "Scan QR",
                    QrAppTab.GENERATOR to "Create QR",
                    QrAppTab.HISTORY to "History (${historyList.size})"
                ).forEach { (tab, label) ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) KuzmixYellow.copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .border(
                                0.5.dp,
                                if (isSelected) KuzmixYellow else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { activeTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (activeTab) {
                QrAppTab.SCANNER -> {
                    ScannerView(
                        context = context,
                        flashEnabled = flashEnabled,
                        onToggleFlash = { flashEnabled = !flashEnabled },
                        isScanning = isScanningActive,
                        scannedResult = scannedResult,
                        onScanTarget = { content, type ->
                            val record = QrScanRecord(
                                content = content,
                                type = type,
                                timestamp = java.text.SimpleDateFormat("HH:mm, MMM d", java.util.Locale.getDefault()).format(java.util.Date())
                            )
                            scannedResult = record
                            historyList = saveQrRecord(prefs, record, historyList)
                        },
                        onClearResult = { scannedResult = null }
                    )
                }
                QrAppTab.GENERATOR -> {
                    QrGeneratorView(
                        context = context,
                        onGenerated = { record ->
                            historyList = saveQrRecord(prefs, record, historyList)
                        }
                    )
                }
                QrAppTab.HISTORY -> {
                    QrHistoryView(
                        context = context,
                        historyList = historyList,
                        onClearHistory = {
                            prefs.edit().remove("qr_records_json").apply()
                            historyList = emptyList()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerView(
    context: Context,
    flashEnabled: Boolean,
    onToggleFlash: () -> Unit,
    isScanning: Boolean,
    scannedResult: QrScanRecord?,
    onScanTarget: (String, String) -> Unit,
    onClearResult: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LaserTransition")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserLine"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Viewfinder Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (flashEnabled) DarkCard else DarkBackground)
                .border(1.dp, KuzmixYellow.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            // HUD Scanner reticle
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val boxSize = w.coerceAtMost(h) * 0.65f
                val left = (w - boxSize) / 2f
                val top = (h - boxSize) / 2f
                val right = left + boxSize
                val bottom = top + boxSize

                // Corner target lines
                val cornerLen = 24.dp.toPx()
                val strokeW = 4.dp.toPx()
                val cyan = KuzmixYellow

                // Top Left
                drawLine(cyan, Offset(left, top), Offset(left + cornerLen, top), strokeWidth = strokeW)
                drawLine(cyan, Offset(left, top), Offset(left, top + cornerLen), strokeWidth = strokeW)

                // Top Right
                drawLine(cyan, Offset(right, top), Offset(right - cornerLen, top), strokeWidth = strokeW)
                drawLine(cyan, Offset(right, top), Offset(right, top + cornerLen), strokeWidth = strokeW)

                // Bottom Left
                drawLine(cyan, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeWidth = strokeW)
                drawLine(cyan, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeWidth = strokeW)

                // Bottom Right
                drawLine(cyan, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeWidth = strokeW)
                drawLine(cyan, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeWidth = strokeW)

                // Laser scan line
                val lineY = top + (boxSize * laserY)
                drawLine(
                    color = Color(0xFFFF0055),
                    start = Offset(left + 10f, lineY),
                    end = Offset(right - 10f, lineY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Controls overlay inside HUD
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                IconButton(
                    onClick = onToggleFlash,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashEnabled) KuzmixYellow else Color.White
                    )
                }
            }

            Text(
                text = "Align QR code or Barcode in frame",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Sample Target Selectors (for testing / instant demo)
        Text(
            text = "TAP TEST TARGET TO SIMULATE SCAN:",
            color = KuzmixYellow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sampleTargets = listOf(
                Triple("https://kuzmixos.com", "URL", "Kuzmix Web"),
                Triple("WIFI:S:Kuzmix_5G;P:CyberKey2026;;", "WIFI", "Wi-Fi"),
                Triple("MECARD:N:Oni Oluwatobi;TEL:+2348143186133;EMAIL:thekreadivegalaxy@gmail.com;;", "CONTACT", "vCard"),
                Triple("079357318921", "BARCODE", "Barcode")
            )

            sampleTargets.forEach { (content, type, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(0.5.dp, KuzmixYellow.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onScanTarget(content, type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scanned Result Card
        scannedResult?.let { record ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkBackground)
                    .border(1.dp, KuzmixYellow, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (record.type) {
                                    "WIFI" -> Icons.Default.Wifi
                                    "URL" -> Icons.Default.OpenInBrowser
                                    else -> Icons.Default.QrCode
                                },
                                contentDescription = "Type",
                                tint = KuzmixYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DECODED: ${record.type}",
                                color = KuzmixYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = onClearResult, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = record.content,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (record.content.startsWith("http://") || record.content.startsWith("https://")) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(KuzmixYellow)
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(record.content))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Open Link", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", record.content))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Copy", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, record.content)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Share", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QrGeneratorView(
    context: Context,
    onGenerated: (QrScanRecord) -> Unit
) {
    var textInput by remember { mutableStateOf("https://kreadivegalaxy.com") }
    var selectedCategory by remember { mutableStateOf("URL") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("URL", "TEXT", "WIFI", "CONTACT").forEach { cat ->
                val selected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) KuzmixYellow.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                        .border(0.5.dp, if (selected) KuzmixYellow else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { selectedCategory = cat }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        color = if (selected) KuzmixYellow else Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            BasicTextField(
                value = textInput,
                onValueChange = { textInput = it },
                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                cursorBrush = SolidColor(KuzmixYellow)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vector QR Canvas preview
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val grid = 12
                val cellSize = size.width / grid
                val hash = abs(textInput.hashCode())

                for (row in 0 until grid) {
                    for (col in 0 until grid) {
                        val isCorner = (row < 3 && col < 3) || (row < 3 && col >= grid - 3) || (row >= grid - 3 && col < 3)
                        val bit = ((hash xor (row * 23 + col * 13)) and 1) == 1
                        if (isCorner || bit) {
                            drawRoundRect(
                                color = DarkBackground,
                                topLeft = Offset(col * cellSize, row * cellSize),
                                size = Size(cellSize * 0.88f, cellSize * 0.88f),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(12.dp))
                .background(KuzmixYellow)
                .clickable {
                    if (textInput.isNotBlank()) {
                        val record = QrScanRecord(
                            content = textInput,
                            type = selectedCategory,
                            timestamp = java.text.SimpleDateFormat("HH:mm, MMM d", java.util.Locale.getDefault()).format(java.util.Date())
                        )
                        onGenerated(record)
                        Toast.makeText(context, "QR Saved to History!", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Save & Share QR",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QrHistoryView(
    context: Context,
    historyList: List<QrScanRecord>,
    onClearHistory: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCAN & GENERATED LOGS",
                color = KuzmixYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (historyList.isNotEmpty()) {
                Text(
                    text = "Clear All",
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onClearHistory() }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No QR history recorded yet",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList) { record ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = record.type,
                                        color = KuzmixYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = record.timestamp,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = record.content,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", record.content))
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun loadQrHistory(prefs: android.content.SharedPreferences): List<QrScanRecord> {
    val jsonStr = prefs.getString("qr_records_json", null) ?: return defaultSampleRecords()
    return try {
        val array = org.json.JSONArray(jsonStr)
        val list = mutableListOf<QrScanRecord>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                QrScanRecord(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    content = obj.optString("content", ""),
                    type = obj.optString("type", "TEXT"),
                    timestamp = obj.optString("timestamp", "")
                )
            )
        }
        if (list.isEmpty()) defaultSampleRecords() else list
    } catch (e: Exception) {
        defaultSampleRecords()
    }
}

private fun saveQrRecord(
    prefs: android.content.SharedPreferences,
    record: QrScanRecord,
    currentList: List<QrScanRecord>
): List<QrScanRecord> {
    val updated = listOf(record) + currentList.filterNot { it.content == record.content }
    try {
        val array = org.json.JSONArray()
        updated.forEach { item ->
            val obj = org.json.JSONObject().apply {
                put("id", item.id)
                put("content", item.content)
                put("type", item.type)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("qr_records_json", array.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return updated
}

private fun defaultSampleRecords(): List<QrScanRecord> {
    return listOf(
        QrScanRecord(
            content = "https://kuzmixos.com",
            type = "URL",
            timestamp = "12:00, Today"
        ),
        QrScanRecord(
            content = "Developer: Oni Oluwatobi X The Kreadive Galaxy",
            type = "CONTACT",
            timestamp = "Yesterday"
        )
    )
}
