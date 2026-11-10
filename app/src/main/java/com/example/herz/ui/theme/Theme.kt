package com.example.herz.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF9D4936), onPrimary = Color.White,
    primaryContainer = Color(0xFFFADBCF), onPrimaryContainer = Color(0xFF56362D),
    secondary = Color(0xFF52634C), secondaryContainer = Color(0xFFE3EAD9),
    onSecondaryContainer = Color(0xFF34432E),
    background = Color(0xFFFBF7EF), onBackground = Color(0xFF302E2A),
    surface = Color(0xFFFFFCF6), onSurface = Color(0xFF302E2A),
    surfaceVariant = Color(0xFFF0EADF), onSurfaceVariant = Color(0xFF70685D),
    outline = Color(0xFF8A8075), outlineVariant = Color(0xFFE6DFD3)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFF1AD96), onPrimary = Color(0xFF43291F),
    primaryContainer = Color(0xFF513B36), onPrimaryContainer = Color(0xFFFFDCCE),
    secondary = Color(0xFFB5C6A7), secondaryContainer = Color(0xFF354334),
    onSecondaryContainer = Color(0xFFDFEAD5),
    background = Color(0xFF202923), onBackground = Color(0xFFF4F0E7),
    surface = Color(0xFF29332C), onSurface = Color(0xFFF4F0E7),
    surfaceVariant = Color(0xFF354036), onSurfaceVariant = Color(0xFFC3C7BD),
    outline = Color(0xFF929E90), outlineVariant = Color(0xFF414D42)
)

@Composable
fun HerzTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val target = if (darkTheme) DarkColors else LightColors
    @Composable fun fade(color: Color) = animateColorAsState(color, tween(850), label = "theme color").value
    val colors = target.copy(
        primary = fade(target.primary), onPrimary = fade(target.onPrimary),
        primaryContainer = fade(target.primaryContainer), onPrimaryContainer = fade(target.onPrimaryContainer),
        secondary = fade(target.secondary), secondaryContainer = fade(target.secondaryContainer),
        onSecondaryContainer = fade(target.onSecondaryContainer),
        background = fade(target.background), onBackground = fade(target.onBackground),
        surface = fade(target.surface), onSurface = fade(target.onSurface),
        surfaceVariant = fade(target.surfaceVariant), onSurfaceVariant = fade(target.onSurfaceVariant),
        outline = fade(target.outline), outlineVariant = fade(target.outlineVariant)
    )
    MaterialTheme(colorScheme = colors,
        typography = Typography, content = content)
}
