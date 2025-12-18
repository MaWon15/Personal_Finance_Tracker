package com.example.personal_finance_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.personal_finance_tracker.data.local.dao.CategoryDao
import com.example.personal_finance_tracker.data.local.dao.TransactionDao
import com.example.personal_finance_tracker.data.local.entity.CategoryEntity
import com.example.personal_finance_tracker.data.local.entity.TransactionEntity

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
