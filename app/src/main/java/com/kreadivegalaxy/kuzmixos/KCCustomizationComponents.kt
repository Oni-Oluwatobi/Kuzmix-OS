package com.kreadivegalaxy.kuzmixos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Clean, modular components for KC Customization.
 * Implements strict zero-ad architecture with high-end iOS dark aesthetic.
 */

// =========================================================================
// A. Top Navigation Bar (Primary View Switcher)
// =========================================================================
@Composable
fun TopViewSwitcher(
    selectedTab: String, // "featured" or "top"
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCloseClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = KCTokens.DarkCanvas
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Segmented Text Tabs ("Featured", "Themes & Packs", & "Top")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Tab: Featured
                val isFeatured = selectedTab == "featured"
                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected("featured") },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Featured",
                        fontSize = 19.sp,
                        fontWeight = if (isFeatured) FontWeight.Bold else FontWeight.Medium,
                        color = if (isFeatured) KCTokens.TextPureWhite else KCTokens.TextMutedGray,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(2.5.dp)
                            .background(
                                color = if (isFeatured) KCTokens.ElectricBlue else Color.Transparent,
                                shape = KCTokens.PillShape
                            )
                    )
                }

                // Tab: Themes & Icon Packs
                val isThemes = selectedTab == "themes"
                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected("themes") },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Icon Packs",
                        fontSize = 19.sp,
                        fontWeight = if (isThemes) FontWeight.Bold else FontWeight.Medium,
                        color = if (isThemes) KCTokens.TextPureWhite else KCTokens.TextMutedGray,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(2.5.dp)
                            .background(
                                color = if (isThemes) KCTokens.ElectricBlue else Color.Transparent,
                                shape = KCTokens.PillShape
                            )
                    )
                }

                // Tab: Top
                val isTop = selectedTab == "top"
                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected("top") },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Top",
                        fontSize = 19.sp,
                        fontWeight = if (isTop) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTop) KCTokens.TextPureWhite else KCTokens.TextMutedGray,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.5.dp)
                            .background(
                                color = if (isTop) KCTokens.ElectricBlue else Color.Transparent,
                                shape = KCTokens.PillShape
                            )
                    )
                }
            }

            // Close / Action icon if provided
            if (onCloseClick != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(KCTokens.ElevatedSurface)
                        .border(0.5.dp, KCTokens.GlassBorder, CircleShape)
                        .clickable { onCloseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KCTokens.TextPureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// B. Category Filter Carousel (Horizontal Scroll)
// =========================================================================
@Composable
fun CategoryPillBar(
    categories: List<Category>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
    ) {
        items(categories, key = { it.id }) { cat ->
            val isSelected = cat.id == selectedCategoryId
            val bgCol by animateColorAsState(
                targetValue = if (isSelected) KCTokens.ElevatedSurfaceSecondary else KCTokens.ElevatedSurface,
                label = "pill_bg"
            )
            val borderCol by animateColorAsState(
                targetValue = if (isSelected) KCTokens.ElectricBlue.copy(alpha = 0.8f) else KCTokens.GlassBorder,
                label = "pill_border"
            )

            Row(
                modifier = Modifier
                    .clip(KCTokens.PillShape)
                    .background(bgCol)
                    .border(
                        width = if (isSelected) 1.2.dp else 0.8.dp,
                        color = borderCol,
                        shape = KCTokens.PillShape
                    )
                    .clickable { onCategorySelected(cat.id) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = cat.emoji,
                    fontSize = 13.sp
                )
                Text(
                    text = cat.label,
                    fontSize = KCTokens.PillTextSize,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) KCTokens.TextPureWhite else KCTokens.TextSoftGray
                )
            }
        }
    }
}

// =========================================================================
// C. Section Header
// =========================================================================
@Composable
fun SectionHeader(
    title: String,
    onSeeMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onSeeMoreClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = KCTokens.SectionTitleSize,
            fontWeight = FontWeight.Bold,
            color = KCTokens.TextPureWhite,
            letterSpacing = (-0.3).sp
        )

        if (showChevron) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "See more $title",
                    tint = KCTokens.TextMutedGray,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

