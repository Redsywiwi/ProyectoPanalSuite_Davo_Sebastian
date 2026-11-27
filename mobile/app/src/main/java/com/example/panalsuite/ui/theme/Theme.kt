package com.example.panalsuite.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = HoneyPrimaryLight,
    onPrimary = HoneyOnPrimaryLight,
    primaryContainer = HoneyPrimaryContainerLight,
    onPrimaryContainer = HoneyOnPrimaryContainerLight,
    secondary = HoneySecondaryLight,
    secondaryContainer = HoneySecondaryContainerLight,
    onSecondaryContainer = HoneyOnSecondaryContainerLight,
    tertiary = HoneyTertiaryLight,
    background = HoneyBackgroundLight,
    surface = HoneySurfaceLight,
    onBackground = HoneyOnSurfaceLight,
    onSurface = HoneyOnSurfaceLight,
    surfaceVariant = HoneySurfaceVariantLight,
    onSurfaceVariant = HoneyOnSurfaceVariantLight,
    outline = HoneyOutlineLight,
    outlineVariant = HoneyOutlineVariantLight
)

@Composable
fun PanalSuiteTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
