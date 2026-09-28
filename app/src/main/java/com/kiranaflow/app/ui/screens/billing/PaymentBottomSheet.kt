package com.kiranaflow.app.ui.screens.billing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.kiranaflow.app.data.model.PaymentMode
import com.kiranaflow.app.ui.theme.*

/**
 * Payment modal matching the reference design:
 *   - Total amount displayed prominently
 *   - Cash / UPI+QR payment mode selector
 *   - Quick tender helper (exact, change amounts)
 *   - Confirm / Paid button
 */
@Composable
fun PaymentBottomSheet(
    totalAmount: Double,
    paymentMode: PaymentMode,
    tenderedAmount: String,
    isProcessing: Boolean,
    onModeChange: (PaymentMode) -> Unit,
    onTenderChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape  = RoundedCornerShape(24.dp),
            color  = KfSurface,
            border = BorderStroke(1.dp, KfBorderStrong)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Payment Modal",
                        color = KfTextSecondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, "Close", tint = KfTextDisabled, modifier = Modifier.size(18.dp))
                    }
                }

                // ── Total Amount
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "TOTAL PAYABLE AMOUNT",
                        color = KfTextSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "₹${totalAmount.toInt()}",
                        color  = KfTextPrimary,
                        style  = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black)
                    )
                    // Amount in words
                    Text(
                        amountInWords(totalAmount),
                        color = KfTextDisabled,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(color = KfBorderSubtle)

                // ── SELECT MODE
                Text("SELECT MODE", color = KfTextSecondary, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentModeCard(
                        icon     = Icons.Filled.Payments,
                        label    = "CASH",
                        sublabel = "Banknotes",
                        selected = paymentMode == PaymentMode.CASH,
                        color    = KfEmeraldBright,
                        modifier = Modifier.weight(1f),
                        onClick  = { onModeChange(PaymentMode.CASH) }
                    )
                    PaymentModeCard(
                        icon     = Icons.Filled.QrCode2,
                        label    = "UPI / QR",
                        sublabel = "Scan & Pay",
                        selected = paymentMode == PaymentMode.UPI,
                        color    = KfAmberBright,
                        modifier = Modifier.weight(1f),
                        onClick  = { onModeChange(PaymentMode.UPI) }
                    )
                }

                // ── Quick Tender (cash mode only)
                if (paymentMode == PaymentMode.CASH) {
                    Text("QUICK TENDER & CHANGE HELPER", color = KfTextSecondary, style = MaterialTheme.typography.labelMedium, letterSpacing = 0.5.sp)

                    // Tender input
                    OutlinedTextField(
                        value         = tenderedAmount,
                        onValueChange = onTenderChange,
                        label         = { Text("Amount given by customer") },
                        prefix        = { Text("₹", color = KfEmeraldBright, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine    = true,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = KfEmeraldBright,
                            unfocusedBorderColor = KfBorderSubtle,
                            focusedLabelColor    = KfEmeraldBright,
                            unfocusedLabelColor  = KfTextDisabled,
                            focusedTextColor     = KfTextPrimary,
                            unfocusedTextColor   = KfTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick amounts row
                    val quickAmounts = listOf(
                        totalAmount,
                        nextRound(totalAmount, 50),
                        nextRound(totalAmount, 100),
                        nextRound(totalAmount, 500)
                    ).distinct().take(4)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.forEach { amt ->
                            val change = (amt - totalAmount).coerceAtLeast(0.0)
                            QuickTenderChip(
                                amount  = amt,
                                change  = change,
                                isExact = change == 0.0,
                                onClick = { onTenderChange(amt.toInt().toString()) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Change display
                    val tendered = tenderedAmount.toDoubleOrNull() ?: 0.0
                    val change = (tendered - totalAmount).coerceAtLeast(0.0)
                    if (tendered > 0 && change > 0) {
                        Surface(
                            color  = KfAmberDark,
                            shape  = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KfAmberBright)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Change to return", color = KfAmberGold, style = MaterialTheme.typography.bodyMedium)
                                Text("₹${change.toInt()}", color = KfAmberGold, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                // ── Confirm button
                Button(
                    onClick  = onConfirm,
                    enabled  = !isProcessing,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = KfEmeraldBright,
                        contentColor   = KfBackgroundDeep
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(22.dp),
                            color     = KfBackgroundDeep,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "PAID • ₹${totalAmount.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 16.sp
                        )
                    }
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
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color  = if (selected) color.copy(alpha = 0.15f) else KfSurfaceElevated,
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) color else KfBorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, label, tint = if (selected) color else KfTextDisabled, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = if (selected) color else KfTextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(sublabel, color = KfTextDisabled, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ─── Quick Tender Chip ────────────────────────────────────────────────────────

@Composable
private fun QuickTenderChip(
    amount: Double,
    change: Double,
    isExact: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color  = if (isExact) KfEmerald.copy(alpha = 0.2f) else KfSurfaceElevated,
        shape  = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isExact) KfEmeraldBright else KfBorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "₹${amount.toInt()}",
                color = if (isExact) KfEmeraldGlow else KfTextPrimary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            if (isExact) {
                Text("Exact", color = KfEmeraldVivid, style = MaterialTheme.typography.labelSmall)
            } else {
                Text("Change ₹${change.toInt()}", color = KfTextDisabled, style = MaterialTheme.typography.labelSmall)
            }
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
        num == 0    -> ""
        num < 20    -> ones[num] + " "
        num < 100   -> tens[num / 10] + " " + convert(num % 10)
        num < 1000  -> ones[num / 100] + " Hundred " + convert(num % 100)
        num < 100000 -> convert(num / 1000) + "Thousand " + convert(num % 1000)
        else        -> convert(num / 100000) + "Lakh " + convert(num % 100000)
    }
    return convert(n).trim() + " Rupees Only"
}
