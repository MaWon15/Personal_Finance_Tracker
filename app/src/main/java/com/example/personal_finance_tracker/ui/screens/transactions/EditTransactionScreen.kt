package com.example.personal_finance_tracker.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personal_finance_tracker.data.local.DatabaseProvider
import com.example.personal_finance_tracker.data.repository.CategoryRepository
import com.example.personal_finance_tracker.data.repository.TransactionRepository
import com.example.personal_finance_tracker.viewmodel.EditTransactionViewModel
import com.example.personal_finance_tracker.viewmodel.TxType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    uid: String,
    transactionId: Long,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val txRepo = remember { TransactionRepository(db) }
    val categoryRepo = remember { CategoryRepository(db) }

    val vm: EditTransactionViewModel = viewModel(
        factory = EditTransactionViewModel.Factory(txRepo, categoryRepo, uid, transactionId)
    )

    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.consumeError()
    }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.date.toEpochDay() * 86_400_000L
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = LocalDate.ofEpochDay(millis / 86_400_000L)
                        vm.setDate(picked)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Transaction") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                state.isLoading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Loading...")
                    return@Column
                }

                state.notFound -> {
                    Text("Transaction not found.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onDone) { Text("Back") }
                    return@Column
                }

                else -> {
                    if (state.isSaving) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    // Type
                    Text("Type", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.type == TxType.EXPENSE,
                            onClick = { vm.setType(TxType.EXPENSE) },
                            label = { Text("Expense") }
                        )
                        FilterChip(
                            selected = state.type == TxType.INCOME,
                            onClick = { vm.setType(TxType.INCOME) },
                            label = { Text("Income") }
                        )
                    }

                    // Amount
                    OutlinedTextField(
                        value = state.amountText,
                        onValueChange = { vm.setAmount(it) },
                        label = { Text("Amount (e.g. 12.50)") },
                        singleLine = true,
                        isError = state.amountError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.amountError != null) {
                        Text(
                            text = state.amountError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Category
                    Text("Category", style = MaterialTheme.typography.titleMedium)
                    ExposedDropdownMenuBox(
                        expanded = categoryMenuExpanded,
                        onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                    ) {
                        val selectedLabel =
                            state.categories.firstOrNull { it.categoryId == state.selectedCategoryId }?.name
                                ?: "Uncategorized"

                        OutlinedTextField(
                            value = selectedLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Uncategorized") },
                                onClick = {
                                    vm.selectCategory(null)
                                    categoryMenuExpanded = false
                                }
                            )
                            state.categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        vm.selectCategory(c.categoryId)
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Date
                    Text("Date", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text(state.date.toString())
                    }

                    // Note
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = { vm.setNote(it) },
                        label = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { vm.save(onSuccess = onDone) },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}
