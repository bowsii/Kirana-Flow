package com.kiranaflow.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── KiranaFlow Design Tokens ─────────────────────────────────────────────────
// Color philosophy: Dark charcoal base + Emerald green accents + Amber/Gold highlights
// NO violet, indigo, or blue.

// ─── Background & Surface
val KfBackgroundDeep   = Color(0xFF0D1008)   // near-black green-tinted
val KfBackgroundMid    = Color(0xFF151A10)   // dark surface
val KfSurface          = Color(0xFF1E2518)   // card surface
val KfSurfaceElevated  = Color(0xFF253020)   // elevated card

// ─── Emerald Primary
val KfEmeraldDark      = Color(0xFF1A4D2E)
val KfEmerald          = Color(0xFF2D6A4F)
val KfEmeraldBright    = Color(0xFF40C074)   // primary CTA
val KfEmeraldVivid     = Color(0xFF52D68A)   // highlight / live
val KfEmeraldGlow      = Color(0xFF78E8A2)   // glow / success

// ─── Amber / Gold Secondary
val KfAmberDark        = Color(0xFF7C4A00)
val KfAmber            = Color(0xFFB86B00)
val KfAmberBright      = Color(0xFFFFAA00)   // secondary CTA / warning
val KfAmberGold        = Color(0xFFFFCC44)   // premium / highlight
val KfAmberGlow        = Color(0xFFFFE08A)   // soft glow

// ─── Semantic
val KfSuccess          = Color(0xFF52D68A)
val KfError            = Color(0xFFFF5252)
val KfWarning          = Color(0xFFFFAA00)
val KfOnline           = Color(0xFF52D68A)
val KfOffline          = Color(0xFFFF5252)

// ─── Text
val KfTextPrimary      = Color(0xFFF5F9F0)   // near-white, warm
val KfTextSecondary    = Color(0xFFAFC9A0)   // muted green-grey
val KfTextDisabled     = Color(0xFF5A6E50)

// ─── Border & Divider
val KfBorderSubtle     = Color(0xFF2A3524)
val KfBorderStrong     = Color(0xFF3D5230)

// ─── Stock / Flow Tags
val KfStock            = Color(0xFF40C074)   // STOCK = emerald
val KfFlow             = Color(0xFFFFAA00)   // FLOW  = amber
