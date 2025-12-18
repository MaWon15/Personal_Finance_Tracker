package com.example.personal_finance_tracker.data.local.model

data class RecentTransactionItem(
    val transactionId: Long,
    val type: String,
    val amountCents: Long,
    val dateEpochDay: Long,
    val note: String?,
    val categoryName: String?
)
