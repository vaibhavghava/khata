package com.khata.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khata.app.KhataViewModel
import com.khata.app.data.CustomerSummary
import com.khata.app.data.balance
import com.khata.app.formatMoney
import com.khata.app.statusOf
import kotlin.math.abs

@Composable
fun CustomersScreen(vm: KhataViewModel, onAdd: () -> Unit, onOpen: (Long) -> Unit) {
    val list by vm.summaries.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    val receivable = list.filter { it.balance > 0 }.sumOf { it.balance }
    val payable = list.filter { it.balance < 0 }.sumOf { -it.balance }
    val overall = receivable - payable

    val filtered = remember(list, query) {
        val q = query.trim()
        if (q.isEmpty()) list
        else list.filter { it.name.contains(q, ignoreCase = true) || it.phone.contains(q) }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column {
                    Text("Khata", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    val sub = if (profile.businessName.isNotBlank()) profile.businessName
                    else "${list.size} customer${if (list.size == 1) "" else "s"}"
                    Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(14.dp))
                    SummaryCard(receivable, payable, overall)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        placeholder = { Text("Search customers") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear") }
                            }
                        }
                    )
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (list.isEmpty()) "No customers yet" else "No matching customers",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (list.isEmpty()) "Tap + to add your first customer" else "Try a different name or number",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                        )
                    }
                }
            }
            items(filtered, key = { it.id }) { c -> CustomerRow(c) { onOpen(c.id) } }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) { Icon(Icons.Default.Add, contentDescription = "Add customer") }
    }
}

@Composable
private fun SummaryCard(receivable: Long, payable: Long, overall: Long) {
    val gradient = Brush.linearGradient(listOf(Color(0xFF2F3E9E), Color(0xFF5B6CE0)))
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(gradient).padding(20.dp)
    ) {
        Text("Overall Balance", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
        Text(formatMoney(overall), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(
            when {
                overall > 0 -> "Net receivable"
                overall < 0 -> "Net payable"
                else -> "All settled"
            },
            color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStat("Total Receivable", formatMoney(receivable), Modifier.weight(1f))
            MiniStat("Total Payable", formatMoney(payable), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.15f)).padding(14.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CustomerRow(c: CustomerSummary, onClick: () -> Unit) {
    val bal = c.balance
    KCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    c.name.trim().take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(c.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    c.phone.ifBlank { "No phone number" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatMoney(abs(bal)), fontWeight = FontWeight.Bold, color = balanceColor(bal), style = MaterialTheme.typography.titleMedium)
                Text(statusOf(bal), style = MaterialTheme.typography.labelSmall, color = balanceColor(bal))
            }
        }
    }
}
