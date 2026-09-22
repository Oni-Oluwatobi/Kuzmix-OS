package com.kreadivegalaxy.kuzmixos

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Horizontal Carousel Row displaying a category section with a header and ThemePackCards.
 * Features smooth scrolling with optional snapping.
 */
@Composable
fun ThemeSectionRow(
    section: ThemeCategorySection,
    onItemClick: (ThemePackItem) -> Unit,
    onSeeMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        // Section Header with Emoji & "View All" Chevron
        CategorySectionHeader(
            emoji = section.emoji,
            title = section.title,
            onSeeMoreClick = onSeeMoreClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Carousel
        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = section.items,
                key = { it.id }
            ) { item ->
                ThemePackCard(
                    theme = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}
