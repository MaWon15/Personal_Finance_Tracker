package com.example.personal_finance_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personal_finance_tracker.data.local.entity.CategoryEntity
import com.example.personal_finance_tracker.data.local.entity.TransactionEntity
import com.example.personal_finance_tracker.data.repository.CategoryRepository
import com.example.personal_finance_tracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

data class EditTransactionUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,

    val amountText: String = "",
    val amountError: String? = null,

    val type: TxType = TxType.EXPENSE,
    val note: String = "",

    val date: LocalDate = LocalDate.now(),

    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,

    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class EditTransactionViewModel(
    private val txRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val uid: String,
    private val transactionId: Long
) : ViewModel() {

    private var originalTx: TransactionEntity? = null

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    init {
        // categories
        categoryRepo.observeAll(uid)
            .onEach { cats -> _uiState.update { it.copy(categories = cats) } }
            .launchIn(viewModelScope)

        txRepo.observeById(uid, transactionId)
            .onEach { tx ->
                if (tx == null) {
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    originalTx = tx
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notFound = false,
                            amountText = centsToText(tx.amountCents),
                            amountError = null,
                            type = if (tx.type == "INCOME") TxType.INCOME else TxType.EXPENSE,
                            note = tx.note.orEmpty(),
                            date = LocalDate.ofEpochDay(tx.dateEpochDay),
                            selectedCategoryId = tx.categoryId
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun setAmount(text: String) {
        _uiState.update { it.copy(amountText = text, amountError = null) }
    }

    fun setType(type: TxType) {
        _uiState.update { it.copy(type = type) }
    }

    fun setNote(text: String) {
        _uiState.update { it.copy(note = text) }
    }

    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        val base = originalTx

        if (base == null) {
            _uiState.update { it.copy(errorMessage = "Transaction not loaded") }
            return
        }

        val cents = parseAmountToCents(state.amountText)
        if (cents == null || cents <= 0L) {
            _uiState.update { it.copy(amountError = "Enter a valid amount (e.g. 12.50)") }
            return
        }

        val updated = base.copy(
            amountCents = cents,
            type = state.type.name,
            note = state.note.trim().ifBlank { null },
            dateEpochDay = state.date.toEpochDay(),
            categoryId = state.selectedCategoryId
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                txRepo.update(updated)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Update failed") }
            }
        }
    }

    private fun centsToText(cents: Long): String {
        val abs = kotlin.math.abs(cents)
        val dollars = abs / 100
        val rem = abs % 100
        val sign = if (cents < 0) "-" else ""
        return "$sign$dollars.${rem.toString().padStart(2, '0')}"
    }

    private fun parseAmountToCents(input: String): Long? {
        val cleaned = input.trim().replace("$", "").replace(",", "")
        if (cleaned.isBlank()) return null
        return try {
            val bd = BigDecimal(cleaned)
            bd.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    class Factory(
        private val txRepo: TransactionRepository,
        private val categoryRepo: CategoryRepository,
        private val uid: String,
        private val transactionId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditTransactionViewModel(txRepo, categoryRepo, uid, transactionId) as T
        }
    }
}
