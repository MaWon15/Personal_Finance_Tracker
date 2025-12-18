package com.example.personal_finance_tracker.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personal_finance_tracker.data.local.DatabaseProvider
import com.example.personal_finance_tracker.data.local.entity.TransactionEntity
import com.example.personal_finance_tracker.data.repository.TransactionRepository
import com.example.personal_finance_tracker.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    uid: String,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val repo = remember { TransactionRepository(db) }

    val vm: TransactionsViewModel = viewModel(
        factory = TransactionsViewModel.Factory(repo, uid)
    )

    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.consumeError()
    }

    if (txToDelete != null) {
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Delete transaction?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.delete(txToDelete!!)
                        txToDelete = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    // Sort menu
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Text("Sort")
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Newest") },
                                onClick = { vm.setSortOrder(TxSortOrder.NEWEST); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest") },
                                onClick = { vm.setSortOrder(TxSortOrder.OLDEST); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Amount (High → Low)") },
                                onClick = { vm.setSortOrder(TxSortOrder.AMOUNT_DESC); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Amount (Low → High)") },
                                onClick = { vm.setSortOrder(TxSortOrder.AMOUNT_ASC); sortMenuExpanded = false }
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = onAdd) { Text("Add") }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Loading state cho actions (delete…)
            if (state.isMutating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.typeFilter == TxTypeFilter.ALL,
                    onClick = { vm.setTypeFilter(TxTypeFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = state.typeFilter == TxTypeFilter.INCOME,
                    onClick = { vm.setTypeFilter(TxTypeFilter.INCOME) },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = state.typeFilter == TxTypeFilter.EXPENSE,
                    onClick = { vm.setTypeFilter(TxTypeFilter.EXPENSE) },
                    label = { Text("Expense") }
                )
            }

            Spacer(Modifier.height(16.dp))

            if (state.items.isEmpty()) {
                Text("No transactions yet.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAdd) { Text("Create your first transaction") }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.items, key = { it.transactionId }) { tx ->
                        TransactionRow(
                            tx = tx,
                            onEdit = { onEdit(tx.transactionId) },
                            onDelete = { txToDelete = tx }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${tx.type} • ${formatCents(tx.amountCents)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "day=${tx.dateEpochDay}", style = MaterialTheme.typography.bodySmall)
            }

            if (!tx.note.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(tx.note!!, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "categoryId=${tx.categoryId ?: "Uncategorized"}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

private fun formatCents(cents: Long): String {
    val abs = kotlin.math.abs(cents)
    val dollars = abs / 100
    val rem = abs % 100
    val sign = if (cents < 0) "-" else ""
    return "$sign$$dollars.${rem.toString().padStart(2, '0')}"
}
