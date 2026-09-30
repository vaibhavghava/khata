package com.khata.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khata.app.KhataViewModel
import com.khata.app.data.Profile

@Composable
fun ProfileScreen(vm: KhataViewModel) {
    val profile by vm.profile.collectAsStateWithLifecycle()
    val theme by vm.theme.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    var name by remember { mutableStateOf("") }
    var business by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        name = profile.name; business = profile.businessName
        phone = profile.phone; address = profile.address
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.trim().take(1).uppercase().ifEmpty { "K" },
                    fontSize = 34.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        ProfileField("Name", name) { name = it }
        ProfileField("Business Name", business) { business = it }
        ProfileField("Phone Number", phone, KeyboardType.Phone) { phone = it }
        ProfileField("Address", address, minLines = 2) { address = it }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                vm.saveProfile(Profile(1, name.trim(), business.trim(), phone.trim(), address.trim()))
                Toast.makeText(ctx, "Profile saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Save profile") }
        Spacer(Modifier.height(20.dp))
        ThemeSelector(theme) { vm.setTheme(it) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    keyboard: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    )
}

@Composable
fun ThemeSelector(current: String, onSelect: (String) -> Unit) {
    KCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            listOf("LIGHT" to "Light", "DARK" to "Dark", "SYSTEM" to "System Default").forEach { (value, label) ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSelect(value) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = current == value, onClick = { onSelect(value) })
                    Text(label)
                }
            }
        }
    }
}
