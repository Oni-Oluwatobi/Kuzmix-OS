package com.kreadivegalaxy.kuzmixos

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Sample dataset matching user request:
 * - "Coastal Bliss" (🪙 3)
 * - "Coquette Bunny Smile" (🪙 3)
 * - "Demon Slayer" (🪙 6)
 * - "Cute Cat Ins" (🪙 3)
 * - Grouped into vibrant categories (Cute, Anime, Aesthetic, Animal).
 */
object ThemePackSampleData {
    val sampleSections = listOf(
        ThemeCategorySection(
            title = "Cute",
            emoji = "🐱",
            items = listOf(
                ThemePackItem(
                    id = "cute_cat_ins",
                    title = "Cute Cat Ins",
                    coinCost = 3,
                    isNew = true,
                    category = "Cute"
                ),
                ThemePackItem(
                    id = "coquette_bunny_smile",
                    title = "Coquette Bunny Smile",
                    coinCost = 3,
                    isNew = true,
                    category = "Cute"
                ),
                ThemePackItem(
                    id = "teddy_fluff",
                    title = "Teddy Fluff",
                    coinCost = 4,
                    isNew = false,
                    category = "Cute"
                ),
                ThemePackItem(
                    id = "peach_bear",
                    title = "Peach Bear",
                    coinCost = 3,
                    isNew = false,
                    category = "Cute"
                )
            )
        ),
        ThemeCategorySection(
            title = "Anime",
            emoji = "🧜‍♀️",
            items = listOf(
                ThemePackItem(
                    id = "demon_slayer",
                    title = "Demon Slayer",
                    coinCost = 6,
                    isNew = true,
                    category = "Anime"
                ),
                ThemePackItem(
                    id = "jujutsu_sorcerer",
                    title = "Cursed Sorcery",
                    coinCost = 5,
                    isNew = false,
                    category = "Anime"
                ),
                ThemePackItem(
                    id = "cyber_shinobi",
                    title = "Cyber Shinobi",
                    coinCost = 4,
                    isNew = false,
                    category = "Anime"
                ),
                ThemePackItem(
                    id = "ghibli_skies",
                    title = "Studio Cloudscape",
                    coinCost = 4,
                    isNew = false,
                    category = "Anime"
                )
            )
        ),
        ThemeCategorySection(
            title = "Aesthetic",
            emoji = "🌊",
            items = listOf(
                ThemePackItem(
                    id = "coastal_bliss",
                    title = "Coastal Bliss",
                    coinCost = 3,
                    isNew = true,
                    category = "Aesthetic"
                ),
                ThemePackItem(
                    id = "matcha_zen",
                    title = "Matcha Calm",
                    coinCost = 3,
                    isNew = false,
                    category = "Aesthetic"
                ),
                ThemePackItem(
                    id = "lavender_haze",
                    title = "Lavender Haze",
                    coinCost = 4,
                    isNew = false,
                    category = "Aesthetic"
                ),
                ThemePackItem(
                    id = "minimal_noir",
                    title = "Minimal Noir",
                    coinCost = 3,
                    isNew = false,
                    category = "Aesthetic"
                )
            )
        ),
        ThemeCategorySection(
            title = "Animal",
            emoji = "🐶",
            items = listOf(
                ThemePackItem(
                    id = "puppy_joy",
                    title = "Puppy Joy Club",
                    coinCost = 3,
                    isNew = true,
                    category = "Animal"
                ),
                ThemePackItem(
                    id = "capybara_chill",
                    title = "Capybara Onsen",
                    coinCost = 4,
                    isNew = false,
                    category = "Animal"
                ),
                ThemePackItem(
                    id = "red_panda_zen",
                    title = "Red Panda Forest",
                    coinCost = 3,
                    isNew = false,
                    category = "Animal"
                )
            )
        )
    )
}

/**
 * Instant Zero-Ad Theme Detail / Unlock Modal
 */
@Composable
fun ThemePackUnlockDialog(
    theme: ThemePackItem,
    userCoins: Int,
    onDismiss: () -> Unit,
    onUnlock: (ThemePackItem) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1C1C1E),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card Preview Large
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161618))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                ) {
                    ThemedAppIconGridPreview(theme = theme)

                    if (theme.isNew) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF00C853))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NEW",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = theme.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFE082), Color(0xFFFFA000))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "C",
                            color = Color(0xFF7A4B00),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = "${theme.coinCost} Coins",
                        color = Color(0xFFFFD54F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "• Category: ${theme.category}",
                        color = Color(0xFF8E8E93),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Fast Instant Action Buttons (Zero Ads)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A3A3C)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF2C2C2E)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Close", color = Color.White)
                    }

                    Button(
                        onClick = { onUnlock(theme) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0A84FF)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (userCoins >= theme.coinCost) "Unlock (${theme.coinCost}🪙)" else "Get Coins",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview Composable demonstrating the full Theme & Icon Pack feed UI.
 * Pure native Jetpack Compose with zero ad wrappers or delays.
 */
@Composable
fun ThemeFeedPreview(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var userCoins by remember { mutableIntStateOf(120) }
    var activeModalTheme by remember { mutableStateOf<ThemePackItem?>(null) }
    val sections = remember { ThemePackSampleData.sampleSections }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0E))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Coin Balance & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Icon Packs & Themes",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Instant 3x3 App Icon Grid Collections",
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp
                    )
                }

                // Coin Balance Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF1C1C1E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2E))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFE082), Color(0xFFFFA000))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                color = Color(0xFF7A4B00),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "$userCoins",
                            color = Color(0xFFFFD54F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Feed Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(
                    items = sections,
                    key = { it.title }
                ) { section ->
                    ThemeSectionRow(
                        section = section,
                        onItemClick = { item ->
                            activeModalTheme = item
                        },
                        onSeeMoreClick = {
                            Toast.makeText(
                                context,
                                "Showing all ${section.title} packs",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }

        // Fast zero-ad unlock modal
        activeModalTheme?.let { theme ->
            ThemePackUnlockDialog(
                theme = theme,
                userCoins = userCoins,
                onDismiss = { activeModalTheme = null },
                onUnlock = { unlocked ->
                    if (userCoins >= unlocked.coinCost) {
                        userCoins -= unlocked.coinCost
                        Toast.makeText(
                            context,
                            "Unlocked ${unlocked.title}! Applied icon pack.",
                            Toast.LENGTH_SHORT
                        ).show()
                        activeModalTheme = null
                    } else {
                        Toast.makeText(
                            context,
                            "Need ${unlocked.coinCost - userCoins} more coins!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B0E)
@Composable
fun ThemeFeedPreviewComponent() {
    ThemeFeedPreview()
}
