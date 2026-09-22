package com.kreadivegalaxy.kuzmixos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = KuzmixOrange,
    secondary = KuzmixYellow,
    tertiary = ElectricPurple,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = KuzmixWhite,
    onSecondary = DarkBackground,
    onBackground = KuzmixWhite,
    onSurface = KuzmixWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = GlassSubTextColor,
)

private val LightColorScheme = lightColorScheme(
    primary = KuzmixOrange,
    secondary = KuzmixYellow,
    tertiary = ElectricPurple,
    background = KuzmixWhite,
    surface = KuzmixWhite,
    onPrimary = KuzmixWhite,
    onSecondary = DarkBackground,
    onBackground = DarkBackground,
    onSurface = DarkBackground,
    surfaceVariant = DarkCard,
    onSurfaceVariant = GlassSubTextColor,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
