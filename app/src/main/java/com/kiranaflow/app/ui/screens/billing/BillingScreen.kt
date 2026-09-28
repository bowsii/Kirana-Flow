package com.kiranaflow.app.ui.screens.billing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranaflow.app.data.model.CartLine
import com.kiranaflow.app.data.model.InventoryType
import com.kiranaflow.app.ui.theme.*

@Composable
fun BillingScreen(
    viewModel: BillingViewModel = hiltViewModel(),
    onNavigateToStock: () -> Unit,
    onNavigateToPastBills: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Payment bottom sheet
    if (state.showPaymentSheet) {
        PaymentBottomSheet(
            totalAmount    = state.cart.totalAmount,
            paymentMode    = state.paymentMode,
            tenderedAmount = state.tenderedAmount,
            isProcessing   = state.isCommitting,
            onModeChange   = viewModel::setPaymentMode,
            onTenderChange = viewModel::setTenderedAmount,
            onConfirm      = viewModel::confirmPayment,
            onDismiss      = viewModel::dismissPaymentSheet
        )
    }

    Scaffold(
        containerColor = KfBackgroundDeep,
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
                .background(KfBackgroundDeep)
        ) {
            // ── Top status bar
            BillingTopBar(
                billCount   = state.cart.itemCount,
                isListening = state.isListening
            )

            // ── Voice waveform + button (top section)
            VoiceInputSection(
                isListening = state.isListening,
                voiceText   = state.voiceText,
                onToggle    = viewModel::toggleListening
            )

            // ── Cart list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.cart.lines.isEmpty()) {
                    EmptyCartPlaceholder()
                } else {
                    CartList(
                        lines     = state.cart.lines,
                        onInc     = viewModel::incrementItem,
                        onDec     = viewModel::decrementItem,
                        onRemove  = viewModel::removeItem
                    )
                }

                // Error snackbar overlay
                state.lastError?.let { err ->
                    ErrorBanner(
                        message  = err,
                        onDismiss = viewModel::clearError,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                // Success overlay
                AnimatedVisibility(
                    visible = state.commitSuccess,
                    enter   = fadeIn() + scaleIn(),
                    exit    = fadeOut() + scaleOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    BillSavedOverlay { viewModel.acknowledgeSuccess() }
                }
            }

            // ── Cart footer
            CartFooter(
                total      = state.cart.totalAmount,
                itemCount  = state.cart.itemCount,
                onDone     = viewModel::showPaymentSheet,
                onClear    = viewModel::clearCart
            )
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun BillingTopBar(billCount: Int, isListening: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KfBackgroundMid)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status dot
        val dotColor = if (isListening) KfEmeraldVivid else KfOnline
        val dotScale by animateFloatAsState(
            targetValue = if (isListening) 1.3f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "dot"
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(dotScale)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = if (isListening) "LISTENING" else "ONLINE",
            color = dotColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            letterSpacing = 1.sp
        )
        Spacer(Modifier.weight(1f))
        Text(
            text  = "Billing",
            color = KfTextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.weight(1f))
        // Bill count badge
        if (billCount > 0) {
            Surface(
                color  = KfSurfaceElevated,
                shape  = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text     = "$billCount items",
                    color    = KfTextSecondary,
                    style    = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─── Voice Input Section ──────────────────────────────────────────────────────

@Composable
private fun VoiceInputSection(
    isListening: Boolean,
    voiceText: String,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue  = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(KfBackgroundMid, KfBackgroundDeep)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring when listening
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(KfEmeraldBright.copy(alpha = 0.12f))
            )
        }

        // Main mic button
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    if (isListening)
                        Brush.radialGradient(listOf(KfEmeraldBright, KfEmerald))
                    else
                        Brush.radialGradient(listOf(KfSurfaceElevated, KfSurface))
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector  = if (isListening) Icons.Filled.Mic else Icons.Outlined.MicNone,
                contentDescription = "Tap to speak",
                tint   = if (isListening) KfBackgroundDeep else KfEmeraldBright,
                modifier = Modifier.size(40.dp)
            )
        }

        // Voice text caption
        if (voiceText.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .widthIn(max = 280.dp),
                color = KfSurfaceElevated,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text     = "\"$voiceText\"",
                    color    = KfAmberBright,
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        } else {
            Text(
                text     = if (isListening) "LISTENING" else "TAP & SPEAK\nBoliye aur bill banayein",
                color    = if (isListening) KfEmeraldGlow else KfTextDisabled,
                style    = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
            )
        }
    }
}

