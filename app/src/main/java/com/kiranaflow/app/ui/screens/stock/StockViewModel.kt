package com.kiranaflow.app.ui.screens.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranaflow.app.data.model.CatalogItem
import com.kiranaflow.app.data.repository.KiranaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StockUiState(
    val catalog: List<CatalogItem>       = emptyList(),
    val lowStockItems: List<CatalogItem> = emptyList(),
    val searchResults: List<CatalogItem> = emptyList(),
    val editingItem: CatalogItem?        = null,
    // Dashboard metrics
    val todayRevenue: Double  = 0.0,
    val todayBillCount: Int   = 0,
    val cashAmount: Double    = 0.0,
    val upiAmount: Double     = 0.0
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: KiranaRepository
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
                    .filter { it.paymentMode == com.kiranaflow.app.data.model.PaymentMode.CASH }
                    .sumOf { it.totalAmount }
                val upi = todayBills
                    .filter { it.paymentMode != com.kiranaflow.app.data.model.PaymentMode.CASH }
                    .sumOf { it.totalAmount }

                _uiState.update {
                    it.copy(
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
}
