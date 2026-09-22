package com.kreadivegalaxy.kuzmixos

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Currency model for theme and icon pack purchases.
 */
data class CoinPrice(
    val amount: Int
)

/**
 * Data model representing an App Icon Pack & Theme item.
 */
data class ThemePackItem(
    val id: String,
    val title: String,
    val previewImageUrl: String = "",
    val isNew: Boolean = false,
    val coinCost: Int,
    val category: String,
    val accentColors: List<Color> = emptyList(),
    val tagText: String? = null,
    val downloads: String = "12.4k",
    val rating: Float = 4.9f
)

/**
 * Category section model for grouping theme packs in horizontal carousels.
 */
data class ThemeCategorySection(
    val title: String,
    val emoji: String,
    val items: List<ThemePackItem>
)
