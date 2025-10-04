package com.fnt.notiglyph.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fnt.notiglyph.domain.model.IconType
import com.fnt.notiglyph.domain.model.NotificationPattern
import com.fnt.notiglyph.domain.model.PatternType
import com.fnt.notiglyph.ui.viewmodel.InstalledApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternEditorScreen(
    pattern: NotificationPattern?,
    installedApps: List<InstalledApp>,
    onLoadInstalledApps: () -> Unit,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    onTestGlyph: (String) -> Unit,
    onUpdatePattern: (
        appPackageName: String,
        appDisplayName: String,
        patternType: PatternType,
        patternString: String,
        displayTemplate: String,
        priority: Int,
        iconType: IconType,
        displayDurationSeconds: Int,
        delaySeconds: Int
    ) -> Unit
) {
    // Load installed apps when screen opens
    LaunchedEffect(Unit) {
        onLoadInstalledApps()
    }
    var appPackageName by remember { mutableStateOf(pattern?.appPackageName ?: "") }
    var appDisplayName by remember { mutableStateOf(pattern?.appDisplayName ?: "") }
    var patternType by remember { mutableStateOf(pattern?.patternType ?: PatternType.TEMPLATE) }
    var patternString by remember { mutableStateOf(pattern?.patternString ?: "") }
    var displayTemplate by remember { mutableStateOf(pattern?.displayTemplate ?: "") }
    var priority by remember { mutableStateOf(pattern?.priority?.toFloat() ?: 5f) }
    var iconType by remember { mutableStateOf(pattern?.iconType ?: IconType.EMOJI) }
    var displayDuration by remember { mutableStateOf(pattern?.displayDurationSeconds ?: 30) }
    var delaySeconds by remember { mutableStateOf(pattern?.delaySeconds ?: 0) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(pattern?.id){
        pattern?.let {
            appPackageName = it.appPackageName
            appDisplayName = it.appDisplayName
            patternType = it.patternType
            patternString = it.patternString
            displayTemplate = it.displayTemplate
            priority = it.priority.toFloat()
            iconType = it.iconType
            displayDuration = it.displayDurationSeconds
            delaySeconds = it.delaySeconds
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pattern?.id == 0L) "New Pattern" else "Edit Pattern") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onUpdatePattern(
                                appPackageName,
                                appDisplayName,
                                patternType,
                                patternString,
                                displayTemplate,
                                priority.toInt(),
                                iconType,
                                displayDuration,
                                delaySeconds
                            )
                            onSave()
                            onNavigateBack()
                        },
                        enabled = appPackageName.isNotEmpty() &&
                                  appDisplayName.isNotEmpty() &&
                                  patternString.isNotEmpty() &&
                                  displayTemplate.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = appDisplayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Select App") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    installedApps.forEach { app ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(app.appName)
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                appDisplayName = app.appName
                                appPackageName = app.packageName
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Pattern Type Selection
            Text("Pattern Type", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PatternType.values().forEach { type ->
                    FilterChip(
                        selected = patternType == type,
                        onClick = { patternType = type },
                        label = { Text(type.name) }
                    )
                }
            }

            // Pattern Input
            Text(
                when (patternType) {
                    PatternType.TEMPLATE -> "Use {variable} to capture parts of the notification. Example: arriving in {minutes} min"
                    PatternType.REGEX -> "Use regular expressions with capture groups. Example: ETA: (\\d+):(\\d+)"
                    PatternType.KEYWORD -> "Use keywords with AND/OR operators. Example: delivered OR arrived"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = patternString,
                onValueChange = { patternString = it },
                label = { Text("Pattern") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        when (patternType) {
                            PatternType.TEMPLATE -> "e.g., arriving in {minutes} min"
                            PatternType.REGEX -> "e.g., ETA: (\\d+):(\\d+)"
                            PatternType.KEYWORD -> "e.g., delivered OR arrived"
                        }
                    )
                },
                minLines = 2
            )

            // Display Template
            OutlinedTextField(
                value = displayTemplate,
                onValueChange = { displayTemplate = it },
                label = { Text("Display Template") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 🍔 {minutes}m") }
            )

            // Test Glyph Button
            Button(
                onClick = { onTestGlyph(displayTemplate) },
                modifier = Modifier.fillMaxWidth(),
                enabled = displayTemplate.isNotEmpty()
            ) {
                Text("Test on Glyph")
            }

            // Priority Slider
            Text("Priority: ${priority.toInt()}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = priority,
                onValueChange = { priority = it },
                valueRange = 1f..10f,
                steps = 8
            )

            // Icon Type
            Text("Icon Type", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconType.values().forEach { type ->
                    FilterChip(
                        selected = iconType == type,
                        onClick = { iconType = type },
                        label = { Text(type.name) }
                    )
                }
            }

            // Display Duration
            OutlinedTextField(
                value = displayDuration.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { duration ->
                        displayDuration = duration
                    }
                },
                label = { Text("Display Duration (seconds)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Display Delay
            Text("Display Delay: $delaySeconds seconds", style = MaterialTheme.typography.titleMedium)
            Text(
                "Wait before showing on Glyph",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = delaySeconds.toFloat(),
                onValueChange = { delaySeconds = it.toInt() },
                valueRange = 0f..30f,
                steps = 29,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
