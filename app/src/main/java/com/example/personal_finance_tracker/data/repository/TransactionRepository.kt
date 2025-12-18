package com.example.personal_finance_tracker.data.repository

import com.example.personal_finance_tracker.data.local.AppDatabase
import com.example.personal_finance_tracker.data.local.entity.TransactionEntity
import com.example.personal_finance_tracker.data.local.model.CategorySpend
import com.example.personal_finance_tracker.data.local.model.RecentTransactionItem
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val db: AppDatabase
) {
    private val transactionDao = db.transactionDao()

    fun observeAll(uid: String): Flow<List<TransactionEntity>> =
        transactionDao.observeAll(uid)

    fun observeByCategory(uid: String, categoryId: Long): Flow<List<TransactionEntity>> =
        transactionDao.observeByCategory(uid, categoryId)

    fun observeByDateRange(
        uid: String,
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<TransactionEntity>> =
        transactionDao.observeByDateRange(uid, startEpochDay, endEpochDay)

    fun observeById(uid: String, transactionId: Long): Flow<TransactionEntity?> =
        transactionDao.observeById(uid, transactionId)

    suspend fun add(tx: TransactionEntity): Long =
        transactionDao.insert(tx)

    suspend fun update(tx: TransactionEntity) {
        transactionDao.update(tx)
    }

    suspend fun delete(tx: TransactionEntity) {
        transactionDao.delete(tx)
    }

    suspend fun clearAllForUser(uid: String) {
        transactionDao.deleteAllForUser(uid)
    }

    fun observeBalanceCents(uid: String): Flow<Long> =
        transactionDao.observeBalanceCents(uid)

    fun observeRecentItems(uid: String, limit: Int): Flow<List<RecentTransactionItem>> =
        transactionDao.observeRecentItems(uid, limit)

    fun observeSpendingByCategory(uid: String): Flow<List<CategorySpend>> =
        transactionDao.observeSpendingByCategory(uid)
}
