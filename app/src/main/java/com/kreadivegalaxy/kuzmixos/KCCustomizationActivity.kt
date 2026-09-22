package com.kreadivegalaxy.kuzmixos

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreadivegalaxy.kuzmixos.ui.theme.MyApplicationTheme

/**
 * KC Customization: Redesigned Main Browsing Interface.
 *
 * Implements the clean, modern dark-mode aesthetic of high-end iOS wallpaper & theme stores.
 * Strictly 100% ad-free architecture: zero ad SDKs, zero banners, zero interstitial delay,
 * instant presentation with smooth skeleton shimmer placeholders.
 */
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

            var showDeviceStudioSheet by remember { mutableStateOf(false) }
            var showIconStyleScreen by remember { mutableStateOf(false) }

            // Device Local Gallery Picker
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
                    color = KCTokens.DarkCanvas
                ) {
                    if (showIconStyleScreen) {
                        IconStyleScreen(
                            onBack = { showIconStyleScreen = false }
                        )
                    } else {
                        // Main Browsing Screen (Featured / Top, Categories, Modular Carousels)
                        KCCustomizationBrowseScreen(
                            onSelectWallpaperKey = { key ->
                                selectedWallpaperKey = key
                                wallpaperUri = null
                                prefs.edit()
                                    .putString("default_wallpaper_key", key)
                                    .putString("wallpaper_key", key)
                                    .remove("wallpaper_uri")
                                    .apply()
                                legacyPrefs.edit()
                                    .putString("wallpaper_key", key)
                                    .remove("wallpaper_uri")
                                    .apply()
                            },
                            onOpenDeviceStudio = {
                                showDeviceStudioSheet = true
                            },
                            onClose = {
                                finish()
                            }
                        )

                        // Optional Device Studio Bottom Sheet (Aero-Glass Mode, Icon Mask, Scale, Gallery)
                        if (showDeviceStudioSheet) {
                            DeviceStudioBottomSheet(
                                themeMode = themeMode,
                                onThemeModeChange = { newMode ->
                                    themeMode = newMode
                                    prefs.edit().putString("theme_mode", newMode).apply()
                                    legacyPrefs.edit().putString("theme_mode", newMode).apply()
                                },
                                iconMaskKey = iconMaskKey,
                                onIconMaskChange = { newMask ->
                                    iconMaskKey = newMask
                                    prefs.edit().putString("icon_mask_shape", newMask).apply()
                                    legacyPrefs.edit().putString("icon_mask_shape", newMask).apply()
                                },
                                iconScaleFactor = iconScaleFactor,
                                onIconScaleChange = { newScale ->
                                    iconScaleFactor = newScale
                                    prefs.edit().putFloat("icon_scale_factor", newScale).apply()
                                    legacyPrefs.edit().putFloat("icon_scale_factor", newScale).apply()
                                },
                                onOpenGallery = {
                                    galleryLauncher.launch("image/*")
                                },
                                onOpenIconStyle = {
                                    showDeviceStudioSheet = false
                                    showIconStyleScreen = true
                                },
                                onDismiss = {
                                    showDeviceStudioSheet = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Secondary Device Studio Bottom Sheet for system adjustments (gallery, icon shape, theme mode).
 * Kept strictly secondary to preserve the pristine iOS store aesthetic of the main browsing feed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceStudioBottomSheet(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    iconMaskKey: String,
    onIconMaskChange: (String) -> Unit,
    iconScaleFactor: Float,
    onIconScaleChange: (Float) -> Unit,
    onOpenGallery: () -> Unit,
    onOpenIconStyle: () -> Unit = {},
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KCTokens.DarkCanvas,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Device Studio",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KCTokens.TextPureWhite
                    )
                    Text(
                        text = "Configure system icons, gallery, and Aero-Glass",
                        fontSize = 12.sp,
                        color = KCTokens.TextSoftGray
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(KCTokens.ElevatedSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KCTokens.TextPureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Custom Device Wallpaper Action
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = KCTokens.CardShape,
                colors = CardDefaults.cardColors(containerColor = KCTokens.ElevatedSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenGallery() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(KCTokens.ElectricBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = KCTokens.ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Device Photo Gallery",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KCTokens.TextPureWhite
                            )
                            Text(
                                text = "Pick high-res photo from your local storage",
                                fontSize = 11.sp,
                                color = KCTokens.TextMutedGray
                            )
                        }
                    }
                    Text(
                        text = "Choose",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KCTokens.ElectricBlue
                    )
                }
            }

            // Icon Style
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = KCTokens.CardShape,
                colors = CardDefaults.cardColors(containerColor = KCTokens.ElevatedSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenIconStyle() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(KCTokens.ElectricBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = KCTokens.ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Icon Style",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KCTokens.TextPureWhite
                            )
                            Text(
                                text = "Change your app icons",
                                fontSize = 11.sp,
                                color = KCTokens.TextMutedGray
                            )
                        }
                    }
                    Text(
                        text = "Change",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KCTokens.ElectricBlue
                    )
                }
            }

            // Theme Mode
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "AERO-GLASS THEME MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KCTokens.TextMutedGray,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("system", "System", Icons.Default.Settings),
                        Triple("light", "Light", Icons.Default.LightMode),
                        Triple("dark", "Dark", Icons.Default.DarkMode)
                    ).forEach { (mode, label, icon) ->
                        val isSelected = themeMode == mode
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onThemeModeChange(mode) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) KCTokens.ElevatedSurfaceSecondary else KCTokens.ElevatedSurface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.verticalGradient(
                                    if (isSelected) listOf(KCTokens.ElectricBlue, KCTokens.ElectricBlue)
                                    else listOf(KCTokens.GlassBorder, KCTokens.GlassBorder)
                                ),
                                width = if (isSelected) 1.2.dp else 0.5.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) KCTokens.ElectricBlue else KCTokens.TextMutedGray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) KCTokens.TextPureWhite else KCTokens.TextSoftGray
                                )
                            }
                        }
                    }
                }
            }

            // Icon Shape
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NEO ICON SHAPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KCTokens.TextMutedGray,
                    letterSpacing = 1.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(LauncherIconMask.entries) { mask ->
                        val isSelected = iconMaskKey.equals(mask.key, ignoreCase = true)
                        val previewShape = remember(mask.key) { getIconMaskShape(mask.key) }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) KCTokens.ElevatedSurfaceSecondary else KCTokens.ElevatedSurface)
                                .border(
                                    width = if (isSelected) 1.2.dp else 0.5.dp,
                                    color = if (isSelected) KCTokens.ElectricBlue else KCTokens.GlassBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onIconMaskChange(mask.key) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(previewShape)
                                    .background(
                                        Brush.linearGradient(listOf(KCTokens.ElectricBlue, Color(0xFF64D2FF)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = mask.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) KCTokens.TextPureWhite else KCTokens.TextSoftGray
                            )
                        }
                    }
                }
            }

            // Icon Scale
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DYNAMIC ICON SCALE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KCTokens.TextMutedGray,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.85f to "85%", 0.95f to "95%", 1.0f to "100%", 1.08f to "108%", 1.15f to "115%").forEach { (scaleVal, scaleLabel) ->
                        val isSelected = kotlin.math.abs(iconScaleFactor - scaleVal) < 0.03f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) KCTokens.ElevatedSurfaceSecondary else KCTokens.ElevatedSurface)
                                .border(
                                    width = if (isSelected) 1.2.dp else 0.5.dp,
                                    color = if (isSelected) KCTokens.ElectricBlue else KCTokens.GlassBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onIconScaleChange(scaleVal) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = scaleLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) KCTokens.ElectricBlue else KCTokens.TextSoftGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

class CustomizationHubActivity : KCCustomizationActivity()
