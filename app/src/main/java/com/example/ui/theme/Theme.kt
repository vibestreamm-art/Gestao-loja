package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GestaoDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = OnGold,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldLight,
    secondary = GoldLight,
    onSecondary = OnGold,
    secondaryContainer = ElevatedSurface,
    onSecondaryContainer = WhiteText,
    tertiary = EmeraldSuccess,
    onTertiary = OnGold,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldSuccess,
    background = DeepBlack,
    onBackground = WhiteText,
    surface = CharcoalSurface,
    onSurface = WhiteText,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = GraySecondary,
    surfaceContainerLowest = DeepBlack,
    surfaceContainerLow = CharcoalSurface,
    surfaceContainer = CharcoalSurface,
    surfaceContainerHigh = ElevatedSurface,
    surfaceContainerHighest = ElevatedSurface,
    outline = SubtleBorder,
    outlineVariant = SubtleBorder,
    error = CoralDanger,
    onError = WhiteText,
    errorContainer = CoralContainer,
    onErrorContainer = CoralDanger
)

@Composable
fun GestaoStoreTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GestaoDarkColorScheme,
        typography = GestaoTypography,
        shapes = GestaoShapes,
        content = content
    )
}
