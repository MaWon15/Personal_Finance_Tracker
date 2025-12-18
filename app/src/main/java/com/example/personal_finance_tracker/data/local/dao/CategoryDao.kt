package com.example.personal_finance_tracker.data.local.dao

import androidx.room.*
import com.example.personal_finance_tracker.data.local.entity.CategoryEntity
import com.example.personal_finance_tracker.data.local.model.CategoryWithCount
import kotlinx.coroutines.flow.Flow


@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE ownerUid = :uid ORDER BY name ASC")
    fun observeAll(uid: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE ownerUid = :uid AND categoryId = :categoryId")
    suspend fun deleteById(uid: String, categoryId: Long)

    @Query("DELETE FROM categories WHERE ownerUid = :uid")
    suspend fun deleteAllForUser(uid: String)

    @Query("""
    SELECT 
        c.categoryId AS categoryId,
        c.ownerUid AS ownerUid,
        c.name AS name,
        COUNT(t.transactionId) AS txCount
    FROM categories c
    LEFT JOIN transactions t
        ON t.categoryId = c.categoryId
        AND t.ownerUid = :uid
    WHERE c.ownerUid = :uid
    GROUP BY c.categoryId
    ORDER BY c.name COLLATE NOCASE ASC
    """)
    fun observeAllWithCounts(uid: String): Flow<List<CategoryWithCount>>
}
