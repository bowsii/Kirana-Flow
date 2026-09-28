package com.kiranaflow.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KfLightColorScheme = lightColorScheme(
    primary            = KfTeal,
    onPrimary          = KfTextOnTeal,
    primaryContainer   = KfTealDark,
    onPrimaryContainer = KfTealGlow,

    secondary            = KfAmber,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFFFF3CD),
    onSecondaryContainer = KfAmberDark,

    tertiary            = KfNavy,
    onTertiary          = Color.White,
    tertiaryContainer   = KfNavyLight,
    onTertiaryContainer = Color.White,

    error            = KfError,
    onError          = Color.White,
    errorContainer   = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF9B1B1B),

    background         = KfBgSand,
    onBackground       = KfTextDark,
    surface            = KfCard,
    onSurface          = KfTextDark,
    surfaceVariant     = KfCardTinted,
    onSurfaceVariant   = KfTextMid,
    surfaceTint        = KfTeal,

    outline            = KfBorderLight,
    outlineVariant     = KfBorderMid,

    inverseSurface     = KfNavy,
    inverseOnSurface   = Color.White,
    inversePrimary     = KfTealVivid,

    scrim              = Color(0x99000000),
)

@Composable
fun KiranaFlowTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let { win ->
                win.statusBarColor = KfBgSand.toArgb()
                win.navigationBarColor = KfNavy.toArgb()
                WindowCompat.getInsetsController(win, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(win, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = KfLightColorScheme,
        typography  = KfTypography,
        content     = content
    )
}
