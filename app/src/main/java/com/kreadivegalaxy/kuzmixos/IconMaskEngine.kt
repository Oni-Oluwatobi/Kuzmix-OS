package com.kreadivegalaxy.kuzmixos

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

enum class LauncherIconMask(
    val key: String,
    val label: String
) {
    SQUIRCLE("squircle", "Squircle"),
    CIRCLE("circle", "Circle"),
    TEARDROP("teardrop", "Teardrop"),
    ROUNDED_SQUARE("rounded_square", "Rounded Sq"),
    HEXAGON("hexagon", "Hexagon"),
    PILLOW("pillow", "Stadium");

    companion object {
        fun fromKey(key: String): LauncherIconMask {
            return values().firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SQUIRCLE
        }
    }
}

val HexagonShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, 0f)
    lineTo(w * 0.95f, h * 0.25f)
    lineTo(w * 0.95f, h * 0.75f)
    lineTo(w * 0.5f, h)
    lineTo(w * 0.05f, h * 0.75f)
    lineTo(w * 0.05f, h * 0.25f)
    close()
}

fun getIconMaskShape(maskKey: String): Shape {
    return when (LauncherIconMask.fromKey(maskKey)) {
        LauncherIconMask.SQUIRCLE -> RoundedCornerShape(18.dp)
        LauncherIconMask.CIRCLE -> CircleShape
        LauncherIconMask.TEARDROP -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 6.dp)
        LauncherIconMask.ROUNDED_SQUARE -> RoundedCornerShape(10.dp)
        LauncherIconMask.HEXAGON -> HexagonShape
        LauncherIconMask.PILLOW -> RoundedCornerShape(26.dp)
    }
}
