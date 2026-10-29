package com.example.caminata20.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified

private val LightColors = lightColorScheme(
    primary = BreakeeLight.primary, onPrimary = BreakeeLight.onPrimary,
    primaryContainer = BreakeeLight.primaryContainer, onPrimaryContainer = BreakeeLight.onPrimaryContainer,
    secondary = BreakeeLight.secondary, onSecondary = BreakeeLight.onSecondary,
    secondaryContainer = BreakeeLight.secondaryContainer, onSecondaryContainer = BreakeeLight.onSecondaryContainer,
    background = BreakeeLight.background, onBackground = BreakeeLight.onBackground,
    surface = BreakeeLight.surface, onSurface = BreakeeLight.onSurface,
    surfaceVariant = BreakeeLight.surfaceVariant, onSurfaceVariant = BreakeeLight.onSurfaceVariant,
    outline = BreakeeLight.outline, error = BreakeeLight.error, errorContainer = BreakeeLight.errorContainer,
    inverseSurface = BreakeeLight.inverseSurface, inverseOnSurface = BreakeeLight.inverseOnSurface,
)

private val DarkColors = darkColorScheme(
    primary = BreakeeDark.primary, onPrimary = BreakeeDark.onPrimary,
    primaryContainer = BreakeeDark.primaryContainer, onPrimaryContainer = BreakeeDark.onPrimaryContainer,
    secondary = BreakeeDark.secondary, onSecondary = BreakeeDark.onSecondary,
    secondaryContainer = BreakeeDark.secondaryContainer, onSecondaryContainer = BreakeeDark.onSecondaryContainer,
    background = BreakeeDark.background, onBackground = BreakeeDark.onBackground,
    surface = BreakeeDark.surface, onSurface = BreakeeDark.onSurface,
    surfaceVariant = BreakeeDark.surfaceVariant, onSurfaceVariant = BreakeeDark.onSurfaceVariant,
    outline = BreakeeDark.outline, error = BreakeeDark.error, errorContainer = BreakeeDark.errorContainer,
    inverseSurface = BreakeeDark.inverseSurface, inverseOnSurface = BreakeeDark.inverseOnSurface,
)

/** Colores de estado que Material3 no trae (éxito / advertencia). */
@Immutable
data class BreakeeStatusColors(
    val success: Color, val successContainer: Color,
    val warning: Color, val warningContainer: Color,
)

val LocalStatusColors = staticCompositionLocalOf {
    BreakeeStatusColors(BreakeeLight.success, BreakeeLight.successContainer, BreakeeLight.warning, BreakeeLight.warningContainer)
}

val BreakeeShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

private fun scaledTypography(scale: Float): androidx.compose.material3.Typography {
    if (scale == 1f) return BreakeeTypography
    fun androidx.compose.ui.text.TextStyle.scaled() = copy(
        fontSize = if (fontSize.isUnspecified) fontSize else fontSize * scale,
        lineHeight = if (lineHeight.isUnspecified) lineHeight else lineHeight * scale,
    )
    return BreakeeTypography.copy(
        displayLarge = BreakeeTypography.displayLarge.scaled(),
        displaySmall = BreakeeTypography.displaySmall.scaled(),
        headlineMedium = BreakeeTypography.headlineMedium.scaled(),
        headlineSmall = BreakeeTypography.headlineSmall.scaled(),
        titleLarge = BreakeeTypography.titleLarge.scaled(),
        titleMedium = BreakeeTypography.titleMedium.scaled(),
        titleSmall = BreakeeTypography.titleSmall.scaled(),
        bodyLarge = BreakeeTypography.bodyLarge.scaled(),
        bodyMedium = BreakeeTypography.bodyMedium.scaled(),
        bodySmall = BreakeeTypography.bodySmall.scaled(),
        labelLarge = BreakeeTypography.labelLarge.scaled(),
        labelMedium = BreakeeTypography.labelMedium.scaled(),
        labelSmall = BreakeeTypography.labelSmall.scaled(),
    )
}

@Composable
fun BreakeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val status = if (darkTheme) {
        BreakeeStatusColors(BreakeeDark.success, BreakeeDark.successContainer, BreakeeDark.warning, BreakeeDark.warningContainer)
    } else {
        BreakeeStatusColors(BreakeeLight.success, BreakeeLight.successContainer, BreakeeLight.warning, BreakeeLight.warningContainer)
    }
    CompositionLocalProvider(LocalStatusColors provides status) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = scaledTypography(textScale),
            shapes = BreakeeShapes,
            content = content,
        )
    }
}

object BreakeeThemeExt {
    val status: BreakeeStatusColors
        @Composable get() = LocalStatusColors.current
}
