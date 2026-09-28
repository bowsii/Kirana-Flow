package com.kiranaflow.core.model

data class DaySummary(
    val businessDate: String,
    val totalRevenuePaise: Long,
    val totalBills: Int,
    val cashRevenuePaise: Long,
    val upiRevenuePaise: Long,
    val flowPurchasePlan: List<FlowPurchasePlanItem>,
    val stockReorderList: List<CatalogItem>
) {
    val totalRevenue: Double get() = totalRevenuePaise / 100.0
    val cashRevenue: Double get() = cashRevenuePaise / 100.0
    val upiRevenue: Double get() = upiRevenuePaise / 100.0
}

data class FlowPurchasePlanItem(
    val itemId: String,
    val itemName: String,
    val soldBaseUnits: Long,
    val suggestedPurchaseDisplayUnits: Double,
    val displayUnit: String
)
