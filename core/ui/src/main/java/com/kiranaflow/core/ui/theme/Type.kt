package com.kiranaflow.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val KfFontFamily = FontFamily.Default

val KfTypography = Typography(
    displayLarge  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Black,     fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp, color = KfTextDark),
    displayMedium = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Bold,      fontSize = 45.sp, lineHeight = 52.sp, color = KfTextDark),
    displaySmall  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Bold,      fontSize = 36.sp, lineHeight = 44.sp, color = KfTextDark),
    headlineLarge  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Bold,     fontSize = 32.sp, lineHeight = 40.sp, color = KfTextDark),
    headlineMedium = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, color = KfTextDark),
    headlineSmall  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, color = KfTextDark),
    titleLarge  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, color = KfTextDark),
    titleMedium = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, color = KfTextDark),
    titleSmall  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp, color = KfTextMid),
    bodyLarge   = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp, color = KfTextDark),
    bodyMedium  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp, color = KfTextMid),
    bodySmall   = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp, color = KfTextLight),
    labelLarge  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, color = KfTextDark),
    labelMedium = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp, color = KfTextMid),
    labelSmall  = TextStyle(fontFamily = KfFontFamily, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp, color = KfTextLight)
)
