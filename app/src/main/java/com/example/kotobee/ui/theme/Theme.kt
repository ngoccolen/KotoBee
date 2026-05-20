package com.example.kotobee.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = KotoBeeText,
    secondary = KotoBeeSecondary,
    tertiary = Pink80,
    background = KotoBeeText,
    surface = Color(0xFF231B1B),
    onBackground = KotoBeePrimaryLight,
    onSurface = KotoBeePrimaryLight
)

private val LightColorScheme = lightColorScheme(
    primary = KotoBeePrimary,
    onPrimary = Color.White,
    primaryContainer = KotoBeePrimaryLight,
    onPrimaryContainer = KotoBeePrimaryDark,
    secondary = KotoBeeSecondary,
    onSecondary = Color.White,
    tertiary = Pink40,
    background = KotoBeeBackground,
    onBackground = KotoBeeText,
    surface = KotoBeeSurface,
    onSurface = KotoBeeText,
    surfaceVariant = KotoBeePrimaryLight,
    onSurfaceVariant = KotoBeeTextMuted,
    outline = KotoBeeBorder

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun KotoBeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
