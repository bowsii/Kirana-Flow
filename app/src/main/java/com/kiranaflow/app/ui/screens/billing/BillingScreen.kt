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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranaflow.core.model.CartLine
import com.kiranaflow.core.model.InventoryType
import com.kiranaflow.app.ui.theme.*

/**
 * Billing screen — active bill list.
 * Matches Image 3: "Bill #1049 | LIVE", white item cards,
 * dark qty badges, ✓OK chip, listening banner, dark DONE button.
 */
@Composable
fun BillingScreen(
    viewModel: BillingViewModel = hiltViewModel(),
    onNavigateToStock: () -> Unit,
    onNavigateToPastBills: () -> Unit,
    onNavigateToSpeak: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.showPaymentSheet) {
        PaymentBottomSheet(
            totalAmount    = state.cart.totalAmount,
            paymentMode    = state.paymentMode,
            tenderedAmount = state.tenderedAmount,
            billNumber     = "#${(System.currentTimeMillis() % 10000).toInt()}",
            itemCount      = state.cart.itemCount,
            isProcessing   = state.isCommitting,
            onModeChange   = viewModel::setPaymentMode,
            onTenderChange = viewModel::setTenderedAmount,
            onConfirm      = viewModel::confirmPayment,
            onDismiss      = viewModel::dismissPaymentSheet
        )
    }

    Scaffold(
        containerColor = KfBgSand,
        topBar = {
            BillingTopBar(
                billNumber  = "#1049",
                itemCount   = state.cart.itemCount,
                isListening = state.isListening
            )
        },
        bottomBar = {
            KfBottomBar(
                onBill      = onNavigateToSpeak,
                onStock     = onNavigateToStock,
                onPastBills = onNavigateToPastBills,
                selectedIndex = 0
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KfBgSand)
        ) {
            // ── Cart list
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.cart.lines.isEmpty()) {
                    EmptyCartPlaceholder()
                } else {
                    LazyColumn(
                        contentPadding     = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(state.cart.lines, key = { _, l -> l.catalogItemId }) { idx, line ->
                            BillingCartCard(
                                line    = line,
                                onInc   = { viewModel.incrementItem(idx) },
                                onDec   = { viewModel.decrementItem(idx) }
                            )
                        }

                        // ── Listening chip (like "And 2 Maggi..." in the design)
                        if (state.isListening || state.voiceText.isNotBlank()) {
                            item {
                                ListeningChip(voiceText = state.voiceText.ifBlank { "Listening…" })
                            }
                        }
                    }
                }

                // Error snackbar
                state.lastError?.let { err ->
                    Surface(
                        modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                        color    = KfError.copy(alpha = 0.93f),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Warning, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = viewModel::clearError, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Bill saved overlay
                if (state.commitSuccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        BillSavedOverlay { viewModel.acknowledgeSuccess() }
                    }
                }
            }

            // ── Cart footer + DONE button
            BillingFooter(
                total     = state.cart.totalAmount,
                itemCount = state.cart.itemCount,
                onDone    = viewModel::showPaymentSheet,
                onClear   = viewModel::clearCart
            )
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
fun BillingTopBar(
    billNumber: String,
    itemCount: Int,
    isListening: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KfBgSand)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // OFFLINE READY status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(KfSuccess))
                Spacer(Modifier.width(6.dp))
                Text("OFFLINE READY", color = KfSuccess, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
            }
            Spacer(Modifier.width(10.dp))
            Text("Billing", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = KfTextDark)
            Spacer(Modifier.weight(1f))
            KfTopBarActions()
        }

        // Bill pill row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bill number pill
            Surface(
                color  = KfCard,
                shape  = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, KfBorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(KfTeal))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Bill $billNumber",
                        color = KfTextDark,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // LIVE badge
            Surface(
                color  = KfLive.copy(alpha = 0.12f),
                shape  = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, KfLive.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LIVE", color = KfLive, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp))
                    Spacer(Modifier.width(4.dp))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(KfLive))
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Counter 1  •  $itemCount items verified",
                color = KfTextLight,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HorizontalDivider(color = KfBorderLight)
    }
}

// ─── Top Bar Action buttons (speaker + profile) ───────────────────────────────

