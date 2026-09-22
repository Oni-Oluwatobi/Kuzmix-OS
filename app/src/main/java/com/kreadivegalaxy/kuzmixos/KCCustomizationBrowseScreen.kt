package com.kreadivegalaxy.kuzmixos

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Complete browsing interface for "KC Customization".
 * Replicates the clean, modern dark-mode aesthetic of high-end iOS wallpaper & theme stores.
 * Completely removes all ads, ad loaders, and sponsored placeholders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KCCustomizationBrowseScreen(
    onSelectWallpaperKey: (String) -> Unit,
    onOpenDeviceStudio: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Navigation & Filter State
    var selectedTab by remember { mutableStateOf("featured") } // "featured" or "top"
    var selectedCategoryId by remember { mutableStateOf("all") }

    // 2. Loading & Pull-to-Refresh State (Zero-Ad Architecture)
    var isRefreshing by remember { mutableStateOf(false) }
    var isInitialLoading by remember { mutableStateOf(true) }

    // Selected theme for instant zero-ad preview modal
    var activePreviewTheme by remember { mutableStateOf<ThemeItem?>(null) }

    // Simulate instant smooth native launch (replaces ad delays with smooth shimmer)
    LaunchedEffect(Unit) {
        delay(400) // Brief initial skeleton shimmer before instant presentation
        isInitialLoading = false
    }

    // Dynamic Filtered Data based on selectedTab and selectedCategoryId
    val currentSections by remember(selectedTab, selectedCategoryId) {
        derivedStateOf {
            val baseSections = if (selectedTab == "featured") {
                KCCustomizationMockData.featuredSections
            } else {
                KCCustomizationMockData.topSections
            }

            if (selectedCategoryId == "all") {
                baseSections
            } else {
                baseSections.mapNotNull { section ->
                    val filteredItems = section.items.filter { it.categoryId == selectedCategoryId }
                    if (filteredItems.isNotEmpty()) {
                        section.copy(items = filteredItems)
                    } else null
                }
            }
        }
    }

    // Pull-to-Refresh trigger (Instant zero-ad data sync)
    val onRefresh: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            delay(500) // Clean native sync delay without ads
            isRefreshing = false
            Toast.makeText(context, "Themes updated to latest catalogue", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KCTokens.DarkCanvas)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // A. Sticky Top Navigation Bar (Segmented "Featured" vs "Top")
            TopViewSwitcher(
                selectedTab = selectedTab,
                onTabSelected = { newTab ->
                    if (selectedTab != newTab) {
                        selectedTab = newTab
                    }
                },
                onCloseClick = onClose
            )

            // B. Content Switcher based on Selected Tab
            if (selectedTab == "themes") {
                ThemeFeedPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                // Horizontal Category Filter Carousel (Single row of pill chips)
                CategoryPillBar(
                    categories = KCCustomizationMockData.categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { newCat ->
                        selectedCategoryId = newCat
                    }
                )

                // C. Main Feed (Vertical Scroll of Modular Carousels) with Pull-to-Refresh
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (currentSections.isEmpty() && !isInitialLoading && !isRefreshing) {
                        // Empty state for category
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "✨",
                                    fontSize = 42.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "More Themes Coming Soon",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KCTokens.TextPureWhite
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Explore our Featured and Top collections in other categories.",
                                    fontSize = 13.sp,
                                    color = KCTokens.TextMutedGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { selectedCategoryId = "all" },
                                    colors = ButtonDefaults.buttonColors(containerColor = KCTokens.ElevatedSurfaceSecondary),
                                    shape = KCTokens.PillShape
                                ) {
                                    Text("View All Categories", color = KCTokens.ElectricBlue)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
                        ) {
                            item {
                                ThemeFeed(
                                    sections = currentSections,
                                    isLoading = isInitialLoading || isRefreshing,
                                    onThemeClick = { theme ->
                                        // Open instant zero-ad preview modal
                                        activePreviewTheme = theme
                                    },
                                    onSeeMoreClick = { section ->
                                        Toast.makeText(context, "Showing all in ${section.title}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Device Studio / System Tools Button (Bottom Right)
        if (onOpenDeviceStudio != null) {
            FloatingActionButton(
                onClick = onOpenDeviceStudio,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp)
                    .navigationBarsPadding(),
                containerColor = KCTokens.ElevatedSurfaceSecondary,
                contentColor = KCTokens.ElectricBlue,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Device Studio",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Studio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Instant Zero-Ad Theme Modal
        ThemeDetailModal(
            theme = activePreviewTheme,
            onDismiss = { activePreviewTheme = null },
            onApplyWallpaper = { theme ->
                onSelectWallpaperKey(theme.wallpaperKey)
                Toast.makeText(context, "Applied '${theme.title}' to Kuzmix OS!", Toast.LENGTH_SHORT).show()
            },
            onDownload = { theme ->
                Toast.makeText(context, "Asset for '${theme.title}' saved in 4K UHD!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
