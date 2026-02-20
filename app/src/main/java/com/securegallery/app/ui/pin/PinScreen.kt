package com.securegallery.app.ui.pin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    isSetup: Boolean,
    error: String?,
    onPinEntered: (String) -> Unit,
    onSkip: (() -> Unit)? = null
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSetup) "Set PIN" else "Enter PIN",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
            label = { Text("PIN (4-6 digits)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true
        )
        if (isSetup) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirm = it },
                label = { Text("Confirm PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true
            )
        }
        error?.let { Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(all = 8.dp)) }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (isSetup) {
                    if (pin.length in 4..6 && pin == confirm) onPinEntered(pin)
                } else {
                    if (pin.length in 4..6) onPinEntered(pin)
                }
            },
            enabled = pin.length in 4..6 && (!isSetup || pin == confirm)
        ) {
            Text(if (isSetup) "Set" else "Unlock")
        }
        if (isSetup && onSkip != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSkip) { Text("Skip") }
        }
    }
}
