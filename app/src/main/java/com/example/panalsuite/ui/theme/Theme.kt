package com.example.panalsuite.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HoneyPrimaryDark,
    onPrimary = HoneyOnPrimaryDark,
    secondary = HoneySecondaryDark,
    tertiary = HoneyTertiaryDark,
    background = HoneyBackgroundDark,
    surface = HoneySurfaceDark,
    onBackground = HoneyOnSurfaceDark,
    onSurface = HoneyOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = HoneyPrimaryLight,
    onPrimary = HoneyOnPrimaryLight,
    secondary = HoneySecondaryLight,
    tertiary = HoneyTertiaryLight,
    background = HoneyBackgroundLight,
    surface = HoneySurfaceLight,
    onBackground = HoneyOnSurfaceLight,
    onSurface = HoneyOnSurfaceLight
)

@Composable
fun PanalSuiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
