package com.example.personal_finance_tracker.data.local.model

data class CategoryWithCount(
    val categoryId: Long,
    val ownerUid: String,
    val name: String,
    val txCount: Int
)