// =========================================================================
// D. Theme Card (9:16 Aspect Ratio) with NEW Badge & Geometric Art
// =========================================================================
@Composable
fun ThemeCard(
    item: ThemeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(148.dp)
            .height(256.dp)
            .shadow(elevation = 10.dp, shape = KCTokens.CardShape, spotColor = Color.Black.copy(alpha = 0.6f))
            .clickable { onClick() },
        shape = KCTokens.CardShape,
        colors = CardDefaults.cardColors(containerColor = KCTokens.ElevatedSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.04f))
            ),
            width = 0.8.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Procedural Wallpaper Preview Canvas
            ThemeVisualCanvas(item = item, modifier = Modifier.fillMaxSize())

            // Gradient Scrim for crisp text contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Top-Left "NEW" Indicator Badge
            if (item.isNew) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp)
                        .clip(KCTokens.BadgeShape)
                        .background(KCTokens.EmeraldGreen)
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "NEW",
                        color = Color.White,
                        fontSize = KCTokens.BadgeTextSize,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Bottom Labels: Title & Subtitle
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = KCTokens.CardLabelSize,
                    fontWeight = FontWeight.SemiBold,
                    color = KCTokens.TextPureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = KCTokens.CardSubtitleSize,
                    fontWeight = FontWeight.Medium,
                    color = KCTokens.TextSoftGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Procedural Art Preview for Wallpapers & Themes.
 * Generates stunning geometric, liquid glass, cyber, or minimalist visuals without heavy remote dependencies.
 */
