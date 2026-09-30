package com.khata.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khata.app.KhataViewModel
import com.khata.app.TYPE_GIVEN
import com.khata.app.TYPE_RECEIVED
import com.khata.app.data.CustomerSummary
import com.khata.app.data.Txn
import com.khata.app.data.balance
import com.khata.app.dayLabel
import com.khata.app.formatMoney
import com.khata.app.fullDate
import com.khata.app.prettyTime
import com.khata.app.statusOf
import kotlin.math.abs

private sealed interface ChatItem {
    data class Header(val date: String) : ChatItem
    data class Entry(val txn: Txn) : ChatItem
}

@Composable
fun LedgerScreen(
    vm: KhataViewModel,
    customerId: Long,
    onBack: () -> Unit,
    onEditCustomer: () -> Unit,
    onAddTxn: (String) -> Unit,
    onEditTxn: (Long, String) -> Unit,
    onCustomerDeleted: () -> Unit
) {
    val summary by remember(customerId) { vm.summary(customerId) }.collectAsStateWithLifecycle(null)
    val txns by remember(customerId) { vm.txns(customerId) }.collectAsStateWithLifecycle(emptyList())
    val profile by vm.profile.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    var menu by remember { mutableStateOf(false) }
    var confirmDeleteCustomer by remember { mutableStateOf(false) }
    var txnToDelete by remember { mutableStateOf<Txn?>(null) }

    val chat = remember(txns) {
        buildList<ChatItem> {
            var last = ""
            txns.forEach {
                if (it.date != last) { add(ChatItem.Header(it.date)); last = it.date }
                add(ChatItem.Entry(it))
            }
        }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(chat.size) {
        if (chat.isNotEmpty()) listState.scrollToItem(chat.lastIndex)
    }

    val s = summary

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text(s?.name.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (s == null) "" else s.phone.ifBlank { "No phone number" },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (s != null) {
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Menu") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit customer") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { menu = false; onEditCustomer() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete customer") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { menu = false; confirmDeleteCustomer = true }
                        )
                    }
                }
            }
        }

        if (s != null) {
            BalanceCard(s)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pad = PaddingValues(horizontal = 8.dp)
                Button(
                    onClick = { onAddTxn(TYPE_RECEIVED) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    contentPadding = pad,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E8E4E), contentColor = Color.White)
                ) { Text("Received", maxLines = 1) }
                Button(
                    onClick = { onAddTxn(TYPE_GIVEN) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    contentPadding = pad,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD13B3B), contentColor = Color.White)
                ) { Text("Given", maxLines = 1) }
                OutlinedButton(
                    onClick = { sendMessage(ctx, s, profile.businessName) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    contentPadding = pad
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Message", maxLines = 1)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (chat.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No transactions yet.\nTap Received or Given to add the first entry.",
                        textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        chat,
                        key = { when (it) { is ChatItem.Header -> "h" + it.date; is ChatItem.Entry -> "t" + it.txn.id } }
                    ) { item ->
                        when (item) {
                            is ChatItem.Header -> DateChip(dayLabel(item.date))
                            is ChatItem.Entry -> TxnBubble(
                                t = item.txn,
                                onEdit = { onEditTxn(item.txn.id, item.txn.type) },
                                onDelete = { txnToDelete = item.txn }
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmDeleteCustomer && s != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteCustomer = false },
            title = { Text("Delete customer?") },
            text = { Text("This will permanently delete ${s.name} and all of their transactions.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteCustomer = false
                    vm.deleteCustomer(customerId)
                    onCustomerDeleted()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteCustomer = false }) { Text("Cancel") } }
        )
    }

    txnToDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { txnToDelete = null },
            title = { Text("Delete transaction?") },
            text = { Text("Delete this ${if (t.type == TYPE_RECEIVED) "received" else "given"} entry of ${formatMoney(t.amount)}? Balances will be recalculated.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteTxn(t.id)
                    txnToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { txnToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BalanceCard(s: CustomerSummary) {
    val bal = s.balance
    KCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Current Balance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = balanceColor(bal).copy(alpha = 0.14f)) {
                    Text(
                        statusOf(bal), color = balanceColor(bal), style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
            Text(formatMoney(abs(bal)), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = balanceColor(bal))
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Total Received", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatMoney(s.totalReceived), fontWeight = FontWeight.SemiBold, color = receivedColor(), style = MaterialTheme.typography.titleMedium)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Total Given", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatMoney(s.totalGiven), fontWeight = FontWeight.SemiBold, color = givenColor(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun DateChip(label: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                label.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TxnBubble(t: Txn, onEdit: () -> Unit, onDelete: () -> Unit) {
    val received = t.type == TYPE_RECEIVED
    val accent = if (received) receivedColor() else givenColor()
    val container = if (received) receivedContainer() else givenContainer()
    var menu by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth(), contentAlignment = if (received) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            color = container,
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (received) 20.dp else 4.dp,
                bottomEnd = if (received) 4.dp else 20.dp
            ),
            modifier = Modifier.widthIn(min = 170.dp, max = 300.dp)
        ) {
            Column(Modifier.padding(start = 14.dp, top = 4.dp, end = 4.dp, bottom = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (received) "RECEIVED" else "GIVEN",
                        color = accent, style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        IconButton(onClick = { menu = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = accent, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menu = false; onEdit() })
                            DropdownMenuItem(text = { Text("Delete") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menu = false; onDelete() })
                        }
                    }
                }
                Text(formatMoney(t.amount), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(end = 10.dp))
                if (t.note.isNotBlank()) {
                    Text(t.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp, end = 10.dp))
                }
                Text(
                    "${fullDate(t.date)} · ${prettyTime(t.time)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/** Opens the user's SMS app pre-filled. Nothing is sent automatically and no SMS permission is used. */
private fun sendMessage(ctx: Context, s: CustomerSummary, businessName: String) {
    val phone = s.phone.filter { it.isDigit() || it == '+' }
    if (phone.isEmpty()) {
        Toast.makeText(ctx, "Add a phone number for ${s.name} to send a message", Toast.LENGTH_LONG).show()
        return
    }
    val bal = s.balance
    val balanceLine = when {
        bal > 0 -> "Balance Receivable: ${formatMoney(bal)}"
        bal < 0 -> "Balance Payable: ${formatMoney(-bal)}"
        else -> "Your account is settled."
    }
    val body = buildString {
        append("Hello ${s.name},\n\n")
        append("Your current account balance is ${formatMoney(abs(bal))}.\n\n")
        append("Total Received: ${formatMoney(s.totalReceived)}\n")
        append("Total Given: ${formatMoney(s.totalGiven)}\n\n")
        append(balanceLine)
        append("\n\nThank you.")
        if (businessName.isNotBlank()) append("\n$businessName")
    }
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
        putExtra("sms_body", body)
    }
    try {
        ctx.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(ctx, "No messaging app found on this device", Toast.LENGTH_LONG).show()
    }
}
