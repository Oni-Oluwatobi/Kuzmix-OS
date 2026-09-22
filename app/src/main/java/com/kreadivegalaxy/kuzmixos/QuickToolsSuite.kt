package com.kreadivegalaxy.kuzmixos

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixYellow
import com.kreadivegalaxy.kuzmixos.ui.theme.DarkBackground

enum class QuickToolTab {
    CONVERTER,
    QR_GENERATOR,
    SCRATCHPAD
}

@Composable
fun QuickToolsSuiteCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(QuickToolTab.CONVERTER) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QUICK TOOLS", color = KuzmixYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickToolTab.entries.forEach { tab ->
                        val isSelected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) KuzmixYellow.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSelected) KuzmixYellow else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { activeTab = tab }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (tab) {
                                    QuickToolTab.CONVERTER -> Icons.Default.Calculate
                                    QuickToolTab.QR_GENERATOR -> Icons.Default.QrCode
                                    QuickToolTab.SCRATCHPAD -> Icons.Default.EditNote
                                },
                                contentDescription = tab.name,
                                tint = if (isSelected) KuzmixYellow else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (activeTab) {
                QuickToolTab.CONVERTER -> ConverterTool()
                QuickToolTab.QR_GENERATOR -> QrGeneratorTool()
                QuickToolTab.SCRATCHPAD -> ScratchpadTool(context)
            }
        }
    }
}

@Composable
private fun ConverterTool() {
    var amountText by remember { mutableStateOf("100") }
    var conversionType by remember { mutableStateOf(0) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val convertedResult = when (conversionType) {
        0 -> "$${String.format("%.2f", amount)} USD = ₦${String.format("%.2f", amount * 1550.0)} NGN"
        1 -> "$${String.format("%.2f", amount)} USD = €${String.format("%.2f", amount * 0.92)} EUR"
        2 -> "${String.format("%.1f", amount)} km = ${String.format("%.1f", amount * 0.621371)} Miles"
        3 -> "${String.format("%.1f", amount)} °C = ${String.format("%.1f", (amount * 9.0 / 5.0) + 32)} °F"
        else -> ""
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("USD→NGN", "USD→EUR", "km→Mi", "°C→°F").forEachIndexed { index, label ->
                val selected = conversionType == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) KuzmixYellow.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (selected) KuzmixYellow else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { conversionType = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (selected) KuzmixYellow else Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                cursorBrush = SolidColor(KuzmixYellow),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(convertedResult, color = KuzmixYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QrGeneratorTool() {
    var inputText by remember { mutableStateOf("https://kuzmixos.com") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                cursorBrush = SolidColor(KuzmixYellow),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Pattern Preview", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(10.dp)
        ) {
            val grid = 12
            val cellSize = size.width / grid
            val hash = abs(inputText.hashCode())

            for (row in 0 until grid) {
                for (col in 0 until grid) {
                    val isCornerFinder = (row < 3 && col < 3) || (row < 3 && col >= grid - 3) || (row >= grid - 3 && col < 3)
                    val bit = ((hash xor (row * 31 + col * 17)) and 1) == 1
                    if (isCornerFinder || bit) {
                        drawRoundRect(
                            color = DarkBackground,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize * 0.85f, cellSize * 0.85f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScratchpadTool(context: Context) {
    val prefs = remember { context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE) }
    var noteText by remember { mutableStateOf(prefs.getString("neural_scratchpad", "") ?: "") }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            if (noteText.isEmpty()) {
                Text("Type quick notes...", color = Color.White.copy(alpha = 0.35f), fontSize = 12.sp)
            }
            BasicTextField(
                value = noteText,
                onValueChange = {
                    noteText = it
                    prefs.edit().putString("neural_scratchpad", it).apply()
                },
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                cursorBrush = SolidColor(KuzmixYellow)
            )
        }
    }
}
