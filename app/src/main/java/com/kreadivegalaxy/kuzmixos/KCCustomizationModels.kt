package com.kreadivegalaxy.kuzmixos

import androidx.compose.ui.graphics.Color

/**
 * Structured Mock Data Models for KC Customization.
 */
data class Category(
    val id: String,
    val emoji: String,
    val label: String
)

data class ThemeItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val categoryId: String,
    val isNew: Boolean = false,
    val isTop: Boolean = false,
    val wallpaperKey: String = "ocean", // Maps to existing launcher engine or custom rendering
    val gradientColors: List<Color>,
    val secondaryAccent: Color = Color(0xFF0A84FF),
    val artStyle: String = "Liquid", // "Liquid", "Geometric", "Minimal", "Cyber", "Cosmic", "Cute"
    val downloadsCount: String = "124K",
    val description: String = "High-definition ultra-minimalist OLED design crafted for iOS & Kuzmix OS."
)

data class ThemeSection(
    val id: String,
    val title: String,
    val hasSeeMore: Boolean = true,
    val items: List<ThemeItem>
)

object KCCustomizationMockData {
    val categories = listOf(
        Category("all", "🔥", "All"),
        Category("anime", "🐰", "Anime"),
        Category("minimalist", "✨", "Minimalist"),
        Category("cute", "🎀", "Cute"),
        Category("automotive", "🏎️", "Automotive"),
        Category("abstract", "🌌", "Abstract"),
        Category("liquid_glass", "🧊", "Liquid Glass"),
        Category("cosmic", "🪐", "Cosmic"),
        Category("botanical", "🌿", "Botanical")
    )

    // Themes
    val sunsetDrift = ThemeItem(
        id = "t_sunset_drift",
        title = "Neon Mirage",
        subtitle = "OS 27",
        categoryId = "automotive",
        isNew = true,
        isTop = true,
        wallpaperKey = "sunset",
        gradientColors = listOf(Color(0xFF19062A), Color(0xFF4A0E4E), Color(0xFFFF5722)),
        secondaryAccent = Color(0xFFFF7043),
        artStyle = "Cyber",
        downloadsCount = "340K",
        description = "High-velocity synthwave gradients inspired by midnight highway runs."
    )

    val frostedTitanium = ThemeItem(
        id = "t_frosted_titanium",
        title = "Liquid Aerogel",
        subtitle = "Liquid Glass",
        categoryId = "liquid_glass",
        isNew = true,
        isTop = true,
        wallpaperKey = "ocean",
        gradientColors = listOf(Color(0xFF04101E), Color(0xFF082846), Color(0xFF00ADB5)),
        secondaryAccent = Color(0xFF00ADB5),
        artStyle = "Liquid",
        downloadsCount = "512K",
        description = "Prismatic caustics and fluid specular reflections on deep glass."
    )

    val minimalistOled = ThemeItem(
        id = "t_minimalist_oled",
        title = "Obsidian Horizon",
        subtitle = "Minimalist",
        categoryId = "minimalist",
        isNew = false,
        isTop = true,
        wallpaperKey = "burj",
        gradientColors = listOf(Color(0xFF000000), Color(0xFF0F0F14), Color(0xFF1E1E26)),
        secondaryAccent = Color(0xFF0A84FF),
        artStyle = "Minimal",
        downloadsCount = "680K",
        description = "Absolute true-black geometry built specifically to save battery and reduce eye fatigue."
    )

    val kawaiiDream = ThemeItem(
        id = "t_kawaii_dream",
        title = "Marshmallow Moon",
        subtitle = "Cute Pastel",
        categoryId = "cute",
        isNew = true,
        isTop = false,
        wallpaperKey = "sunset",
        gradientColors = listOf(Color(0xFF261226), Color(0xFF592348), Color(0xFFFF85A1)),
        secondaryAccent = Color(0xFFFF85A1),
        artStyle = "Cute",
        downloadsCount = "188K",
        description = "Soothing dreamy pastel accents with floating fluffy clouds and gentle luminescence."
    )

    val cyberTokyo = ThemeItem(
        id = "t_cyber_tokyo",
        title = "Akira Skyline",
        subtitle = "Anime Cyber",
        categoryId = "anime",
        isNew = true,
        isTop = true,
        wallpaperKey = "sunset",
        gradientColors = listOf(Color(0xFF0D0221), Color(0xFF240046), Color(0xFFFF007F)),
        secondaryAccent = Color(0xFFFF007F),
        artStyle = "Cyber",
        downloadsCount = "420K",
        description = "Neo-Tokyo retro-futurism with stark kanji lines and crimson particle streaks."
    )

