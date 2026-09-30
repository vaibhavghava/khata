package com.khata.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.khata.app.KhataViewModel

@Composable
fun CustomerFormScreen(
    vm: KhataViewModel,
    customerId: Long,
    onBack: () -> Unit,
    onSavedNew: (Long) -> Unit,
    onDeleted: () -> Unit
) {
    val editing = customerId > 0
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        if (editing) {
            vm.getCustomer(customerId)?.let {
                name = it.name; phone = it.phone; note = it.note
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        KhataTopBar(if (editing) "Edit Customer" else "Add Customer", onBack)
        Column(
            Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            val err = nameError
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Name *") },
                singleLine = true,
                isError = err != null,
                supportingText = { if (err != null) Text(err) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
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
                    if (name.isBlank()) {
                        nameError = "Customer name is required"
                    } else {
                        vm.saveCustomer(customerId, name.trim(), phone.trim(), note.trim()) { id ->
                            if (editing) onBack() else onSavedNew(id)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(if (editing) "Save changes" else "Save customer") }

            if (editing) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete customer", color = MaterialTheme.colorScheme.error) }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete customer?") },
            text = { Text("This will permanently delete $name and all of their transactions.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteCustomer(customerId)
                    onDeleted()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
