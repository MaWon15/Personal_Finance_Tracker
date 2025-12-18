package com.example.personal_finance_tracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personal_finance_tracker.data.local.DatabaseProvider
import com.example.personal_finance_tracker.data.local.model.CategorySpend
import com.example.personal_finance_tracker.data.local.model.RecentTransactionItem
import com.example.personal_finance_tracker.data.repository.TransactionRepository
import com.example.personal_finance_tracker.viewmodel.DashboardViewModel
import java.time.LocalDate
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    uid: String,
    onGoTransactions: () -> Unit,
    onGoCategories: () -> Unit,
    onGoProfile: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val txRepo = remember { TransactionRepository(db) }

    val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(txRepo, uid))
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Dashboard") },
            actions = {
                TextButton(onClick = onGoProfile) {
                    Text("Profile")
                }
            }
        ) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // === BALANCE ===
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Balance", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = formatSignedMoney(state.balanceCents),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onGoTransactions) { Text("Transactions") }
                        OutlinedButton(onClick = onGoCategories) { Text("Categories") }
                    }
                }
            }

            // === RECENT TRANSACTIONS ===
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Recent transactions", style = MaterialTheme.typography.titleMedium)

                    if (state.recent.isEmpty()) {
                        Text("No transactions yet.")
                    } else {
                        state.recent.forEach { item ->
                            RecentRow(item = item)
                            Divider()
                        }
                    }
                }
            }

            // === SPENDING CATEGORIES (PIE CHART) ===
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Spending by category", style = MaterialTheme.typography.titleMedium)
                    Text("Expenses only • All time", style = MaterialTheme.typography.bodySmall)

                    if (!state.hasSpending) {
                        Text("No expense data yet.")
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PieChart(
                                items = state.spending,
                                modifier = Modifier.size(210.dp)
                            )

                            Legend(
                                items = state.spending,
                                total = state.totalSpendingCents,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentRow(item: RecentTransactionItem) {
    val date = runCatching { LocalDate.ofEpochDay(item.dateEpochDay) }.getOrNull()
    val category = item.categoryName ?: "Uncategorized"
    val signedCents = if (item.type == "INCOME") item.amountCents else -item.amountCents

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = listOfNotNull(date?.toString(), item.note?.takeIf { it.isNotBlank() }).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = formatSignedMoney(signedCents),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PieChart(items: List<CategorySpend>, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val palette = listOf(
        scheme.primary,
        scheme.secondary,
        scheme.tertiary,
        scheme.error,
        scheme.primaryContainer,
        scheme.secondaryContainer,
        scheme.tertiaryContainer,
        scheme.surfaceTint
    )

    val total = items.sumOf { it.spendCents }.toFloat().coerceAtLeast(1f)

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            var startAngle = -90f
            items.forEachIndexed { index, item ->
                val sweep = (item.spendCents.toFloat() / total) * 360f
                val color = palette[index % palette.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
private fun Legend(items: List<CategorySpend>, total: Long, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val palette = listOf(
        scheme.primary,
        scheme.secondary,
        scheme.tertiary,
        scheme.error,
        scheme.primaryContainer,
        scheme.secondaryContainer,
        scheme.tertiaryContainer,
        scheme.surfaceTint
    )

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.take(6).forEachIndexed { index, item ->
            val color = palette[index % palette.size]
            val pct = if (total > 0) (item.spendCents * 100.0 / total) else 0.0

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.categoryName, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${"%.1f".format(pct)}% • ${formatMoney(item.spendCents)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        if (items.size > 6) {
            Text("…and ${items.size - 6} more", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatMoney(cents: Long): String {
    val abs = abs(cents)
    val dollars = abs / 100
    val rem = abs % 100
    return "$$dollars.${rem.toString().padStart(2, '0')}"
}

private fun formatSignedMoney(cents: Long): String {
    val sign = if (cents < 0) "-" else "+"
    return sign + formatMoney(abs(cents))
}
