package com.example.personal_finance_tracker.ui.screens.categories

import androidx.compose.foundation.clickable
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
import com.example.personal_finance_tracker.data.local.entity.CategoryEntity
import com.example.personal_finance_tracker.data.repository.CategoryRepository
import com.example.personal_finance_tracker.data.repository.DeleteCategoryPolicy
import com.example.personal_finance_tracker.viewmodel.CategoriesViewModel
import com.example.personal_finance_tracker.data.local.model.CategoryWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(uid: String) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val repo = remember { CategoryRepository(db) }

    val vm: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory(repo, uid))
    val state by vm.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.consumeError()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<CategoryWithCount?>(null) }
    var deleteTarget by remember { mutableStateOf<CategoryWithCount?>(null) }

    if (showAddDialog) {
        CategoryNameDialog(
            title = "Add Category",
            initialName = "",
            confirmText = "Add",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                vm.addCategory(name)
                showAddDialog = false
            }
        )
    }

    if (editTarget != null) {
        val target = editTarget!!
        CategoryNameDialog(
            title = "Edit Category",
            initialName = target.name,
            confirmText = "Save",
            onDismiss = { editTarget = null },
            onConfirm = { newName ->
                vm.updateCategory(target.categoryId, newName)
                editTarget = null
            }
        )
    }

    if (deleteTarget != null) {
        val target = deleteTarget!!
        DeleteCategoryPolicyDialog(
            category = target,
            allCategories = state.categories,
            onDismiss = { deleteTarget = null },
            onConfirm = { policy, moveToId ->
                vm.deleteCategoryWithPolicy(
                    categoryId = target.categoryId,
                    policy = policy,
                    moveToCategoryId = moveToId
                )
                deleteTarget = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                actions = {
                    Button(onClick = { showAddDialog = true }) { Text("Add") }
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
            if (state.isMutating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            if (state.categories.isEmpty()) {
                Text("No categories yet.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { showAddDialog = true }) { Text("Create your first category") }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.categories, key = { it.categoryId }) { c ->
                        CategoryRow(
                            category = c,
                            onEdit = { editTarget = c },
                            onDelete = { deleteTarget = c }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: CategoryWithCount,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(category.name, style = MaterialTheme.typography.titleMedium)
                Text("id=${category.categoryId} • ${category.txCount} transactions",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initialName: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tip: names must be unique per user.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteCategoryPolicyDialog(
    category: CategoryWithCount,
    allCategories: List<CategoryWithCount>,
    onDismiss: () -> Unit,
    onConfirm: (DeleteCategoryPolicy, Long?) -> Unit
) {
    val moveTargets = remember(allCategories, category.categoryId) {
        allCategories.filter { it.categoryId != category.categoryId }
    }

    var selectedPolicy by remember { mutableStateOf(DeleteCategoryPolicy.UNCATEGORIZE) }
    var moveToId by remember { mutableStateOf<Long?>(moveTargets.firstOrNull()?.categoryId) }
    var moveMenuExpanded by remember { mutableStateOf(false) }

    val moveEnabled = moveTargets.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete \"${category.name}\"?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                Text("This category currently has ${category.txCount} transactions.")

                Text("What should we do with those transactions?")

                PolicyRow(
                    selected = selectedPolicy == DeleteCategoryPolicy.UNCATEGORIZE,
                    title = "Uncategorized",
                    subtitle = "Keep transactions, set category = null",
                    onClick = { selectedPolicy = DeleteCategoryPolicy.UNCATEGORIZE }
                )

                PolicyRow(
                    selected = selectedPolicy == DeleteCategoryPolicy.DELETE_TRANSACTIONS,
                    title = "Delete transactions",
                    subtitle = "Remove all transactions in this category",
                    onClick = { selectedPolicy = DeleteCategoryPolicy.DELETE_TRANSACTIONS }
                )

                PolicyRow(
                    selected = selectedPolicy == DeleteCategoryPolicy.MOVE,
                    title = "Move transactions",
                    subtitle = if (moveEnabled) "Move to another category" else "Create another category to enable moving",
                    enabled = moveEnabled,
                    onClick = { if (moveEnabled) selectedPolicy = DeleteCategoryPolicy.MOVE }
                )

                if (selectedPolicy == DeleteCategoryPolicy.MOVE) {
                    ExposedDropdownMenuBox(
                        expanded = moveMenuExpanded,
                        onExpandedChange = { moveMenuExpanded = !moveMenuExpanded }
                    ) {
                        val label = moveTargets.firstOrNull { it.categoryId == moveToId }?.name ?: "Select category"

                        OutlinedTextField(
                            value = label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Move to") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = moveMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = moveMenuExpanded,
                            onDismissRequest = { moveMenuExpanded = false }
                        ) {
                            moveTargets.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        moveToId = c.categoryId
                                        moveMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val canConfirm =
                selectedPolicy != DeleteCategoryPolicy.MOVE || (moveEnabled && moveToId != null)

            TextButton(
                enabled = canConfirm,
                onClick = {
                    val moveId = if (selectedPolicy == DeleteCategoryPolicy.MOVE) moveToId else null
                    onConfirm(selectedPolicy, moveId)
                }
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PolicyRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadioButton(selected = selected, onClick = if (enabled) onClick else null, enabled = enabled)
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