// ─── Cart List ────────────────────────────────────────────────────────────────

@Composable
private fun CartList(
    lines: List<CartLine>,
    onInc: (Int) -> Unit,
    onDec: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    LazyColumn(
        contentPadding     = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(lines, key = { _, l -> l.catalogItemId }) { idx, line ->
            CartLineCard(line = line, onInc = { onInc(idx) }, onDec = { onDec(idx) }, onRemove = { onRemove(idx) })
        }
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier      = Modifier.fillMaxWidth(),
        color         = KfSurface,
        shape         = RoundedCornerShape(12.dp),
        border        = BorderStroke(1.dp, KfBorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Inventory type dot
            val dotColor = if (line.inventoryType == InventoryType.STOCK) KfStock else KfFlow
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(10.dp))

            // Item name + unit
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = line.itemName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = KfTextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text  = "₹${line.pricePerUnit.toInt()} / ${line.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KfTextSecondary
                )
            }

            // Quantity stepper
            QuantityStepper(qty = line.quantity, unit = line.unit, onInc = onInc, onDec = onDec)

            Spacer(Modifier.width(10.dp))

            // Line total
            Text(
                text  = "₹${line.lineTotal.toInt()}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = KfEmeraldBright
            )

            Spacer(Modifier.width(6.dp))

            // Remove
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, "Remove", tint = KfTextDisabled, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun QuantityStepper(qty: Double, unit: String, onInc: () -> Unit, onDec: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick  = onDec,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(KfSurfaceElevated)
        ) {
            Icon(Icons.Filled.Remove, "-", tint = KfTextPrimary, modifier = Modifier.size(14.dp))
        }

        Text(
            text  = if (qty == qty.toLong().toDouble()) "${qty.toLong()}" else "$qty",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = KfTextPrimary,
            modifier = Modifier.widthIn(min = 24.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick  = onInc,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(KfEmerald)
        ) {
            Icon(Icons.Filled.Add, "+", tint = KfTextPrimary, modifier = Modifier.size(14.dp))
        }
    }
}

// ─── Empty Cart ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyCartPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = null,
            tint     = KfBorderStrong,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("Speak an item to start billing", color = KfTextDisabled, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text("e.g. \"Rendu Parle-G\"", color = KfAmberBright.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
    }
}

// ─── Cart Footer ─────────────────────────────────────────────────────────────

@Composable
private fun CartFooter(
    total: Double,
    itemCount: Int,
    onDone: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color  = KfSurface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text  = "$itemCount Items",
                    style = MaterialTheme.typography.labelMedium,
                    color = KfTextSecondary
                )
                Text(
                    text  = "Total: ₹${total.toInt()}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = KfTextPrimary
                )
            }

            Spacer(Modifier.weight(1f))

            if (itemCount > 0) {
                TextButton(onClick = onClear) {
                    Text("Clear", color = KfError)
                }
                Spacer(Modifier.width(8.dp))
            }

            Button(
                onClick  = onDone,
                enabled  = itemCount > 0,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = KfEmeraldBright,
                    contentColor   = KfBackgroundDeep,
                    disabledContainerColor = KfBorderSubtle
                ),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("DONE", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

// ─── Error Banner ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(16.dp),
        color    = KfError.copy(alpha = 0.92f),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Warning, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(message, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Close, "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─── Bill Saved Overlay ───────────────────────────────────────────────────────

@Composable
private fun BillSavedOverlay(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onDismiss()
    }
    Surface(
        color  = KfEmeraldDark.copy(alpha = 0.95f),
        shape  = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, KfEmeraldBright)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                "Bill saved",
                tint     = KfEmeraldGlow,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("Bill Saved!", color = KfTextPrimary, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text("Ready for next customer", color = KfTextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
fun KfBottomBar(
    onBill: () -> Unit,
    onStock: () -> Unit,
    onPastBills: () -> Unit,
    selectedIndex: Int
) {
    Surface(
        color         = KfSurface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KfNavItem(Icons.Outlined.Receipt, Icons.Filled.Receipt,       "Bill",      selectedIndex == 0, onBill)
            KfNavItem(Icons.Outlined.Inventory2, Icons.Filled.Inventory2, "Stock",     selectedIndex == 1, onStock)
            KfNavItem(Icons.Outlined.History, Icons.Filled.History,       "Past Bills", selectedIndex == 2, onPastBills)
        }
    }
}

@Composable
private fun KfNavItem(
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint     = if (selected) KfEmeraldBright else KfTextDisabled,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = label,
            color = if (selected) KfEmeraldBright else KfTextDisabled,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
