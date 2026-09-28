package com.kiranaflow.app.ui.screens.pastbills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranaflow.app.data.model.Bill
import com.kiranaflow.app.data.model.BillItem
import com.kiranaflow.app.data.repository.KiranaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class PastBillsUiState(
    val bills: List<Bill>            = emptyList(),
    val totalRevenue: Double         = 0.0,
    val selectedBill: Bill?          = null,
    val selectedBillItems: List<BillItem> = emptyList()
)

@HiltViewModel
class PastBillsViewModel @Inject constructor(
    private val repository: KiranaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PastBillsUiState())
    val uiState: StateFlow<PastBillsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRecentBills().collect { bills ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }
                val todayStart = cal.timeInMillis
                val todayRevenue = bills
                    .filter { it.createdAt >= todayStart }
                    .sumOf { it.totalAmount }

                _uiState.update { it.copy(bills = bills, totalRevenue = todayRevenue) }
            }
        }
    }

    fun selectBill(bill: Bill) {
        viewModelScope.launch {
            val items = repository.getBillItems(bill.id)
            _uiState.update { it.copy(selectedBill = bill, selectedBillItems = items) }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedBill = null, selectedBillItems = emptyList()) }
    }
}
