package com.example.personal_finance_tracker.data.repository

/**
 * Khi xoá Category, bạn muốn xử lý các Transaction thuộc category đó như thế nào?
 */
enum class DeleteCategoryPolicy {
    MOVE,
    UNCATEGORIZE,
    DELETE_TRANSACTIONS
}
