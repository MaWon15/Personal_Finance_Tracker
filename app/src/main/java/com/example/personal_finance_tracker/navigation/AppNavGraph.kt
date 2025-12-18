package com.example.personal_finance_tracker.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.personal_finance_tracker.auth.AuthViewModel
import com.example.personal_finance_tracker.ui.screens.auth.LoginScreen
import com.example.personal_finance_tracker.ui.screens.auth.SignUpScreen
import com.example.personal_finance_tracker.ui.screens.categories.CategoriesScreen
import com.example.personal_finance_tracker.ui.screens.transactions.AddTransactionScreen
import com.example.personal_finance_tracker.ui.screens.transactions.EditTransactionScreen
import com.example.personal_finance_tracker.ui.screens.transactions.TransactionsListScreen
import com.example.personal_finance_tracker.ui.screens.home.HomeDashboardScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(
    isLoggedIn: Boolean,
    uid: String,
    authVm: AuthViewModel
) {
    val navController = rememberNavController()

    val startGraph = if (isLoggedIn) Routes.MAIN_GRAPH else Routes.AUTH_GRAPH

    NavHost(
        navController = navController,
        startDestination = startGraph
    ) {
        navigation(
            startDestination = Routes.LOGIN,
            route = Routes.AUTH_GRAPH
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    authVm = authVm,
                    onGoSignUp = { navController.navigate(Routes.SIGN_UP) }
                )
            }

            composable(Routes.SIGN_UP) {
                SignUpScreen(
                    authVm = authVm,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        navigation(
            startDestination = Routes.HOME,
            route = Routes.MAIN_GRAPH
        ) {
            composable(Routes.HOME) {
                HomeDashboardScreen(
                    uid = uid,
                    onGoTransactions = { navController.navigate(Routes.TRANSACTIONS) },
                    onGoCategories = { navController.navigate(Routes.CATEGORIES) },
                    onGoProfile = { navController.navigate(Routes.PROFILE) }
                )

            }

            composable(Routes.TRANSACTIONS) {
                TransactionsListScreen(
                    uid = uid,
                    onAdd = { navController.navigate(Routes.ADD_TRANSACTION) },
                    onEdit = { id -> navController.navigate(Routes.editTransaction(id)) }
                )
            }

            composable(Routes.ADD_TRANSACTION) {
                AddTransactionScreen(
                    uid = uid,
                    onDone = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EDIT_TRANSACTION,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("transactionId") ?: -1L

                EditTransactionScreen(
                    uid = uid,
                    transactionId = id,
                    onDone = { navController.popBackStack() }
                )
            }

            composable(Routes.CATEGORIES) {
                CategoriesScreen(uid = uid)
            }

            composable(Routes.PROFILE) {
                SimpleScreenScaffold(title = "Profile / Settings") {
                    val email = FirebaseAuth.getInstance().currentUser?.email
                    Text("UID: $uid")
                    Text("Email: ${email ?: "N/A"}")

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            authVm.signOut()
                            navController.navigate(Routes.AUTH_GRAPH) {
                                popUpTo(Routes.MAIN_GRAPH) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Text("Sign out")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleScreenScaffold(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            content = content
        )
    }
}
