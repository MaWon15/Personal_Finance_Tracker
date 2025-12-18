package com.example.personal_finance_tracker.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.personal_finance_tracker.data.local.DatabaseProvider
import com.example.personal_finance_tracker.data.local.entity.CategoryEntity
import com.example.personal_finance_tracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDebugScreen() {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val uid = remember { "debug_user" }
    val categories by db.categoryDao().observeAll(uid).collectAsState(initial = emptyList())
    val transactions by db.transactionDao().observeAll(uid).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Room Debug") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())

                                val foodId = db.categoryDao().insert(
                                    CategoryEntity(ownerUid = uid, name = "Food")
                                )
                                val salaryId = db.categoryDao().insert(
                                    CategoryEntity(ownerUid = uid, name = "Salary")
                                )

                                db.transactionDao().insert(
                                    TransactionEntity(
                                        ownerUid = uid,
                                        amountCents = 1250, // $12.50
                                        type = "EXPENSE",
                                        note = "Banh mi",
                                        dateEpochDay = todayEpochDay,
                                        categoryId = foodId
                                    )
                                )

                                db.transactionDao().insert(
                                    TransactionEntity(
                                        ownerUid = uid,
                                        amountCents = 250000, // $2500.00
                                        type = "INCOME",
                                        note = "Paycheck",
                                        dateEpochDay = todayEpochDay,
                                        categoryId = salaryId
                                    )
                                )

                                message = "Seeded 2 categories + 2 transactions ✅"
                            } catch (e: Exception) {
                                message = "Seed error: ${e.message}"
                            }
                        }
                    }
                ) { Text("Seed Data") }

                OutlinedButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                db.transactionDao().deleteAllForUser(uid)
                                db.categoryDao().deleteAllForUser(uid)
                                message = "Cleared all data ✅"
                            } catch (e: Exception) {
                                message = "Clear error: ${e.message}"
                            }
                        }
                    }
                ) { Text("Clear Data") }
            }

            if (message != null) {
                Spacer(Modifier.height(12.dp))
                Text(message!!)
            }

            Spacer(Modifier.height(16.dp))

            Text("Categories (${categories.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(categories) { c ->
                    Text("• #${c.categoryId}  ${c.name}")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text("Transactions (${transactions.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(transactions) { t ->
                    Text("• #${t.transactionId}  ${t.type}  ${t.amountCents}¢  cat=${t.categoryId}  day=${t.dateEpochDay}")
                    if (!t.note.isNullOrBlank()) {
                        Text("   note: ${t.note}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
