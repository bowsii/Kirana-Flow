package com.kiranaflow.app.ui.screens.stock

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiranaflow.app.data.model.CatalogItem
import com.kiranaflow.app.data.model.InventoryType
import com.kiranaflow.app.ui.screens.billing.KfBottomBar
import com.kiranaflow.app.ui.theme.*

@Composable
fun StockScreen(
    viewModel: StockViewModel = hiltViewModel(),
    onNavigateToBilling: () -> Unit,
    onNavigateToPastBills: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = KfBackgroundDeep,
        topBar = {
            Surface(color = KfBackgroundMid) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stock", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = KfTextPrimary)
                        Spacer(Modifier.weight(1f))
                        // Low stock badge
                        val lowCount = state.lowStockItems.size
                        if (lowCount > 0) {
                            Surface(color = KfError.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, KfError)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Warning, null, tint = KfError, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("$lowCount Low", color = KfError, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, "Add item", tint = KfEmeraldBright)
                        }
                    }

                    // Search
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.search(it)
                        },
                        placeholder   = { Text("Search catalog…", color = KfTextDisabled) },
                        leadingIcon   = { Icon(Icons.Outlined.Search, null, tint = KfTextDisabled) },
                        singleLine    = true,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = KfEmeraldBright,
                            unfocusedBorderColor = KfBorderSubtle,
                            focusedTextColor     = KfTextPrimary,
                            unfocusedTextColor   = KfTextPrimary
                        ),
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick            = { showAddDialog = true },
                containerColor     = KfEmeraldBright,
                contentColor       = KfBackgroundDeep,
                shape              = CircleShape
            ) {
                Icon(Icons.Filled.Add, "Add item", modifier = Modifier.size(24.dp))
            }
        },
        bottomBar = {
            KfBottomBar(
                onBill      = onNavigateToBilling,
                onStock     = {},
                onPastBills = onNavigateToPastBills,
                selectedIndex = 1
            )
        }
    ) { padding ->
        val displayList = if (searchQuery.isBlank()) state.catalog else state.searchResults

        LazyColumn(
            contentPadding     = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier           = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── Section: Low Stock Alert
            if (state.lowStockItems.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    Text(
                        "⚠️  LOW STOCK — REORDER NOW",
                        color = KfError,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(state.lowStockItems) { item ->
                    StockItemCard(item = item, isLow = true, onEdit = { viewModel.startEdit(it) })
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // ── Section: Full Catalog
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "ALL ITEMS  •  ${displayList.size}",
                        color = KfTextSecondary,
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            items(displayList, key = { it.id }) { item ->
                StockItemCard(item = item, isLow = false, onEdit = { viewModel.startEdit(it) })
            }
        }
    }

    // Edit dialog
    state.editingItem?.let { item ->
        EditStockDialog(
            item     = item,
            onSave   = viewModel::saveEdit,
            onDismiss = viewModel::cancelEdit
        )
    }
}

// ─── Stock Item Card ───────────────────────────────────────────────────────────

@Composable
private fun StockItemCard(item: CatalogItem, isLow: Boolean, onEdit: (CatalogItem) -> Unit) {
    val isStock = item.inventoryType == InventoryType.STOCK
    val tagColor = if (isStock) KfStock else KfFlow

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(item) },
        color    = if (isLow) KfError.copy(alpha = 0.06f) else KfSurface,
        shape    = RoundedCornerShape(12.dp),
        border   = BorderStroke(1.dp, if (isLow) KfError.copy(alpha = 0.4f) else KfBorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type badge
            Surface(
                color  = tagColor.copy(alpha = 0.15f),
                shape  = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, tagColor.copy(alpha = 0.5f))
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
                Text(
                    item.name,
                    color    = KfTextPrimary,
                    style    = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "₹${item.price} / ${item.unit}",
                    color = KfTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Quantity / sold indicator
            Column(horizontalAlignment = Alignment.End) {
                if (isStock) {
                    Text(
                        "${item.stockQty.toInt()} left",
                        color = if (item.stockQty <= item.reorderThreshold) KfError else KfEmeraldBright,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text("reorder < ${item.reorderThreshold.toInt()}", color = KfTextDisabled, style = MaterialTheme.typography.labelSmall)
                } else {
                    Text(
                        "${item.flowSoldToday.toInt()} sold today",
                        color = KfAmberBright,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (item.flowSoldToday > 0) {
                        Text("buy ${item.flowSoldToday.toInt()} tomorrow", color = KfTextDisabled, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.width(6.dp))
            Icon(Icons.Outlined.Edit, "Edit", tint = KfTextDisabled, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Edit Dialog ──────────────────────────────────────────────────────────────

@Composable
private fun EditStockDialog(
    item: CatalogItem,
    onSave: (CatalogItem) -> Unit,
    onDismiss: () -> Unit
) {
    var stockQty by remember { mutableStateOf(item.stockQty.toString()) }
    var price    by remember { mutableStateOf(item.price.toString()) }
    var reorder  by remember { mutableStateOf(item.reorderThreshold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = KfSurface,
        titleContentColor = KfTextPrimary,
        textContentColor  = KfTextSecondary,
        title = {
            Text(item.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    onSave(
                        item.copy(
                            price            = price.toDoubleOrNull() ?: item.price,
                            stockQty         = stockQty.toDoubleOrNull() ?: item.stockQty,
                            reorderThreshold = reorder.toDoubleOrNull() ?: item.reorderThreshold,
                            updatedAt        = System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = KfEmeraldBright, contentColor = KfBackgroundDeep)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = KfTextDisabled) }
        }
    )
}

@Composable
private fun KfField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, color = KfTextDisabled) },
        singleLine    = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = KfEmeraldBright,
            unfocusedBorderColor = KfBorderSubtle,
            focusedTextColor     = KfTextPrimary,
            unfocusedTextColor   = KfTextPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
