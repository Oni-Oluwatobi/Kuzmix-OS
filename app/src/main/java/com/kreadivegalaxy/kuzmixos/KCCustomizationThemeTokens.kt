package com.kreadivegalaxy.kuzmixos

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Global Design System & Theme Tokens for KC Customization.
 * Replicates the clean, modern dark-mode aesthetic of high-end iOS theme stores.
 */
object KCTokens {
    // 1. Base Backgrounds & Surfaces
    val OledBlack = Color(0xFF000000)
    val DarkCanvas = Color(0xFF0B0B0E)
    val ElevatedSurface = Color(0xFF161618)
    val ElevatedSurfaceSecondary = Color(0xFF1C1C20)
    val GlassCardBg = Color(0x0FFFFFFF)
    val GlassBorder = Color(0x14FFFFFF)
    val SubtleDivider = Color(0x1AFFFFFF)

    // 2. Primary Accents & Highlights
    val ElectricBlue = Color(0xFF0A84FF) // iOS Electric Blue
    val ElectricBlueSubtle = Color(0x260A84FF)
    val EmeraldGreen = Color(0xFF30D158) // Bright iOS Emerald for "NEW" Badges
    val EmeraldGreenGlow = Color(0x4030D158)

    // 3. Typography Tokens
    val TextPureWhite = Color(0xFFFFFFFF)
    val TextMutedGray = Color(0xFF8E8E93)
    val TextSoftGray = Color(0xFFA1A1A6)
    val TextDisabled = Color(0xFF545458)

    // Font Sizing
    val SectionTitleSize = 19.sp
    val CardLabelSize = 14.sp
    val CardSubtitleSize = 12.sp
    val BadgeTextSize = 10.sp
    val TabTextSize = 15.sp
    val PillTextSize = 13.sp

    // 4. Corner Radii
    val CardRadius = 18.dp
    val CardShape = RoundedCornerShape(18.dp)
    val PillShape = RoundedCornerShape(9999.dp)
    val BadgeShape = RoundedCornerShape(9999.dp)
    val ModalTopShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}
