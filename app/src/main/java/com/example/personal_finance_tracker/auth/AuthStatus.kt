package com.example.personal_finance_tracker.auth

sealed interface AuthStatus {
    data object Loading : AuthStatus
    data object LoggedOut : AuthStatus
    data class LoggedIn(val uid: String, val email: String?) : AuthStatus
}
