package com.kiranaflow.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// Using system fonts that match the modern bold UI shown in the design image.
// The design shows a clean sans-serif with bold numerals.

val KfFontFamily = FontFamily.Default

val KfTypography = Typography(
    // Display — bill amounts, totals
    displayLarge  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Black,
        fontSize   = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        color = KfTextPrimary
    ),
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 45.sp,
        lineHeight = 52.sp,
        color = KfTextPrimary
    ),
    displaySmall  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 36.sp,
        lineHeight = 44.sp,
        color = KfTextPrimary
    ),
    // Headline
    headlineLarge  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 32.sp,
        lineHeight = 40.sp,
        color = KfTextPrimary
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
        color = KfTextPrimary
    ),
    headlineSmall  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp,
        color = KfTextPrimary
    ),
    // Title
    titleLarge  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
        color = KfTextPrimary
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = KfTextPrimary
    ),
    titleSmall  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = KfTextSecondary
    ),
    // Body
    bodyLarge   = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = KfTextPrimary
    ),
    bodyMedium  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = KfTextSecondary
    ),
    bodySmall   = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = KfTextDisabled
    ),
    // Label
    labelLarge  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = KfTextPrimary
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = KfTextSecondary
    ),
    labelSmall  = androidx.compose.ui.text.TextStyle(
        fontFamily = KfFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = KfTextDisabled
    )
)
