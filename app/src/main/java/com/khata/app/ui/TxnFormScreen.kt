package com.khata.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.khata.app.KhataViewModel
import com.khata.app.TYPE_GIVEN
import com.khata.app.TYPE_RECEIVED
import com.khata.app.data.Txn
import com.khata.app.fullDate
import com.khata.app.parseAmount
import com.khata.app.plainAmount
import com.khata.app.prettyTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

private val amountRegex = Regex("""\d{0,10}(\.\d{0,2})?""")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxnFormScreen(
    vm: KhataViewModel,
    customerId: Long,
    initialType: String,
    txnId: Long,
    onBack: () -> Unit
) {
    val editing = txnId > 0
    var type by remember { mutableStateOf(initialType) }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    LaunchedEffect(txnId) {
        if (editing) {
            vm.getTxn(txnId)?.let {
                type = it.type
                amount = plainAmount(it.amount)
                date = LocalDate.parse(it.date)
                time = LocalTime.parse(it.time)
                note = it.note
            }
        }
    }

    val received = type == TYPE_RECEIVED
    val accent = if (received) Color(0xFF1E8E4E) else Color(0xFFD13B3B)

    Column(Modifier.fillMaxSize()) {
        KhataTopBar(
            (if (editing) "Edit " else "Add ") + (if (received) "Received" else "Given"),
            onBack
        )
        Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = received, onClick = { type = TYPE_RECEIVED }, label = { Text("Received") })
                FilterChip(selected = !received, onClick = { type = TYPE_GIVEN }, label = { Text("Given") })
            }
            Spacer(Modifier.height(16.dp))

            val err = error
            OutlinedTextField(
                value = amount,
                onValueChange = { v -> if (amountRegex.matches(v)) { amount = v; error = null } },
                label = { Text("Amount *") },
                prefix = { Text("₹ ") },
                singleLine = true,
                isError = err != null,
                supportingText = { if (err != null) Text(err) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PickerField("Date", fullDate(date.toString()), Modifier.weight(1f)) { showDate = true }
                PickerField("Time", prettyTime(time.toString()), Modifier.weight(1f)) { showTime = true }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                minLines = 2,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val paise = parseAmount(amount)
                    if (paise == null || paise <= 0L) {
                        error = "Enter an amount greater than 0"
                    } else {
                        vm.saveTxn(
                            Txn(
                                id = if (editing) txnId else 0,
                                customerId = customerId,
                                type = type,
                                amount = paise,
                                date = date.toString(),
                                time = time.toString(),
                                note = note.trim()
                            )
                        )
                        onBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(if (editing) "Save changes" else "Save", fontSize = 16.sp) }
        }
    }

    if (showDate) {
        val st = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    st.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } }
        ) { DatePicker(state = st) }
    }

    if (showTime) {
        val ts = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = false)
        TimeDialog(
            onDismiss = { showTime = false },
            onConfirm = { time = LocalTime.of(ts.hour, ts.minute); showTime = false }
        ) { TimePicker(state = ts) }
    }
}

@Composable
private fun PickerField(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable(onClick = onClick))
    }
}

@Composable
private fun TimeDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.width(IntrinsicSize.Min).height(IntrinsicSize.Min)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                content()
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}
