package com.kiranaflow.feature.pastbills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranaflow.core.model.Bill
import com.kiranaflow.core.model.BillItem
import com.kiranaflow.core.model.PaymentMode
import com.kiranaflow.core.ui.component.KfBottomBar
import com.kiranaflow.core.ui.component.KfTopBarActions
import com.kiranaflow.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PastBillsScreen(
    viewModel: PastBillsViewModel = hiltViewModel(),
    onNavigateToBilling: () -> Unit,
    onNavigateToStock: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = KfBgSand,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(KfBgSand)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(KfSuccess))
                        Spacer(Modifier.width(6.dp))
                        Text("OFFLINE READY", color = KfSuccess, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = KfTextDark)
                    Spacer(Modifier.weight(1f))
                    KfTopBarActions()
                }
                HorizontalDivider(color = KfBorderLight)
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
                    Icon(Icons.AutoMirrored.Outlined.ReceiptLong, null, tint = KfBorderMid, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(14.dp))
                    Text("No bills yet", color = KfTextMid, style = MaterialTheme.typography.titleSmall)
                    Text("Bills will appear here after billing", color = KfTextLight, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                // Stats row
                item {
                    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PastBillStatCard("Bills", "${state.bills.size}", Modifier.weight(1f))
                        PastBillStatCard("Revenue", "₹${state.totalRevenue.toInt()}", Modifier.weight(1f), KfTeal)
                        PastBillStatCard("Avg", "₹${if (state.bills.isEmpty()) 0 else (state.totalRevenue / state.bills.size).toInt()}", Modifier.weight(1f))
                    }
                }

                items(state.bills, key = { it.id }) { bill ->
                    PastBillCard(bill = bill) { viewModel.selectBill(bill) }
                }
            }
        }
    }

    state.selectedBill?.let { bill ->
        BillDetailDialog(bill = bill, items = state.selectedBillItems, onDismiss = viewModel::clearSelection)
    }
}

@Composable
private fun PastBillStatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = KfTextDark) {
    Surface(modifier = modifier, color = KfCard, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, KfBorderLight)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = valueColor, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            Text(label, color = KfTextLight, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PastBillCard(bill: Bill, onClick: () -> Unit) {
    val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val (modeIcon, modeLabel, modeColor) = when (bill.paymentMode) {
        PaymentMode.CASH -> Triple(Icons.Filled.Payments, "CASH", KfTeal)
        PaymentMode.UPI  -> Triple(Icons.Filled.QrCode2, "UPI", KfAmber)
        PaymentMode.QR   -> Triple(Icons.Filled.QrCode2, "QR", KfAmberDark)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color    = KfCard,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(1.dp, KfBorderLight)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = KfNavy, shape = RoundedCornerShape(8.dp)) {
                Text(
                    bill.billNumber,
                    color    = Color.White,
                    style    = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${bill.itemCount} items", color = KfTextDark, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(fmt.format(Date(bill.createdAt)), color = KfTextLight, style = MaterialTheme.typography.bodySmall)
            }
            Surface(color = modeColor.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, modeColor.copy(alpha = 0.4f))) {
                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(modeIcon, modeLabel, tint = modeColor, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(modeLabel, color = modeColor, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
            Spacer(Modifier.width(10.dp))
            Text("₹${bill.totalAmount.toInt()}", color = KfTextDark, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
        }
    }
}

@Composable
private fun BillDetailDialog(bill: Bill, items: List<BillItem>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = KfCard,
        titleContentColor = KfTextDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bill.billNumber, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(8.dp))
                Text("• ${bill.itemCount} items", color = KfTextLight, style = MaterialTheme.typography.bodyMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("${item.quantity.toInt()}× ${item.itemName}", color = KfTextDark, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("₹${item.lineTotal.toInt()}", color = KfAmber, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                HorizontalDivider(color = KfBorderLight)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("TOTAL", color = KfTextMid, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("₹${bill.totalAmount.toInt()}", color = KfTextDark, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                }
                if (bill.changeAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Change returned", color = KfTextLight, modifier = Modifier.weight(1f))
                        Text("₹${bill.changeAmount.toInt()}", color = KfAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = KfNavy, contentColor = Color.White)) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}
