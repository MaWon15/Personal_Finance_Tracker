package com.example.personal_finance_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personal_finance_tracker.data.local.entity.TransactionEntity
import com.example.personal_finance_tracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TxTypeFilter { ALL, INCOME, EXPENSE }
enum class TxSortOrder { NEWEST, OLDEST, AMOUNT_DESC, AMOUNT_ASC }

data class TransactionsUiState(
    val items: List<TransactionEntity> = emptyList(),
    val isMutating: Boolean = false,     // dùng cho delete/update/insert
    val errorMessage: String? = null,
    val typeFilter: TxTypeFilter = TxTypeFilter.ALL,
    val sortOrder: TxSortOrder = TxSortOrder.NEWEST
)

class TransactionsViewModel(
    private val repo: TransactionRepository,
    private val uid: String
) : ViewModel() {

    private val typeFilter = MutableStateFlow(TxTypeFilter.ALL)
    private val sortOrder = MutableStateFlow(TxSortOrder.NEWEST)
    private val isMutating = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val transactionsFlow: Flow<List<TransactionEntity>> =
        repo.observeAll(uid)

    val uiState: StateFlow<TransactionsUiState> =
        combine(
            transactionsFlow,
            typeFilter,
            sortOrder,
            isMutating,
            errorMessage
        ) { items, filter, sort, mutating, error ->
            val filtered = when (filter) {
                TxTypeFilter.ALL -> items
                TxTypeFilter.INCOME -> items.filter { it.type == "INCOME" }
                TxTypeFilter.EXPENSE -> items.filter { it.type == "EXPENSE" }
            }

            val sorted = when (sort) {
                TxSortOrder.NEWEST -> filtered.sortedWith(
                    compareByDescending<TransactionEntity> { it.dateEpochDay }
                        .thenByDescending { it.transactionId }
                )
                TxSortOrder.OLDEST -> filtered.sortedWith(
                    compareBy<TransactionEntity> { it.dateEpochDay }
                        .thenBy { it.transactionId }
                )
                TxSortOrder.AMOUNT_DESC -> filtered.sortedByDescending { it.amountCents }
                TxSortOrder.AMOUNT_ASC -> filtered.sortedBy { it.amountCents }
            }

            TransactionsUiState(
                items = sorted,
                isMutating = mutating,
                errorMessage = error,
                typeFilter = filter,
                sortOrder = sort
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionsUiState()
        )

    fun setTypeFilter(filter: TxTypeFilter) {
        typeFilter.value = filter
    }

    fun setSortOrder(order: TxSortOrder) {
        sortOrder.value = order
    }

    fun consumeError() {
        errorMessage.value = null
    }

    fun delete(tx: TransactionEntity) {
        viewModelScope.launch {
            isMutating.value = true
            try {
                repo.delete(tx)
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Delete failed"
            } finally {
                isMutating.value = false
            }
        }
    }

    class Factory(
        private val repo: TransactionRepository,
        private val uid: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionsViewModel(repo, uid) as T
        }
    }
}