@Composable
fun ThemeVisualCanvas(
    item: ThemeItem,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Base Gradient Atmosphere
        val baseGradient = Brush.verticalGradient(
            colors = item.gradientColors,
            startY = 0f,
            endY = h
        )
        drawRect(brush = baseGradient)

        when (item.artStyle) {
            "Liquid" -> {
                // Liquid glass caustics & refractive rings
                drawCircle(
                    color = item.secondaryAccent.copy(alpha = 0.35f),
                    radius = w * 0.55f,
                    center = Offset(w * 0.6f, h * 0.45f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = w * 0.45f,
                    center = Offset(w * 0.35f, h * 0.65f),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = item.secondaryAccent.copy(alpha = 0.45f),
                    radius = w * 0.28f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                // Diagonal specular sheen
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(0f, h * 0.3f),
                    end = Offset(w, h * 0.1f),
                    strokeWidth = 2.dp.toPx()
                )
            }
            "Minimal" -> {
                // Architectural pure geometry
                val path = Path().apply {
                    moveTo(w * 0.2f, h)
                    lineTo(w * 0.5f, h * 0.35f)
                    lineTo(w * 0.8f, h)
                    close()
                }
                drawPath(path, color = Color.White.copy(alpha = 0.08f))

                // Fine precision horizon lines
                for (i in 1..4) {
                    val y = h * 0.4f + (i * 20.dp.toPx())
                    drawLine(
                        color = Color.White.copy(alpha = 0.06f),
                        start = Offset(w * 0.15f, y),
                        end = Offset(w * 0.85f, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                drawCircle(
                    color = item.secondaryAccent,
                    radius = 4.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.32f)
                )
            }
            "Cyber" -> {
                // High-velocity cyber grid & neon glow
                drawCircle(
                    color = item.secondaryAccent.copy(alpha = 0.4f),
                    radius = w * 0.7f,
                    center = Offset(w * 0.5f, h * 0.85f)
                )
                // Horizon perspective lines
                for (step in 1..5) {
                    val y = h * 0.6f + (step * step * 3.5.dp.toPx())
                    drawLine(
                        color = item.secondaryAccent.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
            "Cosmic" -> {
                // Nebula cloud and stellar points
                drawCircle(
                    color = item.secondaryAccent.copy(alpha = 0.3f),
                    radius = w * 0.6f,
                    center = Offset(w * 0.3f, h * 0.35f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = Offset(w * 0.75f, h * 0.25f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 1.5.dp.toPx(),
                    center = Offset(w * 0.25f, h * 0.6f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 2.dp.toPx(),
                    center = Offset(w * 0.65f, h * 0.7f)
                )
            }
            else -> {
                // Geometric Abstract
                val path = Path().apply {
                    moveTo(0f, h * 0.25f)
                    quadraticTo(w * 0.5f, h * 0.55f, w, h * 0.35f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, color = item.secondaryAccent.copy(alpha = 0.2f))
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = w * 0.3f,
                    center = Offset(w * 0.7f, h * 0.4f)
                )
            }
        }
    }
}

// =========================================================================
// E. Shimmer Skeleton Card (Zero-Ad Native Loading Placeholder)
// =========================================================================
@Composable
fun ThemeShimmerCard(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer_trans")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_float"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            KCTokens.ElevatedSurface,
            Color(0xFF28282E),
            KCTokens.ElevatedSurface
        ),
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )

    Card(
        modifier = modifier
            .width(148.dp)
            .height(256.dp),
        shape = KCTokens.CardShape,
        colors = CardDefaults.cardColors(containerColor = KCTokens.ElevatedSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f))
            ),
            width = 0.8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shimmerBrush)
        ) {
            // Subtle geometric glyph in placeholder center
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .align(Alignment.Center)
            )

            // Bottom placeholder bars
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(12.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(10.dp)
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

// =========================================================================
// F. Theme Feed (Modular Vertical Carousel of Horizontal Sections)
// =========================================================================
@Composable
fun ThemeFeed(
    sections: List<ThemeSection>,
    isLoading: Boolean,
    onThemeClick: (ThemeItem) -> Unit,
    onSeeMoreClick: (ThemeSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        sections.forEach { section ->
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section Header
                SectionHeader(
                    title = section.title,
                    onSeeMoreClick = { onSeeMoreClick(section) },
                    showChevron = section.hasSeeMore
                )

                // Horizontal Carousel
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    if (isLoading) {
                        items(4) {
                            ThemeShimmerCard()
                        }
                    } else {
                        items(section.items, key = { it.id }) { theme ->
                            ThemeCard(
                                item = theme,
                                onClick = { onThemeClick(theme) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// G. Instant Zero-Ad Theme Detail Modal / Quick Apply Sheet
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDetailModal(
    theme: ThemeItem?,
    onDismiss: () -> Unit,
    onApplyWallpaper: (ThemeItem) -> Unit,
    onDownload: (ThemeItem) -> Unit
) {
    if (theme == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KCTokens.DarkCanvas,
        scrimColor = Color.Black.copy(alpha = 0.75f),
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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Hero Preview
            Card(
                modifier = Modifier
                    .width(180.dp)
                    .height(310.dp)
                    .shadow(16.dp, KCTokens.CardShape),
                shape = KCTokens.CardShape,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))
                    ),
                    width = 1.dp
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ThemeVisualCanvas(item = theme, modifier = Modifier.fillMaxSize())
                    if (theme.isNew) {
                        Box(
                            modifier = Modifier
                                .padding(top = 12.dp, start = 12.dp)
                                .clip(KCTokens.BadgeShape)
                                .background(KCTokens.EmeraldGreen)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "NEW",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title and Metadata
            Text(
                text = theme.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = KCTokens.TextPureWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = theme.subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = KCTokens.ElectricBlue
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = KCTokens.TextMutedGray
                )
                Text(
                    text = "${theme.downloadsCount} Applied",
                    fontSize = 12.sp,
                    color = KCTokens.TextSoftGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = theme.description,
                fontSize = 13.sp,
                color = KCTokens.TextSoftGray,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Zero-Ad Action Button: Instant Apply
            Button(
                onClick = {
                    onApplyWallpaper(theme)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KCTokens.ElectricBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Wallpaper,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apply to Home & Lock Screen",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Action: Download / Save
            OutlinedButton(
                onClick = {
                    onDownload(theme)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.1f))),
                    width = 1.dp
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KCTokens.TextPureWhite)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = KCTokens.TextPureWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save High-Res 4K Asset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
