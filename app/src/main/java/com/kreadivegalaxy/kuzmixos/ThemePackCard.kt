package com.kreadivegalaxy.kuzmixos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * 3x3 App Icon Grid Preview rendered inside a Theme Pack Card.
 * Displays 9 stylized squircle app icons reflecting the aesthetic colorway of the theme.
 */
@Composable
fun ThemedAppIconGridPreview(
    theme: ThemePackItem,
    modifier: Modifier = Modifier
) {
    // 9 standard system apps to display in 3x3 grid
    val miniAppIcons = listOf(
        Icons.Default.Phone to "Phone",
        Icons.Default.ChatBubble to "Messages",
        Icons.Default.CameraAlt to "Camera",
        Icons.Default.PhotoLibrary to "Photos",
        Icons.Default.MusicNote to "Music",
        Icons.Default.Language to "Safari/Web",
        Icons.Default.Settings to "Settings",
        Icons.Default.Email to "Mail",
        Icons.Default.Favorite to "Health"
    )

    // Palette derivation based on theme
    val palette = remember(theme) {
        if (theme.accentColors.isNotEmpty()) {
            theme.accentColors
        } else {
            when {
                theme.title.contains("Bunny", ignoreCase = true) || theme.title.contains("Coquette", ignoreCase = true) -> listOf(
                    Color(0xFFFFB6C1), Color(0xFFFFC0CB), Color(0xFFFFD1DC),
                    Color(0xFFFFE4E1), Color(0xFFF8BBD0), Color(0xFFF48FB1),
                    Color(0xFFCE93D8), Color(0xFFFCE4EC), Color(0xFFFFF0F5)
                )
                theme.title.contains("Demon", ignoreCase = true) || theme.title.contains("Slayer", ignoreCase = true) -> listOf(
                    Color(0xFFD32F2F), Color(0xFF1E1E1E), Color(0xFFE53935),
                    Color(0xFF263238), Color(0xFFB71C1C), Color(0xFF37474F),
                    Color(0xFFFF5252), Color(0xFF212121), Color(0xFFFF1744)
                )
                theme.title.contains("Coastal", ignoreCase = true) || theme.title.contains("Ocean", ignoreCase = true) -> listOf(
                    Color(0xFF00B0FF), Color(0xFF00E5FF), Color(0xFF40C4FF),
                    Color(0xFF80D8FF), Color(0xFF0091EA), Color(0xFF00B8D4),
                    Color(0xFF84FFFF), Color(0xFF0288D1), Color(0xFFB2EBF2)
                )
                theme.title.contains("Cat", ignoreCase = true) || theme.title.contains("Cute", ignoreCase = true) -> listOf(
                    Color(0xFFFFB74D), Color(0xFFFFCC80), Color(0xFFFFE0B2),
                    Color(0xFFFFA726), Color(0xFFFF8A65), Color(0xFFFFAB91),
                    Color(0xFFFF7043), Color(0xFFFFE57F), Color(0xFFFFD54F)
                )
                else -> listOf(
                    Color(0xFF5E92F3), Color(0xFF7E57C2), Color(0xFF26A69A),
                    Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFFFFA726),
                    Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFFF7043)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF161618)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0 until 3) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val iconItem = miniAppIcons[index]
                        val iconColor = palette.getOrElse(index) { Color(0xFF3A3A3C) }

                        // Squircle mini app icon
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            iconColor,
                                            iconColor.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(9.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconItem.first,
                                contentDescription = iconItem.second,
                                tint = if (iconColor == Color(0xFF1E1E1E) || iconColor == Color(0xFF212121)) Color.White else Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-end Theme Pack Card composable replicating the iOS customizer aesthetic.
 *
 * Requirements fulfilled:
 * - Dimensions: Width ~160.dp, Card 1:1 ratio with 18.dp rounded corners.
 * - Visual Presentation: 3x3 grid preview of themed apps.
 * - Dark charcoal surface (#1C1C1E) with subtle border (rgba(255, 255, 255, 0.08)).
 * - "NEW" Badge pinned to top-left inside padding (8.dp) with capsule shape and #00C853 background.
 * - Footer: Title (White, SemiBold, 14.sp) + Coin price row (gold coin icon + amount).
 * - Zero ads or ad delays.
 */
@Composable
fun ThemePackCard(
    theme: ThemePackItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 80.dp)
            ) { onClick() }
    ) {
        // Thumbnail Container Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1C1C1E))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            // Visual Preview: Async image if provided, otherwise the authentic 3x3 App Icon Grid
            if (theme.previewImageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(theme.previewImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = theme.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        ThemeCardPlaceholder()
                    },
                    error = {
                        ThemedAppIconGridPreview(theme = theme)
                    }
                )
            } else {
                ThemedAppIconGridPreview(theme = theme)
            }

            // "NEW" Badge (Top-left pinned inside 8.dp padding)
            if (theme.isNew) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF00C853))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NEW",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title (White, SemiBold, 14.sp, single line with ellipsis)
        Text(
            text = theme.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Coin Price Row (🪙 Amount)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Small Gold Coin Icon (Circular shape with gold gradient)
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFE082),
                                Color(0xFFFFD700),
                                Color(0xFFFFA000)
                            )
                        )
                    )
                    .border(0.5.dp, Color(0xFFFFB300), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Subtle coin inner stamp
                Text(
                    text = "C",
                    color = Color(0xFF7A4B00),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 8.sp
                )
            }

            // Amount Text
            Text(
                text = "${theme.coinCost}",
                color = Color(0xFFFFD54F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
