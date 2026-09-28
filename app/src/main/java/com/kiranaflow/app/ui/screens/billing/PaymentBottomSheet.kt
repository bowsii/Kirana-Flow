package com.kiranaflow.app.ui.screens.billing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kiranaflow.app.data.model.PaymentMode
import com.kiranaflow.app.ui.theme.*

/**
 * Payment Modal — matches Image 1 exactly.
 * Cream background, "Payment Modal" header with back arrow,
 * BILL pill, large ₹ amount with amber speaker, CASH/UPI cards,
 * quick tender chips, PAID button.
 */
@Composable
fun PaymentBottomSheet(
    totalAmount: Double,
    paymentMode: PaymentMode,
    tenderedAmount: String,
    billNumber: String,
    itemCount: Int,
    isProcessing: Boolean,
    onModeChange: (PaymentMode) -> Unit,
    onTenderChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f),
            shape  = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color  = KfBgSand
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header row: back arrow + "Payment Modal" + SYNCED + icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KfBgSand)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = KfTextDark)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Payment\nModal",
                        color = KfTextDark,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.weight(1f))
                    // SYNCED badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(KfSynced))
                        Spacer(Modifier.width(5.dp))
                        Text("SYNCED", color = KfSynced, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(Modifier.width(10.dp))
                    KfTopBarActions()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ── BILL pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color  = KfNavy,
                            shape  = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "BILL $billNumber",
                                color    = Color.White,
                                style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            color  = KfCard,
                            shape  = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, KfBorderLight)
                        ) {
                            Text(
                                "$itemCount items",
                                color    = KfTextMid,
                                style    = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // ── TOTAL PAYABLE AMOUNT
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "TOTAL PAYABLE AMOUNT",
                            color = KfTextLight,
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "₹",
                                color = KfTextDark,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                "${totalAmount.toInt()}",
                                color = KfTextDark,
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 52.sp)
                            )
                            Spacer(Modifier.width(12.dp))
                            // Amber speaker button
                            Surface(
                                color  = KfAmberBright,
                                shape  = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Filled.VolumeUp, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Text(
                            amountInWords(totalAmount),
                            color = KfTextLight,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── SELECT MODE
                    Text(
                        "SELECT MODE",
                        color = KfTextLight,
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PaymentModeCard(
                            icon     = Icons.Filled.CreditCard,
                            label    = "CASH",
                            sublabel = "Banknotes",
                            selected = paymentMode == PaymentMode.CASH,
                            selectedBgColor = KfTeal,
                            modifier = Modifier.weight(1f)
                        ) { onModeChange(PaymentMode.CASH) }

                        PaymentModeCard(
                            icon     = Icons.Outlined.QrCode2,
                            label    = "UPI / QR",
                            sublabel = "Scan & Pay",
                            selected = paymentMode == PaymentMode.UPI,
                            selectedBgColor = KfNavy,
                            modifier = Modifier.weight(1f)
                        ) { onModeChange(PaymentMode.UPI) }
                    }

                    // ── QUICK TENDER & CHANGE HELPER (cash only)
                    if (paymentMode == PaymentMode.CASH) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "QUICK TENDER & CHANGE HELPER",
                                color = KfTextLight,
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp)
                            )
                            Surface(
                                color  = KfCard,
                                shape  = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, KfBorderLight)
                            ) {
                                Text(
                                    "Tap note given",
                                    color    = KfTextMid,
                                    style    = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Tender chips
                        val quickAmounts = buildList {
                            add(totalAmount)
                            val next50 = nextRound(totalAmount, 50)
                            val next100 = nextRound(totalAmount, 100)
                            val next500 = nextRound(totalAmount, 500)
                            if (next50 != totalAmount) add(next50)
                            if (next100 != next50 && next100 != totalAmount) add(next100)
                            if (next500 != next100 && next500 != totalAmount) add(next500)
                        }.take(4)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickAmounts.forEach { amt ->
                                val change = (amt - totalAmount).coerceAtLeast(0.0)
                                TenderChip(
                                    amount  = amt,
                                    change  = change,
                                    isExact = change == 0.0,
                                    onClick = { onTenderChange(amt.toInt().toString()) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Custom amount input
                        val tendered = tenderedAmount.toDoubleOrNull() ?: 0.0
                        val change = (tendered - totalAmount).coerceAtLeast(0.0)

                        OutlinedTextField(
                            value = tenderedAmount,
                            onValueChange = onTenderChange,
                            label = { Text("Customer gave", color = KfTextLight) },
                            prefix = { Text("₹", color = KfTeal, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = KfTeal,
                                unfocusedBorderColor = KfBorderMid,
                                focusedLabelColor    = KfTeal,
                                focusedTextColor     = KfTextDark,
                                unfocusedTextColor   = KfTextDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (tendered > 0 && change > 0) {
                            Surface(
                                color  = KfAmberBright.copy(alpha = 0.12f),
                                shape  = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, KfAmber)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Change to return", color = KfAmberDark, style = MaterialTheme.typography.bodyMedium)
                                    Text("₹${change.toInt()}", color = KfAmberDark, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // ── PAID button
                    Button(
                        onClick  = onConfirm,
                        enabled  = !isProcessing,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = KfTeal,
                            contentColor   = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("PAID  •  ₹${totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ─── Payment Mode Card ────────────────────────────────────────────────────────

@Composable
private fun PaymentModeCard(
    icon: ImageVector,
    label: String,
    sublabel: String,
    selected: Boolean,
    selectedBgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color  = if (selected) selectedBgColor else KfCard,
        shape  = RoundedCornerShape(16.dp),
        border = BorderStroke(if (selected) 0.dp else 1.dp, if (selected) Color.Transparent else KfBorderMid)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selected) {
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f))
                        .align(Alignment.End)
                )
                Spacer(Modifier.height(4.dp))
            }
            Icon(icon, label, tint = if (selected) Color.White else KfTextMid, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = if (selected) Color.White else KfTextDark, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(sublabel, color = if (selected) Color.White.copy(alpha = 0.7f) else KfTextLight, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ─── Tender Chip ─────────────────────────────────────────────────────────────

@Composable
private fun TenderChip(
    amount: Double,
    change: Double,
    isExact: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color  = KfCard,
        shape  = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isExact) KfTeal else KfBorderLight)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "₹${amount.toInt()}",
                color = KfTextDark,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                if (isExact) "Exact" else "Change ₹${change.toInt()}",
                color = if (isExact) KfTeal else KfTextLight,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isExact) FontWeight.SemiBold else FontWeight.Normal)
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun nextRound(amount: Double, step: Int): Double {
    val a = amount.toInt()
    return (((a / step) + 1) * step).toDouble()
}

private fun amountInWords(amount: Double): String {
    val n = amount.toInt()
    if (n == 0) return "Zero Rupees Only"
    val ones = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
    val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")
    fun convert(num: Int): String = when {
        num == 0     -> ""
        num < 20     -> ones[num] + " "
        num < 100    -> tens[num / 10] + " " + convert(num % 10)
        num < 1000   -> ones[num / 100] + " Hundred " + convert(num % 100)
        num < 100000 -> convert(num / 1000) + "Thousand " + convert(num % 1000)
        else         -> convert(num / 100000) + "Lakh " + convert(num % 100000)
    }
    return convert(n).trim().replace("  ", " ") + " Rupees Only"
}
