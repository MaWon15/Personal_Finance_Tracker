package com.example.personal_finance_tracker.data.repository

import androidx.room.withTransaction
import com.example.personal_finance_tracker.data.local.AppDatabase
import com.example.personal_finance_tracker.data.local.entity.CategoryEntity
import com.example.personal_finance_tracker.data.local.model.CategoryWithCount
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val db: AppDatabase
) {
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()

    fun observeAll(uid: String): Flow<List<CategoryEntity>> =
        categoryDao.observeAll(uid)

    fun observeAllWithCounts(uid: String): Flow<List<CategoryWithCount>> =
        categoryDao.observeAllWithCounts(uid)

    suspend fun add(category: CategoryEntity): Long =
        categoryDao.insert(category)

    suspend fun update(category: CategoryEntity) {
        categoryDao.update(category)
    }

    suspend fun delete(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    suspend fun clearAllForUser(uid: String) {
        transactionDao.deleteAllForUser(uid)
        categoryDao.deleteAllForUser(uid)
    }

    suspend fun deleteCategoryWithPolicy(
        uid: String,
        categoryId: Long,
        policy: DeleteCategoryPolicy,
        moveToCategoryId: Long? = null
    ) {
        db.withTransaction {
            when (policy) {
                DeleteCategoryPolicy.MOVE -> {
                    require(moveToCategoryId != null) { "moveToCategoryId is required for MOVE policy" }
                    transactionDao.moveCategory(uid, categoryId, moveToCategoryId)
                }

                DeleteCategoryPolicy.UNCATEGORIZE -> {
                    transactionDao.moveCategory(uid, categoryId, null)
                }

                DeleteCategoryPolicy.DELETE_TRANSACTIONS -> {
                    transactionDao.deleteByCategory(uid, categoryId)
                }
            }
            categoryDao.deleteById(uid, categoryId)
        }
    }
}
