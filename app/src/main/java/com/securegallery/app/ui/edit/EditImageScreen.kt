package com.securegallery.app.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.securegallery.app.data.LuxParams

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditImageScreen(
    currentParams: LuxParams,
    luxPresetNames: List<String>,
    onSave: (LuxParams) -> Unit,
    onSaveAsLux: (String, LuxParams) -> Unit,
    onApplyPreset: (LuxParams) -> Unit,
    onBack: () -> Unit
) {
    var brightness by remember { mutableStateOf(currentParams.brightness) }
    var contrast by remember { mutableStateOf(currentParams.contrast) }
    var exposure by remember { mutableStateOf(currentParams.exposure) }
    var highlights by remember { mutableStateOf(currentParams.highlights) }
    var shadows by remember { mutableStateOf(currentParams.shadows) }
    var temperature by remember { mutableStateOf(currentParams.temperature) }
    var tint by remember { mutableStateOf(currentParams.tint) }
    var saturation by remember { mutableStateOf(currentParams.saturation) }
    var vibrance by remember { mutableStateOf(currentParams.vibrance) }
    var clarity by remember { mutableStateOf(currentParams.clarity) }

    val params = LuxParams(
        brightness = brightness,
        contrast = contrast,
        exposure = exposure,
        highlights = highlights,
        shadows = shadows,
        whites = currentParams.whites,
        blacks = currentParams.blacks,
        temperature = temperature,
        tint = tint,
        saturation = saturation,
        vibrance = vibrance,
        clarity = clarity
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adjust (Lightroom style)") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Cancel") }
                },
                actions = {
                    IconButton(onClick = { onSave(params) }) {
                        Icon(Icons.Default.Check, "Save")
                    }
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
            Text("Brightness", style = MaterialTheme.typography.labelMedium)
            Slider(value = brightness, onValueChange = { brightness = it }, valueRange = -1f..1f)
            Text("Contrast", style = MaterialTheme.typography.labelMedium)
            Slider(value = contrast, onValueChange = { contrast = it }, valueRange = -1f..1f)
            Text("Exposure", style = MaterialTheme.typography.labelMedium)
            Slider(value = exposure, onValueChange = { exposure = it }, valueRange = -1f..1f)
            Text("Highlights", style = MaterialTheme.typography.labelMedium)
            Slider(value = highlights, onValueChange = { highlights = it }, valueRange = -1f..1f)
            Text("Shadows", style = MaterialTheme.typography.labelMedium)
            Slider(value = shadows, onValueChange = { shadows = it }, valueRange = -1f..1f)
            Text("Temperature", style = MaterialTheme.typography.labelMedium)
            Slider(value = temperature, onValueChange = { temperature = it }, valueRange = -1f..1f)
            Text("Tint", style = MaterialTheme.typography.labelMedium)
            Slider(value = tint, onValueChange = { tint = it }, valueRange = -1f..1f)
            Text("Saturation", style = MaterialTheme.typography.labelMedium)
            Slider(value = saturation, onValueChange = { saturation = it }, valueRange = -1f..1f)
            Text("Vibrance", style = MaterialTheme.typography.labelMedium)
            Slider(value = vibrance, onValueChange = { vibrance = it }, valueRange = -1f..1f)
            Text("Clarity", style = MaterialTheme.typography.labelMedium)
            Slider(value = clarity, onValueChange = { clarity = it }, valueRange = -1f..1f)

            Spacer(Modifier.height(16.dp))
            if (luxPresetNames.isNotEmpty()) {
                Text("LUX presets", style = MaterialTheme.typography.titleSmall)
                luxPresetNames.forEach { name ->
                    TextButton(onClick = { /* load preset and apply */ }) {
                        Text(name)
                    }
                }
            }
            OutlinedButton(onClick = { /* Save as LUX dialog */ }) {
                Text("Save as LUX preset")
            }
        }
    }
}
