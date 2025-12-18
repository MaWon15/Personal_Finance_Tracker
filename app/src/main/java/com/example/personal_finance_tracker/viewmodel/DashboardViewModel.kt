package com.example.personal_finance_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personal_finance_tracker.data.local.model.CategorySpend
import com.example.personal_finance_tracker.data.local.model.RecentTransactionItem
import com.example.personal_finance_tracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlin.math.abs

data class DashboardUiState(
    val balanceCents: Long = 0L,
    val recent: List<RecentTransactionItem> = emptyList(),
    val spending: List<CategorySpend> = emptyList()
) {
    val totalSpendingCents: Long get() = spending.sumOf { it.spendCents }
    val hasSpending: Boolean get() = totalSpendingCents > 0
}

class DashboardViewModel(
    private val txRepo: TransactionRepository,
    private val uid: String
) : ViewModel() {

    private val balanceFlow = txRepo.observeBalanceCents(uid)
    private val recentFlow = txRepo.observeRecentItems(uid, limit = 8)
    private val spendingFlow = txRepo.observeSpendingByCategory(uid)

    val uiState: StateFlow<DashboardUiState> =
        combine(balanceFlow, recentFlow, spendingFlow) { bal, recent, spending ->
            DashboardUiState(
                balanceCents = bal,
                recent = recent,
                spending = spending
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )

    class Factory(
        private val txRepo: TransactionRepository,
        private val uid: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(txRepo, uid) as T
        }
    }
}
