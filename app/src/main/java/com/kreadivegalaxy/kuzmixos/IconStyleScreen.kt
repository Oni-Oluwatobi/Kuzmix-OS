package com.kreadivegalaxy.kuzmixos

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun Drawable?.toPreviewBitmap(): ImageBitmap {
    if (this == null) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap()
    return try {
        if (this is BitmapDrawable && this.bitmap != null && !this.bitmap.isRecycled) {
            this.bitmap.asImageBitmap()
        } else {
            val w = intrinsicWidth.coerceIn(1, 256)
            val h = intrinsicHeight.coerceIn(1, 256)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bitmap.asImageBitmap()
        }
    } catch (e: Throwable) {
        Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconStyleScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedStyle by remember {
        mutableStateOf(IconStylePreferences.getSelectedStyle(context))
    }
    var previewIcons by remember { mutableStateOf<List<Pair<String, Drawable>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var appliedStyle by remember { mutableStateOf(selectedStyle) }

    LaunchedEffect(selectedStyle) {
        isLoading = true
        IconResolver.setCurrentStyle(selectedStyle)
        withContext(Dispatchers.IO) {
            val icons = IconResolver.getPreviewIcons(context)
            withContext(Dispatchers.Main) {
                previewIcons = icons
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KCTokens.DarkCanvas)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = KCTokens.DarkCanvas
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(KCTokens.ElevatedSurface)
                                .border(0.5.dp, KCTokens.GlassBorder, CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = KCTokens.TextPureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Icon Style",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = KCTokens.TextPureWhite,
                                letterSpacing = (-0.4).sp
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose your favourite icon style",
                    fontSize = 14.sp,
                    color = KCTokens.TextMutedGray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 20.dp)
                )

                IconStyle.entries.forEach { style ->
                    val isSelected = selectedStyle == style.id
                    val isApplied = appliedStyle == style.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable {
                                selectedStyle = style.id
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) KCTokens.ElevatedSurfaceSecondary else KCTokens.ElevatedSurface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.verticalGradient(
                                if (isSelected) listOf(KCTokens.ElectricBlue, KCTokens.ElectricBlue)
                                else listOf(KCTokens.GlassBorder, KCTokens.GlassBorder)
                            ),
                            width = if (isSelected) 1.5.dp else 0.5.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = style.displayName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = KCTokens.TextPureWhite
                                        )
                                        if (isApplied) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(KCTokens.ElectricBlue.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Active",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = KCTokens.ElectricBlue
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = style.description,
                                        fontSize = 13.sp,
                                        color = KCTokens.TextSoftGray,
                                        lineHeight = 18.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(KCTokens.ElectricBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            if (isSelected && style.id != IconStyleId.DEFAULT) {
                                Spacer(modifier = Modifier.height(14.dp))

                                if (isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(KCTokens.DarkCanvas),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = KCTokens.ElectricBlue,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                } else if (previewIcons.isNotEmpty()) {
                                    val previewList = previewIcons.take(12)
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(6),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(
                                                if (previewList.size <= 6) 80.dp
                                                else if (previewList.size <= 12) 148.dp
                                                else 216.dp
                                            ),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp),
                                        userScrollEnabled = false
                                    ) {
                                        items(previewList) { (label, drawable) ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Image(
                                                    bitmap = drawable.toPreviewBitmap(),
                                                    contentDescription = label,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(RoundedCornerShape(12.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 9.sp,
                                                    color = KCTokens.TextMutedGray,
                                                    maxLines = 1,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            IconStylePreferences.setSelectedStyle(context, selectedStyle)
                            IconResolver.setCurrentStyle(selectedStyle)
                            IconResolver.invalidateCache()
                            appliedStyle = selectedStyle
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedStyle != appliedStyle) KCTokens.ElectricBlue
                        else KCTokens.ElevatedSurfaceSecondary
                    )
                ) {
                    if (selectedStyle != appliedStyle) {
                        Text(
                            text = "Apply Style",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = KCTokens.ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Applied",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KCTokens.ElectricBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

enum class IconStyle(
    val id: IconStyleId,
    val displayName: String,
    val description: String
) {
    KUZMIX(
        id = IconStyleId.KUZMIX,
        displayName = "Kuzmix",
        description = "Our signature icon pack. Bold, clean and modern."
    ),
    DEFAULT(
        id = IconStyleId.DEFAULT,
        displayName = "Default",
        description = "Use the original application icons from your device."
    )
}
