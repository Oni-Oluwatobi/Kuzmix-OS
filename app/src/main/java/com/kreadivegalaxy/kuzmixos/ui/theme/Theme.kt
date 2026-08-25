package com.kreadivegalaxy.kuzmixos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val DarkColorScheme =
  darkColorScheme(
    primary = KuzmixOrange, 
    secondary = KuzmixYellow, 
    tertiary = KuzmixWhite,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = KuzmixWhite,
    onSurface = KuzmixWhite,
    primaryContainer = OrangeDark,
    onPrimaryContainer = KuzmixWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = KuzmixOrange, 
    secondary = KuzmixYellow, 
    tertiary = DarkBackground,
    background = KuzmixWhite,
    surface = Color(0xFFF5F5F5),
    onBackground = DarkBackground,
    onSurface = DarkBackground,
    primaryContainer = OrangeLight,
    onPrimaryContainer = DarkBackground
  )

val GlassTextColor: Color
  @Composable
  get() = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) DarkBackground else KuzmixWhite

val GlassSubTextColor: Color
  @Composable
  get() = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) TextSecondary else KuzmixWhite.copy(alpha = 0.7f)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
