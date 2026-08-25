package com.kreadivegalaxy.kuzmixos

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreadivegalaxy.kuzmixos.ui.theme.MyApplicationTheme
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground

open class KCCustomizationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
        val legacyPrefs = getSharedPreferences("kuzmix_prefs", Context.MODE_PRIVATE)
        val hardwareTier = try { HardwareDetection.detectTier(this) } catch (e: Throwable) { HardwareTier.NOVA }

        setContent {
            var selectedWallpaperKey by remember {
                mutableStateOf(
                    prefs.getString("default_wallpaper_key", prefs.getString("wallpaper_key", "sunset")) ?: "sunset"
                )
            }
            var wallpaperUri by remember {
                mutableStateOf(prefs.getString("wallpaper_uri", null))
            }
            var themeMode by remember {
                mutableStateOf(prefs.getString("theme_mode", "dark") ?: "dark")
            }
            var iconMaskKey by remember {
                mutableStateOf(prefs.getString("icon_mask_shape", legacyPrefs.getString("icon_mask_shape", "squircle")) ?: "squircle")
            }
            var iconScaleFactor by remember {
                mutableStateOf(prefs.getFloat("icon_scale_factor", legacyPrefs.getFloat("icon_scale_factor", 1.0f)))
            }

            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let { selectedUri ->
                    try {
                        contentResolver.takePersistableUriPermission(
                            selectedUri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            bitmap?.let {
                                val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                                wallpaperManager.setBitmap(it)
                                val uriStr = selectedUri.toString()
                                wallpaperUri = uriStr
                                prefs.edit()
                                    .putString("wallpaper_uri", uriStr)
                                    .apply()
                                legacyPrefs.edit()
                                    .putString("wallpaper_uri", uriStr)
                                    .apply()
                                Toast.makeText(applicationContext, "Device wallpaper updated!", Toast.LENGTH_SHORT).show()
                            } ?: run {
                                Toast.makeText(applicationContext, "Failed to decode image.", Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            Toast.makeText(applicationContext, "Failed to open image.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, "Error setting wallpaper: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val wallpapers = remember {
                        listOf(
                            WallpaperItem("sunset", "Sunset"),
                            WallpaperItem("burj", "Burj Al Arab"),
                            WallpaperItem("ocean", "Ocean"),
                            WallpaperItem("airplane", "Airplane")
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Blurred Background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        renderEffect = RenderEffectCompat.createBlur(60f)
                                    }
                                    clip = true
                                }
                        ) {
                            var imageLoadFailed by remember { mutableStateOf(false) }
                            if (wallpaperUri != null && !imageLoadFailed) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(wallpaperUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    onState = { state ->
                                        if (state is coil.compose.AsyncImagePainter.State.Error) {
                                            imageLoadFailed = true
                                        }
                                    }
                                )
                            }
                            if (wallpaperUri == null || imageLoadFailed) {
                                DefaultWallpaper(key = selectedWallpaperKey, modifier = Modifier.fillMaxSize())
                            }
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                        }

                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(24.dp))

                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { finish() },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Customization", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    Text("Personalize your Kuzmix OS", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Wallpaper Section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("WALLPAPER", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(wallpapers) { item ->
                                            val isSelected = (item.key == selectedWallpaperKey && wallpaperUri == null)
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(72.dp)
                                                        .height(115.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .border(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(14.dp)
                                                        )
                                                        .clickable {
                                                            selectedWallpaperKey = item.key
                                                            wallpaperUri = null
                                                            prefs.edit().putString("default_wallpaper_key", item.key).putString("wallpaper_key", item.key).remove("wallpaper_uri").apply()
                                                            legacyPrefs.edit().putString("wallpaper_key", item.key).remove("wallpaper_uri").apply()
                                                        }
                                                ) {
                                                    DefaultWallpaper(key = item.key, modifier = Modifier.matchParentSize())
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(item.label, color = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = KuzmixOrange),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Choose from Gallery", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Theme Mode Section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("THEME MODE", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        listOf(
                                            Triple("system", "System", Icons.Default.Settings),
                                            Triple("light", "Light", Icons.Default.LightMode),
                                            Triple("dark", "Dark", Icons.Default.DarkMode)
                                        ).forEach { (mode, label, icon) ->
                                            val isSelected = themeMode == mode
                                            Card(
                                                modifier = Modifier.weight(1f).clickable {
                                                    themeMode = mode
                                                    prefs.edit().putString("theme_mode", mode).apply()
                                                    legacyPrefs.edit().putString("theme_mode", mode).apply()
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) KuzmixOrange.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f)
                                                ),
                                                border = CardDefaults.outlinedCardBorder().takeIf { isSelected }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Icon(icon, contentDescription = label, tint = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(label, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Icon Mask Section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("ICON SHAPE", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(LauncherIconMask.entries) { mask ->
                                            val isSelected = iconMaskKey.equals(mask.key, ignoreCase = true)
                                            val previewShape = remember(mask.key) { getIconMaskShape(mask.key) }

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) KuzmixYellow.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                                    .border(1.dp, if (isSelected) KuzmixYellow else Color.Transparent, RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        iconMaskKey = mask.key
                                                        prefs.edit().putString("icon_mask_shape", mask.key).apply()
                                                        legacyPrefs.edit().putString("icon_mask_shape", mask.key).apply()
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(previewShape)
                                                        .background(Brush.linearGradient(listOf(KuzmixYellow, KuzmixOrange))),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(modifier = Modifier.size(14.dp).background(Color.White, androidx.compose.foundation.shape.CircleShape))
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(mask.label, color = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Icon Scale Section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("ICON SCALE", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(0.85f to "85%", 0.95f to "95%", 1.0f to "100%", 1.08f to "108%", 1.15f to "115%").forEach { (scaleVal, scaleLabel) ->
                                            val isSelected = kotlin.math.abs(iconScaleFactor - scaleVal) < 0.03f
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) KuzmixYellow.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                                    .border(1.dp, if (isSelected) KuzmixYellow else Color.Transparent, RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        iconScaleFactor = scaleVal
                                                        prefs.edit().putFloat("icon_scale_factor", scaleVal).apply()
                                                        legacyPrefs.edit().putFloat("icon_scale_factor", scaleVal).apply()
                                                    }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(scaleLabel, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Hardware Info
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("DEVICE INFO", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val deviceModel = remember { HardwareDetection.getDeviceModel() }
                                    val cores = remember { Runtime.getRuntime().availableProcessors() }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Model", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                            Text(deviceModel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Tier", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                            Text(hardwareTier.name, color = KuzmixYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("CPU Cores", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                            Text("$cores", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("AeroGlass", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                            Text("Active", color = KuzmixYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Close Button
                            Button(
                                onClick = { finish() },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Close", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

class CustomizationHubActivity : KCCustomizationActivity()
