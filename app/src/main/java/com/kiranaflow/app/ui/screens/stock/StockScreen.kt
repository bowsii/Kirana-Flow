package com.kiranaflow.app.ui.screens.stock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranaflow.app.data.model.CatalogItem
import com.kiranaflow.app.data.model.InventoryType
import com.kiranaflow.app.ui.screens.billing.KfBottomBar
import com.kiranaflow.app.ui.screens.billing.KfTopBarActions
import com.kiranaflow.app.ui.theme.*

/**
 * Stock / Dashboard screen.
 * Matches Image 4: SYNCED header, LIVE COUNTER card, TODAY'S GROSS REVENUE,
 * progress bar, Tender Breakdown, Lock Drawer & Close Day, Share via WhatsApp.
 */
@Composable
fun StockScreen(
    viewModel: StockViewModel = hiltViewModel(),
    onNavigateToBilling: () -> Unit,
    onNavigateToPastBills: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showCatalog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = KfBgSand,
        topBar = {
            StockTopBar(onToggleCatalog = { showCatalog = !showCatalog })
        },
        bottomBar = {
            KfBottomBar(
                onBill      = onNavigateToBilling,
                onStock     = { },
                onPastBills = onNavigateToPastBills,
                selectedIndex = 1
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(KfBgSand),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── LIVE COUNTER card
            item { LiveCounterCard() }

            // ── TODAY'S GROSS REVENUE card
            item { RevenueCard(revenue = state.todayRevenue, billCount = state.todayBillCount, dailyGoal = 18000.0) }

            // ── Tender Breakdown
            item { TenderBreakdownCard(cashAmount = state.cashAmount, upiAmount = state.upiAmount) }

            // ── Lock Drawer & Close Day
            item {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KfNavy, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Lock, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lock Drawer & Close Day", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // ── Share via WhatsApp
            item {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KfTextDark),
                    border = BorderStroke(1.dp, KfBorderMid),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Share, null, tint = KfTeal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share Day Summary via WhatsApp", fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Catalog section toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (showCatalog) "CATALOG" else "VIEW CATALOG",
                        color = KfTextMid,
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp)
                    )
                    TextButton(onClick = { showCatalog = !showCatalog }) {
                        Text(if (showCatalog) "Hide ▲" else "Show ▼", color = KfTeal, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (showCatalog) {
                // Search bar
                item {
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = { searchQuery = it; viewModel.search(it) },
                        placeholder   = { Text("Search catalog…", color = KfTextLight) },
                        leadingIcon   = { Icon(Icons.Outlined.Search, null, tint = KfTextLight) },
                        singleLine    = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = KfTeal,
                            unfocusedBorderColor = KfBorderMid,
                            focusedTextColor     = KfTextDark,
                            unfocusedTextColor   = KfTextDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val list = if (searchQuery.isBlank()) state.catalog else state.searchResults
                items(list, key = { it.id }) { item ->
                    StockItemCard(item = item, onEdit = { viewModel.startEdit(it) })
                }
            }
        }
    }

    state.editingItem?.let { item ->
        EditStockDialog(item = item, onSave = viewModel::saveEdit, onDismiss = viewModel::cancelEdit)
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun StockTopBar(onToggleCatalog: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(KfBgSand).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(KfSynced))
            Spacer(Modifier.width(6.dp))
            Text("SYNCED", color = KfSynced, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
        }
        Spacer(Modifier.width(10.dp))
        Text("Stock", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = KfTextDark)
        Spacer(Modifier.weight(1f))
        KfTopBarActions()
    }
}

// ─── LIVE COUNTER card ────────────────────────────────────────────────────────

@Composable
private fun LiveCounterCard() {
    Surface(
        color  = KfCard,
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, KfBorderLight),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(KfOnline))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("LIVE COUNTER  •  SHIFT A", color = KfTextDark, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Text("07:00 AM – Now  (Synced 1m ago)", color = KfTextLight, style = MaterialTheme.typography.bodySmall)
            }
            Surface(
                color  = KfNavy,
                shape  = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Person, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Listen", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

// ─── Revenue Card ─────────────────────────────────────────────────────────────

@Composable
private fun RevenueCard(revenue: Double, billCount: Int, dailyGoal: Double) {
    val progress = (revenue / dailyGoal).coerceIn(0.0, 1.0).toFloat()
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000),
        label = "progress"
    )
    val achieved = (progress * 100).toInt()

    Surface(
        color  = KfCard,
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, KfBorderLight),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TODAY'S GROSS REVENUE", color = KfTextMid, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp))
                Surface(color = KfNavy, shape = RoundedCornerShape(8.dp)) {
                    Text("$billCount Bills Settled", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Text("₹${"%,.0f".format(revenue)}", color = KfTeal, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 40.sp))

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Daily Goal (₹${"%.0f".format(dailyGoal / 1000)}K)", color = KfTextLight, style = MaterialTheme.typography.bodySmall)
                    Text("$achieved% Achieved", color = KfTeal, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }
                LinearProgressIndicator(
                    progress       = { animProgress },
                    modifier       = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color          = KfTeal,
                    trackColor     = KfBgSandDeep,
                    strokeCap      = StrokeCap.Round
                )
            }

            // Voice summary row
            Surface(
                color  = KfBgSandDeep,
                shape  = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.VolumeUp, null, tint = KfTeal, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "\"Gross collection: ${"%.0f".format(revenue)} rupees\"",
                        color = KfTextMid,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(color = KfTeal.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, KfTeal.copy(alpha = 0.4f))) {
                        Text("Ready", color = KfTeal, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

// ─── Tender Breakdown card ────────────────────────────────────────────────────

@Composable
private fun TenderBreakdownCard(cashAmount: Double, upiAmount: Double) {
    Surface(
        color  = KfCard,
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, KfBorderLight),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tender Breakdown", color = KfTextDark, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text("PHYSICAL VS DIGITAL", color = KfTextLight, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp))
            }

            HorizontalDivider(color = KfBorderLight)

            // Cash row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = KfBgSandDeep, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Filled.AccountBalance, null, tint = KfTextMid, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cash in Drawer", color = KfTextDark, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("28 cash handovers", color = KfTextLight, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${"%,.0f".format(cashAmount)}", color = KfTextDark, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Surface(color = KfTeal.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, KfTeal.copy(alpha = 0.3f))) {
                        Text("Count Match ✓", color = KfOkBadge, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            HorizontalDivider(color = KfBorderLight, thickness = 0.5.dp)

            // UPI row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = KfBgSandDeep, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Outlined.QrCode2, null, tint = KfTextMid, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("UPI & Soundbox", color = KfTextDark, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("16 successful scans", color = KfTextLight, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${"%,.0f".format(upiAmount)}", color = KfTextDark, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("Direct Bank Payout", color = KfTextLight, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// ─── Stock Item Card (catalog view) ──────────────────────────────────────────

@Composable
private fun StockItemCard(item: CatalogItem, onEdit: (CatalogItem) -> Unit) {
    val isStock = item.inventoryType == InventoryType.STOCK
    val tagColor = if (isStock) KfStockColor else KfFlowColor
    val isLow = isStock && item.stockBaseUnits <= item.reorderThresholdBaseUnits

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(item) },
        color    = if (isLow) KfError.copy(alpha = 0.05f) else KfCard,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(1.dp, if (isLow) KfError.copy(alpha = 0.3f) else KfBorderLight)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color  = tagColor.copy(alpha = 0.12f),
                shape  = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, tagColor.copy(alpha = 0.4f))
            ) {
                Text(
                    if (isStock) "STOCK" else "FLOW",
                    color    = tagColor,
                    style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = KfTextDark, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${item.price.toFormattedString()} / ${item.unit}", color = KfTextLight, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (isStock) {
                    Text("${item.stockQty.toInt()} left", color = if (isLow) KfError else KfTeal, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("reorder < ${(item.reorderThresholdBaseUnits / item.displayUnit.multiplierToBase.toDouble()).toInt()}", color = KfTextLight, style = MaterialTheme.typography.labelSmall)
                } else {
                    Text("${item.flowSoldToday.toInt()} sold today", color = KfAmber, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    if (item.flowSoldToday > 0) Text("buy ${item.flowSoldToday.toInt()} tomorrow", color = KfTextLight, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Outlined.Edit, "Edit", tint = KfTextLight, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Edit Dialog ──────────────────────────────────────────────────────────────

@Composable
private fun EditStockDialog(item: CatalogItem, onSave: (CatalogItem) -> Unit, onDismiss: () -> Unit) {
    var stockQty by remember { mutableStateOf(item.stockQty.toString()) }
    var price    by remember { mutableStateOf((item.pricePaise / 100.0).toString()) }
    var reorder  by remember { mutableStateOf((item.reorderThresholdBaseUnits / item.displayUnit.multiplierToBase.toDouble()).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = KfCard,
        titleContentColor = KfTextDark,
        title = { Text(item.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KfField("Price (₹)", price) { price = it }
                if (item.inventoryType == InventoryType.STOCK) {
                    KfField("Stock Qty", stockQty) { stockQty = it }
                    KfField("Reorder Threshold", reorder) { reorder = it }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newPricePaise = (price.toDoubleOrNull()?.times(100.0)?.plus(0.5) ?: item.pricePaise.toDouble()).toLong()
                    val newStockBaseUnits = (stockQty.toDoubleOrNull()?.times(item.displayUnit.multiplierToBase)?.plus(0.5) ?: item.stockBaseUnits.toDouble()).toLong()
                    val newReorderBaseUnits = (reorder.toDoubleOrNull()?.times(item.displayUnit.multiplierToBase)?.plus(0.5) ?: item.reorderThresholdBaseUnits.toDouble()).toLong()

                    onSave(
                        item.copy(
                            pricePaise = newPricePaise,
                            stockBaseUnits = newStockBaseUnits,
                            reorderThresholdBaseUnits = newReorderBaseUnits,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                },
                colors  = ButtonDefaults.buttonColors(containerColor = KfTeal, contentColor = Color.White)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = KfTextLight) }
        }
    )
}

@Composable
private fun KfField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label, color = KfTextLight) }, singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KfTeal, unfocusedBorderColor = KfBorderMid, focusedTextColor = KfTextDark, unfocusedTextColor = KfTextDark),
        modifier = Modifier.fillMaxWidth()
    )
}
