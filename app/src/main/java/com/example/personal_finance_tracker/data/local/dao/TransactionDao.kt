package com.example.personal_finance_tracker.data.local.dao

import androidx.room.*
import com.example.personal_finance_tracker.data.local.entity.TransactionEntity
import com.example.personal_finance_tracker.data.local.model.CategorySpend
import com.example.personal_finance_tracker.data.local.model.RecentTransactionItem
import kotlinx.coroutines.flow.Flow


@Dao
interface TransactionDao {

    @Query("""
        SELECT * FROM transactions 
        WHERE ownerUid = :uid
        ORDER BY dateEpochDay DESC, transactionId DESC
    """)
    fun observeAll(uid: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE ownerUid = :uid AND categoryId = :categoryId
        ORDER BY dateEpochDay DESC, transactionId DESC
    """)
    fun observeByCategory(uid: String, categoryId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE ownerUid = :uid AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY dateEpochDay DESC, transactionId DESC
    """)
    fun observeByDateRange(uid: String, startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(tx: TransactionEntity): Long

    @Update
    suspend fun update(tx: TransactionEntity)

    @Delete
    suspend fun delete(tx: TransactionEntity)

    @Query("""
        UPDATE transactions 
        SET categoryId = :newCategoryId
        WHERE ownerUid = :uid AND categoryId = :oldCategoryId
    """)
    suspend fun moveCategory(uid: String, oldCategoryId: Long, newCategoryId: Long?)

    @Query("DELETE FROM transactions WHERE ownerUid = :uid AND categoryId = :categoryId")
    suspend fun deleteByCategory(uid: String, categoryId: Long)

    @Query("DELETE FROM transactions WHERE ownerUid = :uid")
    suspend fun deleteAllForUser(uid: String)

    @Query("""
    SELECT * FROM transactions
    WHERE ownerUid = :uid AND transactionId = :transactionId
    LIMIT 1
""")
    fun observeById(uid: String, transactionId: Long): kotlinx.coroutines.flow.Flow<TransactionEntity?>

    @Query("""
    SELECT COALESCE(SUM(
        CASE 
            WHEN type = 'INCOME' THEN amountCents
            ELSE -amountCents
        END
    ), 0)
    FROM transactions
    WHERE ownerUid = :uid
""")
    fun observeBalanceCents(uid: String): Flow<Long>

    @Query("""
    SELECT 
        t.transactionId AS transactionId,
        t.type AS type,
        t.amountCents AS amountCents,
        t.dateEpochDay AS dateEpochDay,
        t.note AS note,
        c.name AS categoryName
    FROM transactions t
    LEFT JOIN categories c
        ON c.categoryId = t.categoryId AND c.ownerUid = t.ownerUid
    WHERE t.ownerUid = :uid
    ORDER BY t.dateEpochDay DESC, t.transactionId DESC
    LIMIT :limit
""")
    fun observeRecentItems(uid: String, limit: Int): Flow<List<RecentTransactionItem>>

    @Query("""
    SELECT
        t.categoryId AS categoryId,
        COALESCE(c.name, 'Uncategorized') AS categoryName,
        COALESCE(SUM(t.amountCents), 0) AS spendCents
    FROM transactions t
    LEFT JOIN categories c
        ON c.categoryId = t.categoryId AND c.ownerUid = t.ownerUid
    WHERE t.ownerUid = :uid AND t.type = 'EXPENSE'
    GROUP BY t.categoryId, categoryName
    HAVING spendCents > 0
    ORDER BY spendCents DESC
""")
    fun observeSpendingByCategory(uid: String): Flow<List<CategorySpend>>
}
