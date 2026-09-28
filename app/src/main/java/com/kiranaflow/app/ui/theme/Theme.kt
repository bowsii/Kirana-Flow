package com.kiranaflow.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KfDarkColorScheme = darkColorScheme(
    // Primary — Emerald Green
    primary            = KfEmeraldBright,
    onPrimary          = KfBackgroundDeep,
    primaryContainer   = KfEmeraldDark,
    onPrimaryContainer = KfEmeraldGlow,

    // Secondary — Amber/Gold
    secondary            = KfAmberBright,
    onSecondary          = KfBackgroundDeep,
    secondaryContainer   = KfAmberDark,
    onSecondaryContainer = KfAmberGlow,

    // Tertiary — soft warm highlight
    tertiary            = KfAmberGold,
    onTertiary          = KfBackgroundDeep,
    tertiaryContainer   = Color(0xFF4A3000),
    onTertiaryContainer = KfAmberGlow,

    // Error
    error            = KfError,
    onError          = Color.White,
    errorContainer   = Color(0xFF4D0000),
    onErrorContainer = Color(0xFFFFB3B3),

    // Background & Surface
    background         = KfBackgroundDeep,
    onBackground       = KfTextPrimary,
    surface            = KfSurface,
    onSurface          = KfTextPrimary,
    surfaceVariant     = KfSurfaceElevated,
    onSurfaceVariant   = KfTextSecondary,
    surfaceTint        = KfEmeraldBright,

    // Outline
    outline            = KfBorderSubtle,
    outlineVariant     = KfBorderStrong,

    // Inverse
    inverseSurface     = KfTextPrimary,
    inverseOnSurface   = KfBackgroundDeep,
    inversePrimary     = KfEmerald,

    // Scrim & container
    scrim              = Color(0xCC000000),
)

@Composable
fun KiranaFlowTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = KfDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = KfBackgroundDeep.toArgb()
            window.navigationBarColor = KfBackgroundDeep.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = KfTypography,
        content     = content
    )
}
