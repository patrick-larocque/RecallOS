package com.patricklarocque.recallos.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RecallOsLightColors = lightColorScheme(
    primary = Color(0xFF0F4C5C),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF4F772D),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFCB793A),
    background = Color(0xFFF6F4EF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
)

private val RecallOsDarkColors = darkColorScheme(
    primary = Color(0xFF7CD1E3),
    onPrimary = Color(0xFF003640),
    secondary = Color(0xFFB0D18A),
    onSecondary = Color(0xFF203600),
    tertiary = Color(0xFFF6B575),
    background = Color(0xFF111417),
    surface = Color(0xFF181C20),
    onSurface = Color(0xFFE2E2E6),
)

@Composable
fun RecallOsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) RecallOsDarkColors else RecallOsLightColors,
        content = content,
    )
}
