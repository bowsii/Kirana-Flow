package com.kiranaflow.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kiranaflow.core.ui.theme.KfNavy
import com.kiranaflow.core.ui.theme.KfNavyLight

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
    unselected: ImageVector,
    selected: ImageVector,
    label: String,
    isSelected: Boolean,
    isMic: Boolean = false,
    onClick: () -> Unit
) {
    if (isMic && isSelected) {
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
