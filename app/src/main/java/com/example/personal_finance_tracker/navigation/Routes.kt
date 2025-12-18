package com.example.personal_finance_tracker.navigation

object Routes {
    // Graph routes
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"

    // Auth screens
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"

    // Main screens
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val ADD_TRANSACTION = "add_transaction"

    // With argument
    const val EDIT_TRANSACTION = "edit_transaction/{transactionId}"
    fun editTransaction(transactionId: Long): String = "edit_transaction/$transactionId"
    const val CATEGORIES = "categories"
    const val PROFILE = "profile"
}
