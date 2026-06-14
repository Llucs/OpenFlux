package com.openflux.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = DarkGray,
    onPrimaryContainer = PureWhite,
    secondary = MediumGray,
    onSecondary = PureWhite,
    background = PureBlack,
    onBackground = PureWhite,
    surface = NearBlack,
    onSurface = PureWhite,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    outline = MediumGray,
    error = ErrorRed,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = NearWhite,
    onPrimaryContainer = PureBlack,
    secondary = MediumGray,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = NearWhite,
    onSurfaceVariant = MediumGray,
    outline = LightGray,
    error = ErrorRed,
    onError = PureWhite
)

@Composable
fun OpenFluxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
