package com.example.personal_finance_tracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personal_finance_tracker.auth.AuthStatus
import com.example.personal_finance_tracker.auth.AuthViewModel
import com.example.personal_finance_tracker.navigation.AppNavGraph

@Composable
fun FinanceTrackerApp() {
    val authVm: AuthViewModel = viewModel()
    val status by authVm.status.collectAsState()

    val isLoggedIn = status is AuthStatus.LoggedIn
    val uid = (status as? AuthStatus.LoggedIn)?.uid ?: ""

    MaterialTheme {
        Surface {
            // reset nav graph when login/logout
            key(isLoggedIn) {
                AppNavGraph(
                    isLoggedIn = isLoggedIn,
                    uid = uid,
                    authVm = authVm
                )
            }
        }
    }
}
