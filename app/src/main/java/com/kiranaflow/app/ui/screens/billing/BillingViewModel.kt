package com.kiranaflow.app.ui.screens.billing

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiranaflow.app.data.model.*
import com.kiranaflow.app.data.repository.KiranaRepository
import com.kiranaflow.app.service.BillingQueue
import com.kiranaflow.app.service.VoiceRecognitionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillingUiState(
    val cart: Cart = Cart(),
    val catalog: List<CatalogItem> = emptyList(),
    val isListening: Boolean = false,
    val voiceText: String = "",
    val lastMatchedItem: CatalogItem? = null,
    val lastError: String? = null,
    val isCommitting: Boolean = false,
    val commitSuccess: Boolean = false,
    val showPaymentSheet: Boolean = false,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val tenderedAmount: String = ""
)

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val repository: KiranaRepository,
    private val voiceService: VoiceRecognitionService,
    private val vibrator: Vibrator
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    init {
        // Load catalog
        viewModelScope.launch {
            repository.getActiveCatalog().collect { items ->
                _uiState.update { it.copy(catalog = items) }
            }
        }

        // Restore active draft cart across crashes
        viewModelScope.launch {
            val draft = repository.loadDraftCart()
            if (draft != null && draft.lines.isNotEmpty()) {
                _uiState.update { it.copy(cart = draft) }
            }
        }

        // Observe voice recognition results
        viewModelScope.launch {
            voiceService.state.collect { voiceState ->
                handleVoiceState(voiceState)
            }
        }

        // WAL recovery on startup
        viewModelScope.launch {
            repository.replayPendingBills()
        }
    }

    // ─── Voice ──────────────────────────────────────────────────────────────

    fun toggleListening() {
        if (_uiState.value.isListening) {
            voiceService.stopListening()
            _uiState.update { it.copy(isListening = false, voiceText = "") }
        } else {
            voiceService.startListening()
            _uiState.update { it.copy(isListening = true, lastError = null) }
        }
    }

    /** Used for demo / testing without a real microphone. */
    fun simulateVoice(text: String) {
        voiceService.parseText(text)
    }

    private fun handleVoiceState(state: VoiceRecognitionService.VoiceState) {
        when (state) {
            is VoiceRecognitionService.VoiceState.Listening -> {
                _uiState.update { it.copy(isListening = true, voiceText = "Listening…") }
            }
            is VoiceRecognitionService.VoiceState.Recognised -> {
                _uiState.update { it.copy(voiceText = state.text) }
                processCommand(state.command)
            }
            is VoiceRecognitionService.VoiceState.Error -> {
                _uiState.update { it.copy(isListening = false, lastError = state.message, voiceText = "") }
            }
            VoiceRecognitionService.VoiceState.Idle -> {
                _uiState.update { it.copy(isListening = false) }
            }
        }
    }

    // ─── Command Router ──────────────────────────────────────────────────────

    private fun processCommand(command: VoiceCommand) {
        viewModelScope.launch {
            when (command.intent) {
                CommandIntent.ADD         -> handleAddItem(command)
                CommandIntent.REMOVE_LAST -> handleRemoveLast()
                CommandIntent.COMMIT      -> showPaymentSheet()
                CommandIntent.OPEN_CAMERA -> { /* handled by UI navigation */ }
                CommandIntent.MIC_ON      -> { /* already listening */ }
                CommandIntent.UNKNOWN     -> {
                    _uiState.update { it.copy(lastError = "Didn't catch that — try again") }
                }
            }
        }
    }

    private suspend fun handleAddItem(command: VoiceCommand) {
        val itemQuery = command.item ?: return
        val catalog = _uiState.value.catalog

        val matched = repository.findBestMatch(itemQuery, catalog)
        if (matched == null) {
            buzz(type = BuzzType.ERROR)
            _uiState.update { it.copy(lastError = "\"$itemQuery\" not in catalog") }
            return
        }

        val rawUnit = command.unit ?: matched.unit
        val displayUnit = DisplayUnit.fromString(rawUnit)
        val quantityBaseUnits = Quantity.of(command.quantity, displayUnit).baseUnits

        val line = CartLine(
            catalogItemId     = matched.id,
            itemName          = matched.name,
            quantityBaseUnits = quantityBaseUnits,
            unit              = displayUnit.label,
            pricePerUnitPaise = matched.pricePaise,
            inventoryType     = matched.inventoryType
        )

        buzz(type = BuzzType.SUCCESS)
        val updatedCart = _uiState.value.cart.addOrUpdate(line)
        _uiState.update { state ->
            state.copy(
                cart            = updatedCart,
                lastMatchedItem = matched,
                lastError       = null,
                voiceText       = ""
            )
        }
        repository.saveDraftCart(updatedCart)
    }

    private fun handleRemoveLast() {
        buzz(type = BuzzType.LIGHT)
        val updatedCart = _uiState.value.cart.removeLast()
        _uiState.update { it.copy(cart = updatedCart, lastError = null) }
        viewModelScope.launch {
            repository.saveDraftCart(updatedCart)
        }
    }

    // ─── Cart Actions (Manual UI) ────────────────────────────────────────────

    fun incrementItem(index: Int) {
        val state = _uiState.value
        val lines = state.cart.lines.toMutableList()
        if (index in lines.indices) {
            val current = lines[index]
            val unit = DisplayUnit.fromString(current.unit)
            val step = unit.multiplierToBase
            lines[index] = current.copy(quantityBaseUnits = current.quantityBaseUnits + step)
            val updatedCart = state.cart.copy(lines = lines)
            _uiState.update { it.copy(cart = updatedCart) }
            viewModelScope.launch { repository.saveDraftCart(updatedCart) }
        }
    }

    fun decrementItem(index: Int) {
        val state = _uiState.value
        val lines = state.cart.lines.toMutableList()
        if (index in lines.indices) {
            val current = lines[index]
            val unit = DisplayUnit.fromString(current.unit)
            val step = unit.multiplierToBase
            if (current.quantityBaseUnits > step) {
                lines[index] = current.copy(quantityBaseUnits = current.quantityBaseUnits - step)
            } else {
                lines.removeAt(index)
            }
            val updatedCart = state.cart.copy(lines = lines)
            _uiState.update { it.copy(cart = updatedCart) }
            viewModelScope.launch { repository.saveDraftCart(updatedCart) }
        }
    }

    fun removeItem(index: Int) {
        val state = _uiState.value
        val lines = state.cart.lines.toMutableList()
        if (index in lines.indices) {
            lines.removeAt(index)
            val updatedCart = state.cart.copy(lines = lines)
            _uiState.update { it.copy(cart = updatedCart) }
            viewModelScope.launch { repository.saveDraftCart(updatedCart) }
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = Cart(), lastError = null) }
        viewModelScope.launch { repository.clearDraftCart() }
    }

    // ─── Payment ─────────────────────────────────────────────────────────────

    fun showPaymentSheet() {
        if (_uiState.value.cart.lines.isEmpty()) {
            _uiState.update { it.copy(lastError = "Add items first") }
            return
        }
        _uiState.update { it.copy(showPaymentSheet = true) }
    }

    fun dismissPaymentSheet() {
        _uiState.update { it.copy(showPaymentSheet = false) }
    }

    fun setPaymentMode(mode: PaymentMode) {
        _uiState.update { it.copy(paymentMode = mode) }
    }

    fun setTenderedAmount(amount: String) {
        _uiState.update { it.copy(tenderedAmount = amount) }
    }

    fun confirmPayment() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isCommitting = true) }

            val tenderedPaise = state.tenderedAmount.toDoubleOrNull()?.let { (it * 100.0 + 0.5).toLong() }
                ?: state.cart.totalPaise

            val result = repository.commitBill(
                cartLines     = state.cart.lines,
                paymentMode   = state.paymentMode,
                tenderedPaise = tenderedPaise
            )

            when (result) {
                is BillingQueue.CommitResult.Success -> {
                    buzz(BuzzType.SUCCESS)
                    _uiState.update {
                        it.copy(
                            cart             = Cart(),
                            isCommitting     = false,
                            showPaymentSheet = false,
                            commitSuccess    = true,
                            lastError        = null,
                            tenderedAmount   = ""
                        )
                    }
                }
                is BillingQueue.CommitResult.Failure -> {
                    _uiState.update {
                        it.copy(isCommitting = false, lastError = result.reason)
                    }
                }
            }
        }
    }

    fun acknowledgeSuccess() {
        _uiState.update { it.copy(commitSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(lastError = null) }
    }

    // ─── Haptics ─────────────────────────────────────────────────────────────

    private enum class BuzzType { SUCCESS, ERROR, LIGHT }

    @Suppress("DEPRECATION")
    private fun buzz(type: BuzzType) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val effect = when (type) {
                    BuzzType.SUCCESS -> VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                    BuzzType.ERROR   -> VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 60), -1)
                    BuzzType.LIGHT   -> VibrationEffect.createOneShot(40, 50)
                }
                vibrator.vibrate(effect)
            } else {
                vibrator.vibrate(80)
            }
        } catch (_: Exception) { /* Vibrator not available in emulator */ }
    }

    override fun onCleared() {
        super.onCleared()
        voiceService.stopListening()
    }
}
