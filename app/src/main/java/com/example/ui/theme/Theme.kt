package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

object LocalMindTheme {
    val tokens: LocalMindTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalLocalMindTokens.current

    val colors: LocalMindColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLocalMindTokens.current.colors

    val shapes: LocalMindShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalLocalMindTokens.current.shapes

    val typography: LocalMindTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalLocalMindTokens.current.typography

    val spacing: LocalMindSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalLocalMindTokens.current.spacing
}

@Composable
fun LocalMindTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    themeStyle: LocalMindThemeStyle = LocalMindThemeStyle.MINIMAL_EDITORIAL,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val tokens = getLocalMindTokens(style = themeStyle, isDark = isDark)

    val context = LocalContext.current
    val m3ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> darkColorScheme(
            primary = tokens.colors.accent,
            onPrimary = tokens.colors.onAccent,
            primaryContainer = tokens.colors.accentContainer,
            onPrimaryContainer = tokens.colors.onAccentContainer,
            secondary = tokens.colors.textSecondary,
            onSecondary = tokens.colors.background,
            secondaryContainer = tokens.colors.surfaceVariant,
            onSecondaryContainer = tokens.colors.textPrimary,
            tertiary = tokens.colors.statusGreen,
            background = tokens.colors.background,
            onBackground = tokens.colors.textPrimary,
            surface = tokens.colors.surface,
            onSurface = tokens.colors.textPrimary,
            surfaceVariant = tokens.colors.surfaceVariant,
            onSurfaceVariant = tokens.colors.textSecondary,
            outline = tokens.colors.border,
            outlineVariant = tokens.colors.borderSubtle
        )
        else -> lightColorScheme(
            primary = tokens.colors.accent,
            onPrimary = tokens.colors.onAccent,
            primaryContainer = tokens.colors.accentContainer,
            onPrimaryContainer = tokens.colors.onAccentContainer,
            secondary = tokens.colors.textSecondary,
            onSecondary = tokens.colors.onAccent,
            secondaryContainer = tokens.colors.surfaceVariant,
            onSecondaryContainer = tokens.colors.textPrimary,
            tertiary = tokens.colors.statusGreen,
            background = tokens.colors.background,
            onBackground = tokens.colors.textPrimary,
            surface = tokens.colors.surface,
            onSurface = tokens.colors.textPrimary,
            surfaceVariant = tokens.colors.surfaceVariant,
            onSurfaceVariant = tokens.colors.textSecondary,
            outline = tokens.colors.border,
            outlineVariant = tokens.colors.borderSubtle
        )
    }

    CompositionLocalProvider(
        LocalLocalMindTokens provides tokens
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