@Composable
fun KfTopBarActions() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            color  = KfNavy,
            shape  = RoundedCornerShape(10.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Filled.VolumeUp, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Surface(
            color  = KfNavy,
            shape  = RoundedCornerShape(10.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─── Billing Cart Card — matches design: dark qty badge, ✓OK, amber price ─────

@Composable
private fun BillingCartCard(
    line: CartLine,
    onInc: () -> Unit,
    onDec: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = KfCard,
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.dp, KfBorderLight),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dark qty badge
            Surface(
                color  = KfNavy,
                shape  = RoundedCornerShape(10.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        "${line.quantity.toInt()}×",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Item info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        line.itemName,
                        color    = KfTextDark,
                        style    = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 110.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    // ✓ OK badge
                    Surface(
                        color  = KfOkBadgeBg,
                        shape  = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "✓ OK",
                            color    = KfOkBadge,
                            style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    "${line.quantity.toInt()} ${line.unit}  •  ₹${line.pricePerUnit.toInt()} each",
                    color = KfTextLight,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(8.dp))

            // Price
            Text(
                "₹${line.lineTotal.toInt()}",
                color = KfAmber,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            )

            Spacer(Modifier.width(8.dp))

            // Stepper (– and +)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                CartStepperBtn(icon = Icons.Filled.Remove, onClick = onDec)
                CartStepperBtn(icon = Icons.Filled.Add,    onClick = onInc, filled = true)
            }
        }
    }
}

@Composable
private fun CartStepperBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    filled: Boolean = false
) {
    Surface(
        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).clickable(onClick = onClick),
        color    = if (filled) KfBgSandDeep else KfCard,
        shape    = RoundedCornerShape(6.dp),
        border   = BorderStroke(1.dp, KfBorderLight)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(icon, null, tint = KfTextDark, modifier = Modifier.size(14.dp))
        }
    }
}

// ─── Listening chip — "And 2 Maggi..." | LISTENING ───────────────────────────

@Composable
private fun ListeningChip(voiceText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "listenWave")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = KfCard,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(1.dp, KfBorderLight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // mini waveform
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(10, 18, 12, 22, 14).forEachIndexed { i, h ->
                    val scale by infiniteTransition.animateFloat(
                        0.4f, 1f, infiniteRepeatable(tween(350 + i * 70), RepeatMode.Reverse), label = "w$i"
                    )
                    Box(modifier = Modifier.width(3.dp).height((h * scale).dp).clip(RoundedCornerShape(2.dp)).background(KfTeal.copy(alpha = 0.7f)))
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "\"${voiceText.take(28)}${if (voiceText.length > 28) "..." else ""}\"",
                color = KfTextDark,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                color  = KfNavy,
                shape  = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "LISTENING",
                    color    = Color.White,
                    style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
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
        Icon(Icons.Outlined.ShoppingBag, null, tint = KfBorderMid, modifier = Modifier.size(60.dp))
        Spacer(Modifier.height(14.dp))
        Text("Cart is empty", color = KfTextMid, style = MaterialTheme.typography.titleSmall)
        Text("Go back and speak an item", color = KfTextLight, style = MaterialTheme.typography.bodySmall)
    }
}

// ─── Cart Footer ─────────────────────────────────────────────────────────────

@Composable
private fun BillingFooter(
    total: Double,
    itemCount: Int,
    onDone: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KfBgSand)
            .padding(horizontal = 16.dp)
    ) {
        HorizontalDivider(color = KfBorderLight)
        Spacer(Modifier.height(10.dp))

        // Items count + Total row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.ShoppingBag, null, tint = KfTextMid, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "$itemCount Items",
                color = KfTextMid,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.weight(1f))
            Text("Total: ", color = KfTextMid, style = MaterialTheme.typography.bodyMedium)
            Text(
                "₹${total.toInt()}",
                color = KfTextDark,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
        }

        Spacer(Modifier.height(10.dp))

        // DONE button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DONE main button
            Button(
                onClick  = onDone,
                enabled  = itemCount > 0,
                modifier = Modifier.weight(1f).height(54.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = KfNavy,
                    contentColor           = Color.White,
                    disabledContainerColor = KfBgSandDeep
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, null, tint = KfTealVivid, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("DONE", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }

            // Speaker mini button
            Surface(
                color    = KfAmber.copy(alpha = 0.15f),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(1.dp, KfAmber.copy(alpha = 0.4f)),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.VolumeUp, null, tint = KfAmber, modifier = Modifier.size(22.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
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
        color  = KfCard,
        shape  = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, KfTeal),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.CheckCircle, null, tint = KfTeal, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(12.dp))
            Text("Bill Saved!", color = KfTextDark, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text("Ready for next customer", color = KfTextLight, style = MaterialTheme.typography.bodySmall)
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
    Surface(color = KfNavy, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(68.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KfNavItem(
                unselected = Icons.Outlined.Mic,
                selected   = Icons.Filled.Mic,
                label      = "Bill",
                isSelected = selectedIndex == 0,
                isMic      = true,
                onClick    = onBill
            )
            KfNavItem(
                unselected = Icons.Outlined.Inventory2,
                selected   = Icons.Filled.Inventory2,
                label      = "Stock",
                isSelected = selectedIndex == 1,
                onClick    = onStock
            )
            KfNavItem(
                unselected = Icons.Outlined.History,
                selected   = Icons.Filled.History,
                label      = "Past Bills",
                isSelected = selectedIndex == 2,
                onClick    = onPastBills
            )
        }
    }
}

@Composable
private fun KfNavItem(
    unselected: androidx.compose.ui.graphics.vector.ImageVector,
    selected: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    isMic: Boolean = false,
    onClick: () -> Unit
) {
    if (isMic && isSelected) {
        // Big pill for selected mic
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.Transparent
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
                modifier = Modifier
                    .background(KfNavyLight, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(selected, label, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    } else {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (isSelected) selected else unselected,
                label,
                tint     = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