    val deepNebula = ThemeItem(
        id = "t_deep_nebula",
        title = "Orion Event",
        subtitle = "Cosmic 4K",
        categoryId = "cosmic",
        isNew = false,
        isTop = true,
        wallpaperKey = "airplane",
        gradientColors = listOf(Color(0xFF03071E), Color(0xFF370617), Color(0xFF6A040F), Color(0xFFD00000)),
        secondaryAccent = Color(0xFFFFBA08),
        artStyle = "Cosmic",
        downloadsCount = "290K",
        description = "Deep stellar photography simulation with volumetric cosmic gas clouds."
    )

    val os27Prism = ThemeItem(
        id = "t_os27_prism",
        title = "Dynamic Spectrum",
        subtitle = "OS 27 Theme Pack",
        categoryId = "abstract",
        isNew = true,
        isTop = true,
        wallpaperKey = "ocean",
        gradientColors = listOf(Color(0xFF0A1128), Color(0xFF001F54), Color(0xFF034078), Color(0xFF1282A2)),
        secondaryAccent = Color(0xFF0A84FF),
        artStyle = "Geometric",
        downloadsCount = "890K",
        description = "Official OS 27 next-generation interface pack with cohesive icon set."
    )

    val os27Monolith = ThemeItem(
        id = "t_os27_monolith",
        title = "Titanium Dark",
        subtitle = "OS 27 Theme Pack",
        categoryId = "minimalist",
        isNew = true,
        isTop = false,
        wallpaperKey = "burj",
        gradientColors = listOf(Color(0xFF0B0B0E), Color(0xFF16161C), Color(0xFF2A2A36)),
        secondaryAccent = Color(0xFFE2E8F0),
        artStyle = "Minimal",
        downloadsCount = "450K",
        description = "Precision-machined dark industrial aesthetic for the discerning minimalist."
    )

    val superCarApex = ThemeItem(
        id = "t_supercar_apex",
        title = "Carbon Track",
        subtitle = "Automotive",
        categoryId = "automotive",
        isNew = false,
        isTop = true,
        wallpaperKey = "airplane",
        gradientColors = listOf(Color(0xFF0B0C10), Color(0xFF1F2833), Color(0xFFC5C6C7), Color(0xFF45A29E)),
        secondaryAccent = Color(0xFF66FCF1),
        artStyle = "Cyber",
        downloadsCount = "230K",
        description = "Matte carbon weave backgrounds with aerodynamic brake disc heat glow."
    )

    val botanicalMidnight = ThemeItem(
        id = "t_botanical_midnight",
        title = "Emerald Monsoon",
        subtitle = "Botanical",
        categoryId = "botanical",
        isNew = true,
        isTop = false,
        wallpaperKey = "ocean",
        gradientColors = listOf(Color(0xFF021B16), Color(0xFF0B3C35), Color(0xFF1F6E60), Color(0xFF38B29C)),
        secondaryAccent = Color(0xFF38B29C),
        artStyle = "Liquid",
        downloadsCount = "175K",
        description = "Lush midnight rain drops on monstera leaves with deep moss shadows."
    )

    // Modular Sections for "Featured" Tab
    val featuredSections = listOf(
        ThemeSection(
            id = "popular_topics",
            title = "Popular Topics",
            hasSeeMore = true,
            items = listOf(frostedTitanium, os27Prism, sunsetDrift, minimalistOled, cyberTokyo)
        ),
        ThemeSection(
            id = "just_released",
            title = "Just Released",
            hasSeeMore = true,
            items = listOf(os27Monolith, kawaiiDream, botanicalMidnight, sunsetDrift, frostedTitanium)
        ),
        ThemeSection(
            id = "popular_collection",
            title = "Popular Collection",
            hasSeeMore = true,
            items = listOf(minimalistOled, frostedTitanium, deepNebula, superCarApex, os27Prism)
        ),
        ThemeSection(
            id = "os27_theme_packs",
            title = "OS 27 Theme Packs",
            hasSeeMore = true,
            items = listOf(os27Prism, os27Monolith, frostedTitanium, minimalistOled)
        ),
        ThemeSection(
            id = "liquid_glass_themes",
            title = "Liquid Glass & Translucent",
            hasSeeMore = true,
            items = listOf(frostedTitanium, botanicalMidnight, os27Prism, kawaiiDream)
        )
    )

    // Modular Sections for "Top" Tab
    val topSections = listOf(
        ThemeSection(
            id = "top_trending",
            title = "Trending This Week",
            hasSeeMore = true,
            items = listOf(os27Prism, minimalistOled, frostedTitanium, sunsetDrift, deepNebula)
        ),
        ThemeSection(
            id = "top_all_time",
            title = "All-Time Hall of Fame",
            hasSeeMore = true,
            items = listOf(minimalistOled, cyberTokyo, superCarApex, os27Monolith, frostedTitanium)
        ),
        ThemeSection(
            id = "top_curators_choice",
            title = "Curator's Spotlight",
            hasSeeMore = true,
            items = listOf(sunsetDrift, botanicalMidnight, kawaiiDream, os27Prism)
        )
    )
}
