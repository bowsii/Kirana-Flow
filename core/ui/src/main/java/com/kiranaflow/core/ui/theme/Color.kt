package com.kiranaflow.core.ui.theme

import androidx.compose.ui.graphics.Color

// ─── KiranaFlow Design Tokens — Warm Sand + Teal + Navy ──────────────────────
// Matches the reference UI: sandy beige backgrounds, dark navy CTAs, teal primary

// ─── Backgrounds (warm sand / paper feel)
val KfBgSand          = Color(0xFFEDE8DC)   // main background — warm sand
val KfBgSandDeep      = Color(0xFFE0DAC8)   // slightly darker sand
val KfBgSandMid       = Color(0xFFD8D1BC)   // pressed/active
val KfCard            = Color(0xFFFFFFFF)   // card surface — pure white
val KfCardTinted      = Color(0xFFF8F4ED)   // slightly warm card

// ─── Deep Charcoal Forest (CTAs, bottom nav, primary buttons — near-black, zero blue)
val KfCharcoalForest  = Color(0xFF121714)   // deep charcoal forest near-black
val KfCharcoalLight   = Color(0xFF1D2420)   // slightly lighter charcoal
val KfCharcoalMid     = Color(0xFF28332C)   // charcoal tinted
val KfNavy            = KfCharcoalForest    // alias for compatibility
val KfNavyLight       = KfCharcoalLight
val KfNavyMid         = KfCharcoalMid

// ─── Teal / Forest Green (Speak button, selected states, STOCK)
val KfTealDark        = Color(0xFF1E5C4A)   // dark teal
val KfTeal            = Color(0xFF2D6E5E)   // primary teal — Speak button
val KfTealBright      = Color(0xFF3B8C76)   // lighter teal
val KfTealVivid       = Color(0xFF4CAF91)   // highlight / success
val KfTealGlow        = Color(0xFF66D4B4)   // glow

// ─── Amber / Gold (prices, FLOW items, speaker icon, highlights)
val KfAmberDark       = Color(0xFFC4820A)
val KfAmber           = Color(0xFFE09B1A)   // amber — price highlights
val KfAmberBright     = Color(0xFFF5B731)   // speaker button color
val KfAmberGold       = Color(0xFFFFC942)

// ─── Status & Semantic
val KfOnline          = Color(0xFF4CAF50)   // ONLINE dot
val KfLive            = Color(0xFF4CAF50)   // LIVE indicator
val KfSynced          = Color(0xFF4CAF50)   // SYNCED status
val KfSuccess         = Color(0xFF4CAF50)
val KfOkBadge         = Color(0xFF2E7D32)   // ✓ OK badge text
val KfOkBadgeBg       = Color(0xFFE8F5E9)   // ✓ OK badge background
val KfError           = Color(0xFFE53E3E)   // error / low stock
val KfListening       = Color(0xFF6B7280)   // LISTENING badge bg

// ─── Text
val KfTextDark        = Color(0xFF1A1A2E)   // primary text (near black)
val KfTextMid         = Color(0xFF4A5568)   // secondary text
val KfTextLight       = Color(0xFF8A9199)   // tertiary / captions
val KfTextOnDark      = Color(0xFFFFFFFF)   // text on dark surfaces
val KfTextOnTeal      = Color(0xFFFFFFFF)   // text on teal

// ─── Borders & Dividers
val KfBorderLight     = Color(0xFFD4CEBC)   // subtle card border on sand
val KfBorderMid       = Color(0xFFBFB89F)   // stronger border
val KfBorderTeal      = Color(0xFF2D6E5E)

// ─── STOCK vs FLOW
val KfStockColor      = KfTeal             // STOCK = teal
val KfFlowColor       = KfAmber            // FLOW  = amber
