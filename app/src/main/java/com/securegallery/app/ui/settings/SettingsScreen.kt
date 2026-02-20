package com.securegallery.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    trashDays: Int,
    hideBelowKb: Long,
    onTrashDaysChange: (Int) -> Unit,
    onHideBelowKbChange: (Long) -> Unit,
    onBack: () -> Unit
) {
    var trashDaysStr by remember(trashDays) { mutableStateOf(trashDays.toString()) }
    var hideKbStr by remember(hideBelowKb) { mutableStateOf(if (hideBelowKb > 0) hideBelowKb.toString() else "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Trash retention (days)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = trashDaysStr,
                onValueChange = { trashDaysStr = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("Days (e.g. 30)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextButton(onClick = {
                trashDaysStr.toIntOrNull()?.coerceIn(1, 999)?.let { onTrashDaysChange(it) }
            }) { Text("Save") }

            Spacer(Modifier.height(24.dp))
            Text("Hide images smaller than (KB)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = hideKbStr,
                onValueChange = { hideKbStr = it.filter { c -> c.isDigit() } },
                label = { Text("Size (KB), 0 or blank = don't hide") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextButton(onClick = {
                val kb = hideKbStr.toLongOrNull() ?: 0L
                onHideBelowKbChange(kb)
            }) { Text("Save") }
        }
    }
}
