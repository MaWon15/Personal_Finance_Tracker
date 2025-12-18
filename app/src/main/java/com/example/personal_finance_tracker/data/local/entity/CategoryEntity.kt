package com.example.personal_finance_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["ownerUid"]),
        Index(value = ["ownerUid", "name"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0L,
    val ownerUid: String,
    val name: String
)
