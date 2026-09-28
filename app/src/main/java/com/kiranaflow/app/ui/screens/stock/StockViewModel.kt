package com.kiranaflow.app.ui.screens.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranaflow.app.data.model.CatalogItem
import com.kiranaflow.app.data.repository.KiranaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockUiState(
    val catalog: List<CatalogItem>        = emptyList(),
    val lowStockItems: List<CatalogItem>  = emptyList(),
    val searchResults: List<CatalogItem>  = emptyList(),
    val editingItem: CatalogItem?         = null
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
    }

    fun search(query: String) {
        viewModelScope.launch {
            val results = if (query.isBlank()) emptyList()
            else repository.searchCatalog(query)
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun startEdit(item: CatalogItem) {
        _uiState.update { it.copy(editingItem = item) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingItem = null) }
    }

    fun saveEdit(item: CatalogItem) {
        viewModelScope.launch {
            repository.updateCatalogItem(item)
            _uiState.update { it.copy(editingItem = null) }
        }
    }
}
