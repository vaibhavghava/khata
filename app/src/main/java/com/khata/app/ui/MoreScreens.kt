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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khata.app.KhataViewModel
import com.khata.app.TYPE_RECEIVED
import com.khata.app.formatMoney
import com.khata.app.fullDate
import com.khata.app.prettyTime

@Composable
fun MoreScreen(onAllTransactions: () -> Unit, onSettings: () -> Unit, onAbout: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("More", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        MoreItem(Icons.AutoMirrored.Filled.List, "All Transactions", "Every entry from every customer", onAllTransactions)
        MoreItem(Icons.Default.Settings, "App Settings", "Theme and preferences", onSettings)
        MoreItem(Icons.Default.Info, "About", "Khata version 1.0", onAbout)
    }
}

@Composable
private fun MoreItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    KCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AllTransactionsScreen(vm: KhataViewModel, onBack: () -> Unit, onOpen: (Long) -> Unit) {
    val list by vm.allTxns.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        KhataTopBar("All Transactions", onBack)
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No transactions yet", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(list, key = { it.id }) { t ->
                    val received = t.type == TYPE_RECEIVED
                    val accent = if (received) receivedColor() else givenColor()
                    KCard(Modifier.fillMaxWidth(), onClick = { onOpen(t.customerId) }) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(10.dp).clip(CircleShape).background(accent)
                            )
                            Spacer(Modifier.size(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.customerName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${fullDate(t.date)} · ${prettyTime(t.time)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (t.note.isNotBlank()) {
                                    Text(t.note, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatMoney(t.amount), fontWeight = FontWeight.Bold, color = accent, style = MaterialTheme.typography.titleMedium)
                                Text(if (received) "Received" else "Given", style = MaterialTheme.typography.labelSmall, color = accent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(vm: KhataViewModel, onBack: () -> Unit) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        KhataTopBar("App Settings", onBack)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeSelector(theme) { vm.setTheme(it) }
            KCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("All amounts are shown in Indian Rupees", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("₹ INR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        KhataTopBar("About", onBack)
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(84.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) { Text("K", color = MaterialTheme.colorScheme.onPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp))
            Text("Khata", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Version 1.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Text(
                "A simple, private ledger for tracking money you receive and give. " +
                    "Everything is stored on this device only. There is no account, no cloud and no internet access.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
