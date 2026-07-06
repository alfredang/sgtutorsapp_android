package com.alfredang.sgtutors.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Central theme tokens — mirrors the web app's indigo brand palette.
object Brand {
    val Indigo = Color(0xFF4F46E5)
    val IndigoLight = Color(0xFF6366F1)
    val Star = Color(0xFFF59E0B)
    val Verified = Color(0xFF16A34A)
    val Featured = Color(0xFFF2B81A)
}

private val LightColors = lightColorScheme(
    primary = Brand.Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Brand.IndigoLight,
    onSecondary = Color.White,
    background = Color(0xFFF4F4F6),
    onBackground = Color(0xFF1B1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFEDEDF2),
    onSurfaceVariant = Color(0xFF56565E),
)

private val DarkColors = darkColorScheme(
    primary = Brand.IndigoLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Brand.IndigoLight,
    onSecondary = Color.White,
    background = Color(0xFF121216),
    onBackground = Color(0xFFE4E4E8),
    surface = Color(0xFF1D1D22),
    onSurface = Color(0xFFE4E4E8),
    surfaceVariant = Color(0xFF2A2A31),
    onSurfaceVariant = Color(0xFFAAAAB4),
)

@Composable
fun SGTutorsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
