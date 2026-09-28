package com.kiranaflow.app.ui.screens.speak

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiranaflow.app.ui.screens.billing.KfTopBarActions
import com.kiranaflow.app.ui.screens.billing.KfBottomBar
import com.kiranaflow.app.ui.theme.*

/**
 * Speak / Home Screen — the entry point.
 * Matches Image 2: cream bg, large teal square mic, waveform, SCAN button.
 */
@Composable
fun SpeakScreen(
    isOnline: Boolean = true,
    lastBillNumber: String = "",
    lastBillAmount: String = "",
    onMicTap: () -> Unit,
    onScan: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToPastBills: () -> Unit,
    onNavigateToBilling: () -> Unit
) {
    Scaffold(
        containerColor = KfBgSand,
        topBar = {
            KfSpeakTopBar(isOnline = isOnline)
        },
        bottomBar = {
            KfBottomBar(
                onBill       = { /* already here */ },
                onStock      = onNavigateToStock,
                onPastBills  = onNavigateToPastBills,
                selectedIndex = 0
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KfBgSand),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Large teal rounded-square mic button
            TealSpeakButton(onClick = onMicTap)

            Spacer(Modifier.height(20.dp))

            // ── Waveform animation
            VoiceWaveform()

            Spacer(Modifier.height(12.dp))

            // ── TAP & SPEAK label
            Text(
                text  = "TAP & SPEAK",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = KfTextDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Boliye aur bill banayein",
                style = MaterialTheme.typography.bodyMedium,
                color = KfTextLight
            )

            Spacer(Modifier.height(28.dp))

            // ── Last bill chip
            if (lastBillNumber.isNotBlank()) {
                LastBillChip(billNumber = lastBillNumber, amount = lastBillAmount)
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.weight(1f))

            // ── SCAN BARCODE button
            Button(
                onClick  = onScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KfNavy,
                    contentColor   = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Outlined.QrCodeScanner, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("SCAN", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text("BARCODE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun KfSpeakTopBar(isOnline: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KfBgSand)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ONLINE pill
        Surface(
            color  = KfCard,
            shape  = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, KfBorderLight)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) KfOnline else KfError)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isOnline) "ONLINE" else "OFFLINE",
                    color = if (isOnline) KfOnline else KfError,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        KfTopBarActions()
    }
}

// ─── Teal Speak Button ────────────────────────────────────────────────────────

@Composable
private fun TealSpeakButton(onClick: () -> Unit) {
    // Outer frame (sand-tinted border)
    Box(
        modifier = Modifier
            .size(220.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(KfBgSandDeep)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner teal button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(KfTealBright, KfTeal)
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Speak",
                    tint     = Color.White,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "SPEAK",
                    color  = Color.White.copy(alpha = 0.9f),
                    style  = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }
        }
    }
}

// ─── Voice Waveform ───────────────────────────────────────────────────────────

@Composable
private fun VoiceWaveform() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val bars = 9
    val heights = remember { listOf(18, 30, 22, 42, 28, 38, 20, 32, 16) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(50.dp)
    ) {
        heights.forEachIndexed { i, baseH ->
            val anim by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue  = 1f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(400 + i * 60, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "bar$i"
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((baseH * anim).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(KfTeal.copy(alpha = 0.6f + anim * 0.4f))
            )
        }
    }
}

// ─── Last Bill Chip ───────────────────────────────────────────────────────────

@Composable
private fun LastBillChip(billNumber: String, amount: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        color  = KfCard,
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, KfBorderLight),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon square
            Surface(
                color  = KfBgSandDeep,
                shape  = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Outlined.Receipt, null, tint = KfTextMid, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("LAST BILL", color = KfTextLight, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp))
                Text(
                    "$billNumber  •  ₹$amount",
                    color = KfTextDark,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(Modifier.weight(1f))
            Surface(
                color  = KfNavy,
                shape  = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.VolumeUp, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
