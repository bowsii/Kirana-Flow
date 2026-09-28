package com.kiranaflow.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light colour scheme — warm sand base matching the reference design
private val KfLightColorScheme = lightColorScheme(
    // Primary — Teal/Forest Green
    primary            = KfTeal,
    onPrimary          = KfTextOnTeal,
    primaryContainer   = KfTealDark,
    onPrimaryContainer = KfTealGlow,

    // Secondary — Amber/Gold
    secondary            = KfAmber,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFFFF3CD),
    onSecondaryContainer = KfAmberDark,

    // Tertiary
    tertiary            = KfNavy,
    onTertiary          = Color.White,
    tertiaryContainer   = KfNavyLight,
    onTertiaryContainer = Color.White,

    // Error
    error            = KfError,
    onError          = Color.White,
    errorContainer   = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF9B1B1B),

    // Background & Surface — warm sand
    background         = KfBgSand,
    onBackground       = KfTextDark,
    surface            = KfCard,
    onSurface          = KfTextDark,
    surfaceVariant     = KfCardTinted,
    onSurfaceVariant   = KfTextMid,
    surfaceTint        = KfTeal,

    // Outline
    outline            = KfBorderLight,
    outlineVariant     = KfBorderMid,

    // Inverse
    inverseSurface     = KfNavy,
    inverseOnSurface   = Color.White,
    inversePrimary     = KfTealVivid,

    // Scrim
    scrim              = Color(0x99000000),
)

@Composable
fun KiranaFlowTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = KfBgSand.toArgb()
            window.navigationBarColor = KfNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = KfLightColorScheme,
        typography  = KfTypography,
        content     = content
    )
}
