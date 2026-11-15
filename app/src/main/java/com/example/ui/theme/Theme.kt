/**
 * ShebaDoc (সেবাডক) - Material 3 Monochrome Light & Dark Theme
 * Copyright (c) 2026 Md Shabbir Mia <mdshabbirmia516@gmail.com>. All Rights Reserved.
 * Proprietary & Confidential: Unauthorized use, copying, modification, or distribution
 * by anyone other than Md Shabbir Mia is strictly prohibited. See /LICENSE.
 */
package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Strict Monochrome Light Scheme (Crisp White, Slate Gray, Deep Obsidian Black)
private val MonochromeLightColorScheme = lightColorScheme(
    primary = MonoObsidian950,
    onPrimary = MonoPureWhite,
    primaryContainer = MonoSurfaceGray100,
    onPrimaryContainer = MonoObsidian950,
    secondary = MonoCharcoal700,
    onSecondary = MonoPureWhite,
    secondaryContainer = MonoSurfaceGray50,
    onSecondaryContainer = MonoDarkSurface800,
    tertiary = MonoCharcoal600,
    onTertiary = MonoPureWhite,
    tertiaryContainer = MonoSurfaceGray100,
    onTertiaryContainer = MonoObsidian950,
    background = MonoPureWhite,
    onBackground = MonoObsidian950,
    surface = MonoPureWhite,
    onSurface = MonoObsidian950,
    surfaceVariant = MonoSurfaceGray50,
    onSurfaceVariant = MonoCharcoal600,
    outline = MonoBorderGray200,
    outlineVariant = MonoSurfaceGray100,
    error = MonoDarkSurface800,
    onError = MonoPureWhite,
    errorContainer = MonoSurfaceGray100,
    onErrorContainer = MonoObsidian950
)

// Strict Monochrome Dark Scheme (Obsidian Black, Charcoal Gray, Pure White)
private val MonochromeDarkColorScheme = darkColorScheme(
    primary = MonoPureWhite,
    onPrimary = MonoObsidian950,
    primaryContainer = MonoDarkSurface800,
    onPrimaryContainer = MonoPureWhite,
    secondary = MonoBorderGray200,
    onSecondary = MonoObsidian950,
    secondaryContainer = MonoDarkCard900,
    onSecondaryContainer = MonoSurfaceGray100,
    tertiary = MonoMutedGray400,
    onTertiary = MonoObsidian950,
    tertiaryContainer = MonoDarkSurface800,
    onTertiaryContainer = MonoPureWhite,
    background = MonoObsidian950,
    onBackground = MonoPureWhite,
    surface = MonoDarkCard900,
    onSurface = MonoPureWhite,
    surfaceVariant = MonoDarkSurface800,
    onSurfaceVariant = MonoBorderGray200,
    outline = MonoCharcoal700,
    outlineVariant = MonoDarkSurface800,
    error = MonoSurfaceGray100,
    onError = MonoObsidian950,
    errorContainer = MonoDarkSurface800,
    onErrorContainer = MonoPureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MonochromeDarkColorScheme else MonochromeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
