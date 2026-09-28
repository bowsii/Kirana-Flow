package com.kiranaflow.app.ui.screens.pastbills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranaflow.app.data.model.Bill
import com.kiranaflow.app.data.model.PaymentMode
import com.kiranaflow.app.ui.screens.billing.KfBottomBar
import com.kiranaflow.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PastBillsScreen(
    viewModel: PastBillsViewModel = hiltViewModel(),
    onNavigateToBilling: () -> Unit,
    onNavigateToStock: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = KfBackgroundDeep,
        topBar = {
            Surface(color = KfBackgroundMid) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Past Bills",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = KfTextPrimary
                    )
                    Spacer(Modifier.weight(1f))

                    // Summary pill
                    Surface(
                        color  = KfSurfaceElevated,
                        shape  = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, KfBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.TrendingUp, null, tint = KfEmeraldBright, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("₹${state.totalRevenue.toInt()} today", color = KfEmeraldBright, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        },
        bottomBar = {
            KfBottomBar(
                onBill      = onNavigateToBilling,
                onStock     = onNavigateToStock,
                onPastBills = {},
                selectedIndex = 2
            )
        }
    ) { padding ->
        if (state.bills.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ReceiptLong, null, tint = KfBorderStrong, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No bills yet", color = KfTextDisabled, style = MaterialTheme.typography.bodyLarge)
                    Text("Bills will appear here after billing", color = KfTextDisabled, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                contentPadding     = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier           = Modifier.fillMaxSize().padding(padding)
            ) {
                item {
                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Bills Today", "${state.bills.size}", KfEmeraldBright, Modifier.weight(1f))
                        StatCard("Revenue", "₹${state.totalRevenue.toInt()}", KfAmberBright, Modifier.weight(1f))
                        StatCard("Avg Bill", "₹${if (state.bills.isEmpty()) 0 else (state.totalRevenue / state.bills.size).toInt()}", KfStock, Modifier.weight(1f))
                    }
                }

                items(state.bills, key = { it.id }) { bill ->
                    BillCard(bill = bill, onClick = { viewModel.selectBill(bill) })
                }
            }
        }
    }

    // Bill detail dialog
    state.selectedBill?.let { bill ->
        BillDetailDialog(
            bill     = bill,
            items    = state.selectedBillItems,
            onDismiss = viewModel::clearSelection
        )
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color    = KfSurface,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(1.dp, KfBorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            Text(label, color = KfTextDisabled, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ─── Bill Card ────────────────────────────────────────────────────────────────

@Composable
private fun BillCard(bill: Bill, onClick: () -> Unit) {
    val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color    = KfSurface,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(1.dp, KfBorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bill number circle
            Surface(
                color  = KfEmeraldDark,
                shape  = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, KfEmerald)
            ) {
                Text(
                    bill.billNumber,
                    color    = KfEmeraldGlow,
                    style    = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${bill.itemCount} items",
                    color    = KfTextPrimary,
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    fmt.format(Date(bill.createdAt)),
                    color = KfTextDisabled,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Payment mode badge
            val (modeColor, modeLabel) = when (bill.paymentMode) {
                PaymentMode.CASH -> KfStock to "CASH"
                PaymentMode.UPI  -> KfAmberBright to "UPI"
                PaymentMode.QR   -> KfAmberGold to "QR"
            }
            Surface(
                color  = modeColor.copy(alpha = 0.12f),
                shape  = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, modeColor.copy(alpha = 0.4f))
            ) {
                Text(
                    modeLabel,
                    color    = modeColor,
                    style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                "₹${bill.totalAmount.toInt()}",
                color = KfTextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
        }
    }
}

// ─── Bill Detail Dialog ───────────────────────────────────────────────────────

@Composable
private fun BillDetailDialog(
    bill: Bill,
    items: List<com.kiranaflow.app.data.model.BillItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = KfSurface,
        titleContentColor = KfTextPrimary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bill.billNumber, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(8.dp))
                Text("• ${bill.itemCount} items", color = KfTextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("${item.quantity.toInt()}× ${item.itemName}", color = KfTextPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("₹${item.lineTotal.toInt()}", color = KfEmeraldBright, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                HorizontalDivider(color = KfBorderSubtle)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("TOTAL", color = KfTextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("₹${bill.totalAmount.toInt()}", color = KfTextPrimary, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                }
                if (bill.tenderedAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Tendered", color = KfTextDisabled, modifier = Modifier.weight(1f))
                        Text("₹${bill.tenderedAmount.toInt()}", color = KfTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (bill.changeAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Change", color = KfAmberBright, modifier = Modifier.weight(1f))
                            Text("₹${bill.changeAmount.toInt()}", color = KfAmberBright, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors  = ButtonDefaults.buttonColors(containerColor = KfEmeraldBright, contentColor = KfBackgroundDeep)
            ) { Text("Close", fontWeight = FontWeight.Bold) }
        }
    )
}
