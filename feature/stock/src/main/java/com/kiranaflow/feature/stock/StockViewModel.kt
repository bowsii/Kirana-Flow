package com.kiranaflow.feature.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranaflow.core.model.CatalogItem
import com.kiranaflow.core.data.KiranaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

import com.kiranaflow.core.model.BusinessDayManager
import com.kiranaflow.core.model.DaySummary
import com.kiranaflow.core.model.FlowPurchasePlanItem
import com.kiranaflow.core.domain.usecase.CloseBusinessDayUseCase
import com.kiranaflow.core.domain.usecase.GetReorderListUseCase
import com.kiranaflow.core.domain.usecase.GetTomorrowFlowPlanUseCase

data class StockUiState(
    val catalog: List<CatalogItem>       = emptyList(),
    val lowStockItems: List<CatalogItem> = emptyList(),
    val searchResults: List<CatalogItem> = emptyList(),
    val editingItem: CatalogItem?        = null,
    // Dashboard metrics
    val businessDate: String             = "",
    val todayRevenue: Double             = 0.0,
    val todayBillCount: Int              = 0,
    val cashAmount: Double               = 0.0,
    val upiAmount: Double                = 0.0,
    val isDayClosed: Boolean             = false,
    val showCloseDayDialog: Boolean      = false,
    val daySummary: DaySummary?          = null
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: KiranaRepository,
    private val businessDayManager: BusinessDayManager,
    private val closeBusinessDayUseCase: CloseBusinessDayUseCase,
    private val getReorderListUseCase: GetReorderListUseCase,
    private val getTomorrowFlowPlanUseCase: GetTomorrowFlowPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveCatalog().collect { items ->
                _uiState.update { it.copy(catalog = items) }
            }
        }
        viewModelScope.launch {
            repository.getLowStockItems().collect { items ->
                _uiState.update { it.copy(lowStockItems = items) }
            }
        }
        viewModelScope.launch {
            repository.getRecentBills().collect { bills ->
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis

                val todayBills = bills.filter { it.createdAt >= todayStart }
                val revenue    = todayBills.sumOf { it.totalAmount }

                // Split by payment mode
                val cash = todayBills
                    .filter { it.paymentMode == com.kiranaflow.core.model.PaymentMode.CASH }
                    .sumOf { it.totalAmount }
                val upi = todayBills
                    .filter { it.paymentMode != com.kiranaflow.core.model.PaymentMode.CASH }
                    .sumOf { it.totalAmount }

                val currentDate = businessDayManager.getBusinessDate()
                _uiState.update {
                    it.copy(
                        businessDate   = currentDate,
                        todayRevenue   = revenue,
                        todayBillCount = todayBills.size,
                        cashAmount     = cash,
                        upiAmount      = upi
                    )
                }
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            val results = if (query.isBlank()) emptyList()
            else repository.searchCatalog(query)
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun startEdit(item: CatalogItem) = _uiState.update { it.copy(editingItem = item) }
    fun cancelEdit()                  = _uiState.update { it.copy(editingItem = null) }

    fun saveEdit(item: CatalogItem) {
        viewModelScope.launch {
            repository.updateCatalogItem(item)
            _uiState.update { it.copy(editingItem = null) }
        }
    }

    fun updateReorderThreshold(item: CatalogItem, newThresholdDisplayUnits: Double) {
        viewModelScope.launch {
            val baseUnits = (newThresholdDisplayUnits * item.displayUnit.multiplierToBase + 0.5).toLong()
            repository.updateReorderThreshold(item.id, baseUnits)
        }
    }

    fun rebuildStock(item: CatalogItem) {
        viewModelScope.launch {
            repository.rebuildStockFromLedger(item.id)
        }
    }

    fun openCloseDayDialog() {
        viewModelScope.launch {
            val bDate = businessDayManager.getBusinessDate()
            val summary = closeBusinessDayUseCase(bDate)
            _uiState.update {
                it.copy(
                    businessDate = bDate,
                    daySummary = summary,
                    showCloseDayDialog = true
                )
            }
        }
    }

    fun dismissCloseDayDialog() {
        _uiState.update { it.copy(showCloseDayDialog = false) }
    }

    fun confirmCloseDay() {
        _uiState.update {
            it.copy(
                isDayClosed = true,
                showCloseDayDialog = false
            )
        }
    }

    fun buildShareSummaryText(): String {
        val state = _uiState.value
        val summary = state.daySummary
        val date = summary?.businessDate ?: businessDayManager.getBusinessDate()
        val revenue = summary?.totalRevenue ?: state.todayRevenue
        val cash = summary?.cashRevenue ?: state.cashAmount
        val upi = summary?.upiRevenue ?: state.upiAmount
        val bills = summary?.totalBills ?: state.todayBillCount

        val sb = StringBuilder()
        sb.appendLine("🏪 *KiranaFlow — Day Summary*")
        sb.appendLine("📅 Business Date: $date")
        sb.appendLine("💰 Gross Revenue: ₹${String.format(java.util.Locale.US, "%.2f", revenue)}")
        sb.appendLine("   • Cash: ₹${String.format(java.util.Locale.US, "%.2f", cash)}")
        sb.appendLine("   • UPI: ₹${String.format(java.util.Locale.US, "%.2f", upi)}")
        sb.appendLine("🧾 Total Bills: $bills")
        sb.appendLine()

        sb.appendLine("🛒 *Tomorrow's FLOW Purchase Plan:*")
        val flowPlan = summary?.flowPurchasePlan ?: emptyList()
        if (flowPlan.isEmpty()) {
            sb.appendLine("  (No FLOW items sold today)")
        } else {
            flowPlan.forEach { item ->
                sb.appendLine("  • ${item.itemName}: ${item.suggestedPurchaseDisplayUnits} ${item.displayUnit}")
            }
        }
        sb.appendLine()

        sb.appendLine("⚠️ *Low Stock Reorder List:*")
        val reorders = summary?.stockReorderList ?: state.lowStockItems
        if (reorders.isEmpty()) {
            sb.appendLine("  (All shelf stocks are healthy)")
        } else {
            reorders.forEach { item ->
                val currDisplay = item.stockBaseUnits.toDouble() / item.displayUnit.multiplierToBase
                val minDisplay = item.reorderThresholdBaseUnits.toDouble() / item.displayUnit.multiplierToBase
                sb.appendLine("  • ${item.name}: $currDisplay ${item.displayUnit.name} (Min: $minDisplay)")
            }
        }

        return sb.toString().trimEnd()
    }
}
